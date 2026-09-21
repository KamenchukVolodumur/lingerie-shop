package com.lingerieshop.orderservice.dto;

import java.math.BigDecimal;

public record PaymentRequestDTO(Long orderId, BigDecimal amount) {
}
