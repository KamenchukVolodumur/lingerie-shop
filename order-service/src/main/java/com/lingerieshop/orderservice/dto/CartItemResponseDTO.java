package com.lingerieshop.orderservice.dto;

import java.math.BigDecimal;

public record CartItemResponseDTO(
        Long productId,
        Long productVariantId,
        int quantity,
        String productName,
        String productImageUrl,
        String size,
        String color,
        BigDecimal unitPrice,
        BigDecimal itemTotal
) {}
