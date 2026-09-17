package com.lingerieshop.productservice.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ProductResponseDTO (
        Map<String, String> attributes,
        Long id,
        String name, // Назва комплекту (наприклад, "Мереживний комплект Luna")
        String slug,
        String description, // Опис товару
        String imageUrl,
        BigDecimal price, // Базова ціна
        Long category,
        List<ProductVariantResponseDTO> variants
){ }
