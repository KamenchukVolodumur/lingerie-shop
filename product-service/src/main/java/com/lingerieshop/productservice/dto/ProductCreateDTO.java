package com.lingerieshop.productservice.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ProductCreateDTO (
        Map<String, String> attributes,
        @NotBlank(message = "Назва не може бути порожньою") // Validation
         String name,

        @NotBlank(message = "Назва не може бути порожньою") // Validation
        String description,

        String imageUrl,

        @NotNull
        @Min(value = 1, message = "Ціна має бути більше 0") // Validation
        BigDecimal price,

        @NotBlank
        String slug,

        @NotNull
        Long categoryId,

        @NotEmpty(message = "Товар повинен мати хоча б один розмір/варіант")
        @Valid
        List<ProductVariantCreateDTO> variants
){}