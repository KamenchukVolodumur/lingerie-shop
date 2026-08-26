package com.lingerieshop.orderservice.dto;

import java.math.BigDecimal;

public record OrderItemResponseDTO(
        Long id,
        Long productId,
        Long productVariantId,
        int quantity,
        String productName,
        String variantSize,
        String variantColor,
        BigDecimal pricePerUnit,
        BigDecimal totalPrice
) {}
