package com.lingerieshop.cartservice;

/**
 * Deprecated wrapper for backward compatibility. Use com.lingerieshop.cartservice.exceptions.ProductServiceException
 */
@Deprecated
public class ProductServiceException extends com.lingerieshop.cartservice.exceptions.ProductServiceException {
    public ProductServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    public ProductServiceException(String message) { super(message); }
}