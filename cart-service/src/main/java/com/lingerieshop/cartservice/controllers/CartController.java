package com.lingerieshop.cartservice.controllers;

import com.lingerieshop.cartservice.dto.CartItemRequestDTO;
import com.lingerieshop.cartservice.dto.CartResponseDTO;
import com.lingerieshop.cartservice.services.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponseDTO> getCart(@PathVariable String cartId) {
        return ResponseEntity.ok(cartService.getCart(cartId));
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartResponseDTO> addItem(@PathVariable String cartId,
                                                   @Valid @RequestBody CartItemRequestDTO request) {
        return ResponseEntity.ok(cartService.addItem(cartId, request));
    }

    @PutMapping("/{cartId}/items")
    public ResponseEntity<CartResponseDTO> updateItem(@PathVariable String cartId,
                                                      @Valid @RequestBody CartItemRequestDTO request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(cartId, request));
    }

    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<CartResponseDTO> removeItem(@PathVariable String cartId,
                                                      @RequestParam Long productId,
                                                      @RequestParam Long productVariantId) {
        return ResponseEntity.ok(cartService.removeItem(cartId, productId, productVariantId));
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> clearCart(@PathVariable String cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cartId}/ttl")
    public ResponseEntity<Map<String, Long>> setTTL(@PathVariable String cartId,
                                                    @RequestParam long ttlSeconds) {
        long ttl = cartService.setTTL(cartId, ttlSeconds);
        return ResponseEntity.ok(Map.of("ttl", ttl));
    }
}
