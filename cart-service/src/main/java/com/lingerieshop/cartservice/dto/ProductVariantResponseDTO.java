package com.lingerieshop.cartservice.dto;

public record ProductVariantResponseDTO(
        String color,
        String size,
        Integer stock // Залишок на складі (скільки штук є фізично)
) { }
