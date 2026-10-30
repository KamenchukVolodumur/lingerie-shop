package com.lingerieshop.paymentservice.exceptions;

public class PaymentStateConflictException extends RuntimeException {

    public PaymentStateConflictException(String message) {
        super(message);
    }
}
