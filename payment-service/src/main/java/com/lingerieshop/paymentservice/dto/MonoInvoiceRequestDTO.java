package com.lingerieshop.paymentservice.dto;

public record MonoInvoiceRequestDTO(
        Integer amount,       // Monobank приймає суму в копійках (100 грн = 10000)
        Integer ccy,          // Код валюти (980 для UAH)
        String redirectUrl,   // Куди повернути клієнта після успішної оплати
        String webHookUrl,    // Куди банку слати приховані сповіщення про статуси
        String paymentType
) {}
