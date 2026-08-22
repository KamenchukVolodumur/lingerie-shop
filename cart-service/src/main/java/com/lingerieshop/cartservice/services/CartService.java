package com.lingerieshop.cartservice.services;

import com.lingerieshop.cartservice.BadRequestException;
import com.lingerieshop.cartservice.CartNotFoundException;
import com.lingerieshop.cartservice.ProductServiceException;
import com.lingerieshop.cartservice.clients.ProductServiceClient;
import com.lingerieshop.cartservice.dto.CartItemRequestDTO;
import com.lingerieshop.cartservice.dto.CartItemResponseDTO;
import com.lingerieshop.cartservice.dto.CartResponseDTO;
import com.lingerieshop.cartservice.dto.ProductResponseDTO;
import com.lingerieshop.cartservice.models.Cart;
import com.lingerieshop.cartservice.models.CartItem;
import com.lingerieshop.cartservice.repositories.CartRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final ProductServiceClient productClient;

    public CartService(CartRepository cartRepository, ProductServiceClient productClient) {
        this.cartRepository = cartRepository;
        this.productClient = productClient;
    }

    public CartResponseDTO getCart(String cartId) {
        if (cartId == null || cartId.isBlank()) throw new BadRequestException("cartId is required");
        Optional<Cart> opt = cartRepository.findById(cartId);
        if (opt.isEmpty()) {
            return new CartResponseDTO(List.of(), BigDecimal.ZERO, "USD");
        }
        return mapToDto(opt.get());
    }

    public CartResponseDTO addItem(String cartId, CartItemRequestDTO requestDto) {
        validateRequest(requestDto);
        synchronized (cartId.intern()) {
            Cart cart = cartRepository.findById(cartId).orElse(new Cart(cartId));
            // merge quantities when same product+variant
            boolean merged = false;
            for (CartItem it : cart.getProducts()) {
                if (it.getProductId().equals(requestDto.productId()) && it.getProductVariantId().equals(requestDto.productVariantId())) {
                    it.setQuantity(it.getQuantity() + requestDto.quantity());
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                cart.getProducts().add(new CartItem(requestDto.productId(), requestDto.productVariantId(), requestDto.quantity()));
            }
            cartRepository.save(cart);
        }
        return getCart(cartId);
    }

    public CartResponseDTO updateItemQuantity(String cartId, CartItemRequestDTO requestDto) {
        validateRequest(requestDto);
        synchronized (cartId.intern()) {
            Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new CartNotFoundException(cartId));
            boolean found = false;
            for (CartItem it : new ArrayList<>(cart.getProducts())) {
                if (it.getProductId().equals(requestDto.productId()) && it.getProductVariantId().equals(requestDto.productVariantId())) {
                    found = true;
                    if (requestDto.quantity() <= 0) {
                        cart.getProducts().remove(it);
                    } else {
                        it.setQuantity(requestDto.quantity());
                    }
                    break;
                }
            }
            if (!found) throw new BadRequestException("Item not found in cart");
            cartRepository.save(cart);
        }
        return getCart(cartId);
    }

    public CartResponseDTO removeItem(String cartId, Long productId, Long productVariantId) {
        if (productId == null || productVariantId == null) throw new BadRequestException("productId and productVariantId are required");
        synchronized (cartId.intern()) {
            Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new CartNotFoundException(cartId));
            cart.getProducts().removeIf(it -> it.getProductId().equals(productId) && it.getProductVariantId().equals(productVariantId));
            cartRepository.save(cart);
        }
        return getCart(cartId);
    }

    public long setTTL(String cartId, long ttlSeconds) {
        if (ttlSeconds <= 0) throw new BadRequestException("ttl must be positive seconds");
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new CartNotFoundException(cartId));
        cart.setTimeToLive(ttlSeconds);
        cartRepository.save(cart);
        return ttlSeconds;
    }

    private void validateRequest(CartItemRequestDTO requestDto) {
        if (requestDto == null) throw new BadRequestException("request body is required");
        if (requestDto.productId() == null || requestDto.productVariantId() == null) throw new BadRequestException("productId and productVariantId are required");
        if (requestDto.quantity() <= 0) throw new BadRequestException("quantity must be > 0");
    }

    private CartResponseDTO mapToDto(Cart cart) {
        List<CartItemResponseDTO> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem it : cart.getProducts()) {
            try {
                ProductResponseDTO product = productClient.getProductById(it.getProductId());
                BigDecimal unitPrice = product.price() != null ? product.price() : BigDecimal.ZERO;
                BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(it.getQuantity()));
                total = total.add(itemTotal);
                items.add(new CartItemResponseDTO(
                        it.getProductId(),
                        it.getProductVariantId(),
                        it.getQuantity(),
                        product.name(),
                        null,
                        unitPrice,
                        itemTotal
                ));
            } catch (FeignException fe) {
                log.warn("Product service request failed for productId={}: {}", it.getProductId(), fe.getMessage());
                throw new ProductServiceException("Failed to fetch product info", fe);
            } catch (Exception e) {
                log.warn("Unexpected error fetching product {}: {}", it.getProductId(), e.getMessage());
                throw new ProductServiceException("Failed to fetch product info", e);
            }
        }
        // deterministic ordering
        items.sort(Comparator.comparing(CartItemResponseDTO::productId));
        return new CartResponseDTO(items, total, "USD");
    }
}
