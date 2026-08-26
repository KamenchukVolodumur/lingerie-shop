package com.lingerieshop.productservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockAdjustmentRequestDTO(
        @NotNull(message = "productVariantId is required")
        Long productVariantId,

        @Positive(message = "quantity must be greater than 0")
        int quantity
) {
}
