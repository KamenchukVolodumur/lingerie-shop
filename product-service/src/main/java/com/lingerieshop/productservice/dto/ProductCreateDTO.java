package com.lingerieshop.productservice.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record ProductCreateDTO (
        @NotBlank(message = "Назва не може бути порожньою") // Validation
         String name,

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