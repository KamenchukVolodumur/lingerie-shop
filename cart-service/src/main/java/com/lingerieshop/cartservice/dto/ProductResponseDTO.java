package com.lingerieshop.cartservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponseDTO(
        String name,
        BigDecimal price,
        List<ProductVariantResponseDTO> variants
) { }
