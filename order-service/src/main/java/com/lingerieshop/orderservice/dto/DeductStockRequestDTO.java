package com.lingerieshop.orderservice.dto;
public record DeductStockRequestDTO(
        Long productVariantId,
        int quantity
) {}