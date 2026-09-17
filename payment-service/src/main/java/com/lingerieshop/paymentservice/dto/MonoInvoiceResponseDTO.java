package com.lingerieshop.paymentservice.dto;

public record MonoInvoiceResponseDTO (
        String invoiceId, // Їхній унікальний номер транзакції (збережемо як externalBankReference)
        String pageUrl
){}
