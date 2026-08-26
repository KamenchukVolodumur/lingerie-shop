package com.lingerieshop.orderservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponseDTO(
        List<CartItemResponseDTO> items,
        BigDecimal totalSum,
        String currency
) {}
