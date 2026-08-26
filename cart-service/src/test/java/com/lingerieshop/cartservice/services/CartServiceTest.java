package com.lingerieshop.cartservice.services;

import com.lingerieshop.cartservice.clients.ProductServiceClient;
import com.lingerieshop.cartservice.dto.CartItemRequestDTO;
import com.lingerieshop.cartservice.dto.CartResponseDTO;
import com.lingerieshop.cartservice.dto.ProductResponseDTO;
import com.lingerieshop.cartservice.dto.ProductVariantResponseDTO;
import com.lingerieshop.cartservice.models.Cart;
import com.lingerieshop.cartservice.models.CartItem;
import com.lingerieshop.cartservice.repositories.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    CartRepository cartRepository;

    @Mock
    ProductServiceClient productClient;
    
    @Mock
    RedissonClient redissonClient;

    @Mock
    RLock lock;

    @InjectMocks
    CartService cartService;

    @BeforeEach
    void setUp() {
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        try {
            when(lock.tryLock(anyLong(), anyLong(), any())).thenReturn(true);
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
        when(lock.isHeldByCurrentThread()).thenReturn(true);
    }

    @Test
    void addItem_newCart_createsCartAndReturnsDto() {
        AtomicReference<Cart> savedCart = new AtomicReference<>();
        when(cartRepository.findById("c1")).thenAnswer(invocation -> Optional.ofNullable(savedCart.get()));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart saved = invocation.getArgument(0);
            savedCart.set(saved);
            return saved;
        });
        
        ProductVariantResponseDTO variant = new ProductVariantResponseDTO(2L, "Red", "M", 10);
        ProductResponseDTO product = new ProductResponseDTO(1L, "name", "slug", new BigDecimal("10.00"), List.of(variant));
        when(productClient.getProductById(1L)).thenReturn(product);

        CartItemRequestDTO req = new CartItemRequestDTO(1L, 2L, 3);
        CartResponseDTO resp = cartService.addItem("c1", req);

        assertNotNull(resp);
        assertEquals(1, resp.items().size());
        assertEquals(0, resp.totalSum().compareTo(new BigDecimal("30.00")), "totalSum should be 30.00");
        assertEquals("M", resp.items().get(0).size(), "size should be M");
        assertEquals("Red", resp.items().get(0).color(), "color should be Red");
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

        var resp = cartService.removeItem("c3", 1L, 2L);
        assertNotNull(resp);
        assertEquals(0, resp.items().size());
        assertEquals(BigDecimal.ZERO, resp.totalSum());
    }

    @Test
    void clearCart_existingCart_deletesCart() {
        when(cartRepository.existsById("c4")).thenReturn(true);

        cartService.clearCart("c4");

        verify(cartRepository).deleteById("c4");
    }
}
