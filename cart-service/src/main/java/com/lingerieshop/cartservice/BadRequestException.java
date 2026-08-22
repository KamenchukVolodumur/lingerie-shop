package com.lingerieshop.cartservice;

/**
 * Deprecated wrapper for backward compatibility. Use com.lingerieshop.cartservice.exceptions.BadRequestException
 */
@Deprecated
public class BadRequestException extends com.lingerieshop.cartservice.exceptions.BadRequestException {
    public BadRequestException(String message) {
        super(message);
    }
}