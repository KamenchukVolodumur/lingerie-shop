package com.lingerieshop.productservice.dto;

public record CategoryResponseDTO(
        Long categoryId,
        String categoryName,
        String slug,
        String description,
        Long parentId
) { }
