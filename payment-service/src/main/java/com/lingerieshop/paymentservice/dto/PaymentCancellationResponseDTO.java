package com.lingerieshop.paymentservice.dto;

import com.lingerieshop.paymentservice.entities.PaymentStatus;

public record PaymentCancellationResponseDTO(PaymentStatus paymentStatus) {
}
