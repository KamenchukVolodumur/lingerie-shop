package com.lingerieshop.productservice.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateDTO(
        @NotBlank(message = "Назва категорії не може бути пустою")
        String categoryName,

        @NotBlank(message = "Slug не може бути пустим")
        String slug,

        String description,

        // Якщо немає батьківської категорії, значення може бути null
        Long parentId
) { }
