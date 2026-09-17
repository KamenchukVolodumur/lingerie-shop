package com.lingerieshop.paymentservice.dto;

public record PaymentLinkResponseDTO(
        String transactionId, // Наш UUID транзакції
        String paymentUrl) {}
