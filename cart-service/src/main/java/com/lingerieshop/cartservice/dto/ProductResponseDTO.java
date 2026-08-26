package com.lingerieshop.cartservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponseDTO(
        Long id,                                           // F2-01: Added
        String name,
        String slug,                                       // F2-01: Added
        BigDecimal price,
        List<ProductVariantResponseDTO> variants
) { }
