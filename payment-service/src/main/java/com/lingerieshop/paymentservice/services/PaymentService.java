package com.lingerieshop.paymentservice.services;

import com.lingerieshop.paymentservice.clients.MonobankClient;
import com.lingerieshop.paymentservice.dto.MonoInvoiceRequestDTO;
import com.lingerieshop.paymentservice.dto.MonoInvoiceResponseDTO;
import com.lingerieshop.paymentservice.dto.PaymentLinkResponseDTO;
import com.lingerieshop.paymentservice.dto.PaymentRequestDTO;
import com.lingerieshop.paymentservice.entities.PaymentTransaction;
import com.lingerieshop.paymentservice.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final MonobankClient monobankClient;
    @Value("${app.monobank.token}")
    private String monobankToken;


    private PaymentTransaction mapToPaymentTransaction(PaymentRequestDTO requestDTO) {
        return new PaymentTransaction().builder()
                .orderId(requestDTO.orderId())
                .amount(requestDTO.amount())
                .build();
    }
    private MonoInvoiceRequestDTO mapToMonoInvoiceRequestDTO(PaymentRequestDTO requestDTO) {
        int amount = requestDTO.amount().multiply(new java.math.BigDecimal(100)).intValue();
        MonoInvoiceRequestDTO monoRequest = new MonoInvoiceRequestDTO(
                amount,
                980,
                "https://твій-магазин.com/order-success",
                "https://api.твій-магазин.com/api/v1/payments/webhook",
                "hold" // Ключовий параметр для двостадійної оплати[cite: 9]
        );
        return monoRequest;
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

}
