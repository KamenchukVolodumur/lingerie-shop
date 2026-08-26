package com.lingerieshop.cartservice.dto;

import java.math.BigDecimal;

public record CartItemResponseDTO (
        Long productId,
        Long productVariantId,
        int quantity,
        String productName,
        String productImageUrl,
        String size,           // F2-02: Added - fetched dynamically from product variant
        String color,          // F2-02: Added - fetched dynamically from product variant
        BigDecimal unitPrice,
        BigDecimal itemTotal
) {}
