package com.lingerieshop.cartservice;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(String cartId) {
        super("Cart not found: " + cartId);
    }
}