package com.lingerieshop.orderservice.dto;

import com.lingerieshop.orderservice.entities.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequestDTO(
        @NotNull(message = "userId is required")
        Long userId,
        
        @NotBlank(message = "cartId is required")
        String cartId,
        
        @NotNull(message = "paymentMethod is required")
        PaymentMethod paymentMethod,
        
        @NotBlank(message = "shippingAddress is required")
        String shippingAddress,
        
        @NotBlank(message = "shippingPhone is required")
        String shippingPhone
) {
}
