package com.lingerieshop.productservice.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


public record ProductVariantCreateDTO (
        @NotBlank(message = "Розмір не може бути пустим")
        String size,

        @NotBlank(message = "Колір не може бути пустим")
        String color,

        @NotNull
        @Min(value = 0, message = "Кількість на складі не може бути від'ємною")
        Integer stockQuantity
){ }
