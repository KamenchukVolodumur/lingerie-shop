package com.lingerieshop.paymentservice.services;

import com.lingerieshop.paymentservice.clients.MonobankClient;
import com.lingerieshop.paymentservice.clients.OrderServiceClient;
import com.lingerieshop.paymentservice.dto.MonoInvoiceRequestDTO;
import com.lingerieshop.paymentservice.dto.MonoInvoiceResponseDTO;
import com.lingerieshop.paymentservice.dto.MonoInvoiceFinalizeRequestDTO;
import com.lingerieshop.paymentservice.dto.MonoInvoiceFinalizeResponseDTO;
import com.lingerieshop.paymentservice.dto.MonoInvoiceRemoveRequestDTO;
import com.lingerieshop.paymentservice.dto.PaymentLinkResponseDTO;
import com.lingerieshop.paymentservice.dto.PaymentRequestDTO;
import com.lingerieshop.paymentservice.entities.PaymentTransaction;
import com.lingerieshop.paymentservice.entities.PaymentStatus;
import com.lingerieshop.paymentservice.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final MonobankClient monobankClient;
    private final OrderServiceClient orderServiceClient;
    private final ObjectMapper objectMapper;
    private final MonobankWebhookSignatureVerifier signatureVerifier;
    @Value("${app.monobank.token}")
    private String monobankToken;
    @Value("${app.urls.order-success-url}")
    private String orderSuccessUrl;
    @Value("${app.monobank.web-hook-url}")
    private String webHookUrl;
    @Value("${app.monobank.hold-expiration:PT24H}")
    private Duration holdExpiration;

    private PaymentTransaction mapToPaymentTransaction(PaymentRequestDTO requestDTO) {
        return new PaymentTransaction().builder()
                .orderId(requestDTO.orderId())
                .amount(requestDTO.amount())
                .status(PaymentStatus.PENDING)
                .build();
    }
    private MonoInvoiceRequestDTO mapToMonoInvoiceRequestDTO(PaymentRequestDTO requestDTO) {
        int amount = requestDTO.amount().movePointRight(2).intValueExact();
        return new MonoInvoiceRequestDTO(
                amount,
                980,
                orderSuccessUrl,
                webHookUrl,
                "hold"
        );
    }
    @Transactional
    public PaymentLinkResponseDTO generatePaymentLink(PaymentRequestDTO requestDTO){
        // Створюємо транзакцію в базі даних
        PaymentTransaction transaction = mapToPaymentTransaction(requestDTO);
        paymentRepository.save(transaction);

        // Викликаємо Monobank API для створення інвойсу
        MonoInvoiceRequestDTO monoRequest = mapToMonoInvoiceRequestDTO(requestDTO);
        MonoInvoiceResponseDTO monoResponse = monobankClient.createInvoice(monobankToken, monoRequest);

        // Оновлюємо транзакцію з посиланням на оплату та зовнішнім ключем
        transaction.setExternalBankReference(monoResponse.invoiceId());
        paymentRepository.save(transaction);

        return new PaymentLinkResponseDTO(
                transaction.getId(),
                monoResponse.pageUrl()
        );
    }

    @Transactional
    public void processWebHook(String webhookPayload, String signature) {
        signatureVerifier.verify(webhookPayload, signature);
        JsonNode payload = readWebhookPayload(webhookPayload);
        String invoiceId = requiredText(payload, "invoiceId");
        String status = requiredText(payload, "status").toLowerCase(Locale.ROOT);

        PaymentTransaction transaction = paymentRepository.findByExternalBankReference(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found for invoice: " + invoiceId));

        if ("hold".equals(status)) {
            transaction.setStatus(PaymentStatus.HOLD);
        } else if ("failure".equals(status) || "expired".equals(status) || "reversed".equals(status)) {
            transaction.setStatus(PaymentStatus.CANCELED);
            orderServiceClient.markPaymentFailed(transaction.getOrderId());
        }
    }

    @Scheduled(fixedDelayString = "${app.monobank.hold-expiration-check-interval:PT5M}")
    @Transactional
    public void cancelExpiredHolds() {
        List<PaymentTransaction> expiredHolds = paymentRepository.findByStatusAndUpdatedAtBefore(
                PaymentStatus.HOLD,
                LocalDateTime.now().minus(holdExpiration));
        expiredHolds.forEach(transaction -> cancelPayment(transaction.getOrderId()));
    }

    @Transactional
    public void capturePayment(Long orderId) {
        PaymentTransaction transaction = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found for order: " + orderId));
        if (transaction.getStatus() != PaymentStatus.HOLD) {
            throw new IllegalStateException("Only held payments can be captured, current status: " + transaction.getStatus());
        }

        MonoInvoiceFinalizeResponseDTO response = monobankClient.finalizeInvoice(
                monobankToken,
                new MonoInvoiceFinalizeRequestDTO(
                        transaction.getExternalBankReference(),
                        transaction.getAmount().movePointRight(2).longValueExact()));
        if (!"success".equalsIgnoreCase(response.status())) {
            throw new IllegalStateException("Monobank did not accept payment capture");
        }

        transaction.setStatus(PaymentStatus.PAID);
    }

    @Transactional
    public void cancelPayment(Long orderId) {
        PaymentTransaction transaction = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found for order: " + orderId));
        if (transaction.getStatus() == PaymentStatus.CANCELED) {
            return;
        }
        if (transaction.getStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Captured payments must be refunded, not canceled");
        }

        monobankClient.removeInvoice(
                monobankToken,
                new MonoInvoiceRemoveRequestDTO(transaction.getExternalBankReference()));
        transaction.setStatus(PaymentStatus.CANCELED);
    }

    private JsonNode readWebhookPayload(String webhookPayload) {
        try {
            return objectMapper.readTree(webhookPayload);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Invalid Monobank webhook payload", exception);
        }
    }

    private String requiredText(JsonNode payload, String fieldName) {
        String value = payload.path(fieldName).asText(null);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Monobank webhook payload has no " + fieldName);
        }
        return value;
    }
}
