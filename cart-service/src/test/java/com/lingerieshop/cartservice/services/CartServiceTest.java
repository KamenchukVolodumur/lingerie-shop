package com.lingerieshop.cartservice.services;

import com.lingerieshop.cartservice.clients.ProductServiceClient;
import com.lingerieshop.cartservice.dto.CartItemRequestDTO;
import com.lingerieshop.cartservice.dto.CartResponseDTO;
import com.lingerieshop.cartservice.dto.ProductResponseDTO;
import com.lingerieshop.cartservice.models.Cart;
import com.lingerieshop.cartservice.models.CartItem;
import com.lingerieshop.cartservice.repositories.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    CartRepository cartRepository;

    @Mock
    ProductServiceClient productClient;

    @InjectMocks
    CartService cartService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void addItem_newCart_createsCartAndReturnsDto() {
        when(cartRepository.findById("c1")).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart saved = invocation.getArgument(0);
            when(cartRepository.findById("c1")).thenReturn(Optional.of(saved));
            return saved;
        });
        when(productClient.getProductById(1L)).thenReturn(new ProductResponseDTO("name", new BigDecimal("10.00"), List.of()));

        CartItemRequestDTO req = new CartItemRequestDTO(1L, 2L, 3);
        CartResponseDTO resp = cartService.addItem("c1", req);

        System.out.println("DEBUG totalSum=" + resp.totalSum() + " items=" + resp.items().size());
        assertNotNull(resp);
        assertEquals(1, resp.items().size());
        assertEquals(0, resp.totalSum().compareTo(new BigDecimal("30.00")), "totalSum should be 30.00");
    }

    @Test
    void updateItemQuantity_reduceToZero_removesItem() {
        Cart cart = new Cart("c2");
        cart.getProducts().add(new CartItem(1L, 2L, 5));
        when(cartRepository.findById("c2")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        CartItemRequestDTO req = new CartItemRequestDTO(1L, 2L, 0);
        CartResponseDTO resp = cartService.updateItemQuantity("c2", req);

        assertNotNull(resp);
        assertEquals(0, resp.items().size());
        assertEquals(BigDecimal.ZERO, resp.totalSum());
    }

    @Test
    void removeItem_existing_removesAndReturnsCart() {
        Cart cart = new Cart("c3");
        cart.getProducts().add(new CartItem(1L, 2L, 2));
        when(cartRepository.findById("c3")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));
        // no need to stub product client because cart becomes empty and mapToDto won't call product service

        var resp = cartService.removeItem("c3", 1L, 2L);
        assertNotNull(resp);
        assertEquals(0, resp.items().size());
        assertEquals(BigDecimal.ZERO, resp.totalSum());
    }
}
