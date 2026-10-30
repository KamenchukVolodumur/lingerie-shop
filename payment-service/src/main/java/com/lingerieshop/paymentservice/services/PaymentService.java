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
import com.lingerieshop.paymentservice.dto.PaymentCancellationResponseDTO;
import com.lingerieshop.paymentservice.entities.PaymentTransaction;
import com.lingerieshop.paymentservice.entities.PaymentStatus;
import com.lingerieshop.paymentservice.exceptions.MonobankOperationException;
import com.lingerieshop.paymentservice.exceptions.PaymentStateConflictException;
import com.lingerieshop.paymentservice.exceptions.PaymentTransactionNotFoundException;
import com.lingerieshop.paymentservice.repositories.PaymentRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        MonoInvoiceResponseDTO monoResponse = createInvoice(monoRequest);

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
                .orElseThrow(() -> new PaymentTransactionNotFoundException(
                        "Payment transaction not found for invoice: " + invoiceId));

        if ("hold".equals(status) && transaction.getStatus() != PaymentStatus.RELEASE_PENDING) {
            transaction.setStatus(PaymentStatus.HOLD);
        } else if ("failure".equals(status)) {
            transaction.setStatus(PaymentStatus.CANCELED);
            orderServiceClient.markPaymentFailed(transaction.getOrderId());
        } else if ("expired".equals(status) || "reversed".equals(status)) {
            transaction.setStatus(PaymentStatus.CANCELED);
            orderServiceClient.markPaymentCanceled(transaction.getOrderId());
        }
    }

    @Transactional
    public void capturePayment(Long orderId) {
        PaymentTransaction transaction = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentTransactionNotFoundException(
                        "Payment transaction not found for order: " + orderId));
        if (transaction.getStatus() != PaymentStatus.HOLD) {
            throw new PaymentStateConflictException(
                    "Only held payments can be captured, current status: " + transaction.getStatus());
        }

        finalizeInvoice(transaction, transaction.getAmount().movePointRight(2).longValueExact(), "capture");
        transaction.setStatus(PaymentStatus.PAID);
    }

    @Transactional
    public PaymentCancellationResponseDTO cancelPayment(Long orderId) {
        PaymentTransaction transaction = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentTransactionNotFoundException(
                        "Payment transaction not found for order: " + orderId));

        switch (transaction.getStatus()) {
            case CANCELED, RELEASE_PENDING -> {
                return new PaymentCancellationResponseDTO(transaction.getStatus());
            }
            case PENDING -> {
                removeInvoice(transaction);
                transaction.setStatus(PaymentStatus.CANCELED);
            }
            case HOLD -> transaction.setStatus(PaymentStatus.RELEASE_PENDING);
            case PAID -> throw new PaymentStateConflictException(
                    "Captured payments must be refunded, not canceled");
        }

        return new PaymentCancellationResponseDTO(transaction.getStatus());
    }

    private MonoInvoiceResponseDTO createInvoice(MonoInvoiceRequestDTO request) {
        try {
            return monobankClient.createInvoice(monobankToken, request);
        } catch (FeignException exception) {
            throw new MonobankOperationException("Monobank could not create an invoice", exception);
        }
    }

    private void removeInvoice(PaymentTransaction transaction) {
        try {
            monobankClient.removeInvoice(
                    monobankToken,
                    new MonoInvoiceRemoveRequestDTO(transaction.getExternalBankReference()));
        } catch (FeignException exception) {
            throw new MonobankOperationException(
                    "Monobank could not deactivate the unpaid invoice", exception);
        }
    }

    private void finalizeInvoice(PaymentTransaction transaction, long amount, String operation) {
        try {
            MonoInvoiceFinalizeResponseDTO response = monobankClient.finalizeInvoice(
                    monobankToken,
                    new MonoInvoiceFinalizeRequestDTO(transaction.getExternalBankReference(), amount));
            if (!"success".equalsIgnoreCase(response.status())) {
                throw new MonobankOperationException("Monobank did not accept payment " + operation);
            }
        } catch (FeignException exception) {
            throw new MonobankOperationException(
                    "Monobank could not complete payment " + operation, exception);
        }
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
