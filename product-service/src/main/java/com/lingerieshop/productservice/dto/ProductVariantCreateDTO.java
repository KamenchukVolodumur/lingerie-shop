package com.lingerieshop.productservice.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


public record ProductVariantCreateDTO (
        @NotBlank(message = "Розмір не може бути пустим")
        String size,

        @NotBlank(message = "Колір не може бути пустим")
        String color,

        @NotNull(message = "Кількість на складі обов'язкова")
        @Min(value = 1, message = "Кількість на складі має бути щонайменше 1")
        Integer stockQuantity
){ }
