package com.lingerieshop.orderservice.dto;

import com.lingerieshop.orderservice.entities.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateOrderRequestDTO(
        @NotNull(message = "userId is required")
        Long userId,
        
        @NotBlank(message = "cartId is required")
        String cartId,
        
        @NotNull(message = "paymentMethod is required")
        PaymentMethod paymentMethod,
        
        @NotBlank(message = "Ім'я клієнта не може бути порожнім")
        String customerName,

        @NotBlank(message = "Email не може бути порожнім")
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Введіть коректну email адресу (наприклад, example@domain.com)")
        String email,

        @NotBlank(message = "Провайдер доставки обов'язковий (NOVA_POSHTA або UKRPOSHTA)")
        String deliveryProvider,

        @NotBlank(message = "Місто доставки не може бути порожнім")
        String deliveryCity,

        @NotBlank(message = "Відділення не може бути порожнім")
        String deliveryBranch,
        
        @NotBlank(message = "Номер телефону не може бути порожнім")
        @Pattern(regexp = "^\\+380\\d{9}$", message = "Номер телефону має бути у форматі +380XXXXXXXXX")
        String shippingPhone
) {
}
