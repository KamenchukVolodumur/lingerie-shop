package com.lingerieshop.cartservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemRequestDTO(
        @NotNull Long productId,
        @NotNull Long productVariantId,
        @Min(1) int quantity
) {
}
