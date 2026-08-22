package com.lingerieshop.cartservice;

/**
 * Deprecated wrapper for backward compatibility. Use com.lingerieshop.cartservice.exceptions.CartNotFoundException
 */
@Deprecated
public class CartNotFoundException extends com.lingerieshop.cartservice.exceptions.CartNotFoundException {
    public CartNotFoundException(String cartId) {
        super(cartId);
    }
}