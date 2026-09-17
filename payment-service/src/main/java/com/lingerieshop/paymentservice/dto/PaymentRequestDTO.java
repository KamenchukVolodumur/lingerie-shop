package com.lingerieshop.paymentservice.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentRequestDTO(
        @NotNull Long orderId,
        @NotNull @DecimalMin("0.01") BigDecimal amount) {}
