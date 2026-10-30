package com.lingerieshop.paymentservice.exceptions;

public class PaymentTransactionNotFoundException extends RuntimeException {

    public PaymentTransactionNotFoundException(String message) {
        super(message);
    }
}
