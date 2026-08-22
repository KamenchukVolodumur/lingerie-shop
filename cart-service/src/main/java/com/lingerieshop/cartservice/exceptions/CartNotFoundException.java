package com.lingerieshop.cartservice.exceptions;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(String cartId) {
        super("Cart not found: " + cartId);
    }
}