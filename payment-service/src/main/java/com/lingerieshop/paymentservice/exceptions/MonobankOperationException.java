package com.lingerieshop.paymentservice.exceptions;

public class MonobankOperationException extends RuntimeException {

    public MonobankOperationException(String message) {
        super(message);
    }

    public MonobankOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
