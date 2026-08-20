package com.lingerieshop.productservice.dto;

public record ProductVariantResponseDTO(
        Long id,
        String color,
        String size,
        Integer stock, // Залишок на складі (скільки штук є фізично)
        Long productId
) { }
