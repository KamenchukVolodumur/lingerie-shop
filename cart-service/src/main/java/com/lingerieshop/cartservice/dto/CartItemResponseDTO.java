package com.lingerieshop.cartservice.dto;

import java.math.BigDecimal;

public record CartItemResponseDTO (
        Long productId,
        Long productVariantId,
        int quantity,
        String productName,
        String productImageUrl,
        BigDecimal unitPrice,
        BigDecimal itemTotal
        ) {}
