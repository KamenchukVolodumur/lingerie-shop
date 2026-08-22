package com.lingerieshop.productservice.dto;

public record ProductVariantResponseDTO(
        Long id,
        String color,
        String size,

        //!!!!!!!Подумати чи точно лишати сток!!!!!!!!!!

        Integer stock, // Залишок на складі (скільки штук є фізично)
        Long productId
) { }
