package com.lingerieshop.productservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponseDTO (
        Long id,
        String name, // Назва комплекту (наприклад, "Мереживний комплект Luna")
        String slug,
        String description, // Опис товару
        String imageUrl,
        BigDecimal price, // Базова ціна
        Long category,
        List<ProductVariantResponseDTO> variants
){ }
