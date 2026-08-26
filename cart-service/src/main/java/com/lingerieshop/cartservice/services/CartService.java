package com.lingerieshop.cartservice.services;

import com.lingerieshop.cartservice.exceptions.BadRequestException;
import com.lingerieshop.cartservice.exceptions.CartNotFoundException;
import com.lingerieshop.cartservice.exceptions.ProductServiceException;
import com.lingerieshop.cartservice.clients.ProductServiceClient;
import com.lingerieshop.cartservice.dto.CartItemRequestDTO;
import com.lingerieshop.cartservice.dto.CartItemResponseDTO;
import com.lingerieshop.cartservice.dto.CartResponseDTO;
import com.lingerieshop.cartservice.dto.ProductResponseDTO;
import com.lingerieshop.cartservice.dto.ProductVariantResponseDTO;
import com.lingerieshop.cartservice.models.Cart;
import com.lingerieshop.cartservice.models.CartItem;
import com.lingerieshop.cartservice.repositories.CartRepository;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class CartService {
    private static final Logger log = LoggerFactory.getLogger(CartService.class);
    private static final long LOCK_TIMEOUT_SECONDS = 5;
    private static final long DEFAULT_CART_TTL_SECONDS = 604800; // 7 days for unauthorized users

    private final CartRepository cartRepository;
    private final ProductServiceClient productClient;
    private final RedissonClient redissonClient;

    public CartService(CartRepository cartRepository, ProductServiceClient productClient, RedissonClient redissonClient) {
        this.cartRepository = cartRepository;
        this.productClient = productClient;
        this.redissonClient = redissonClient;
    }

    public CartResponseDTO getCart(String cartId) {
        if (cartId == null || cartId.isBlank()) throw new BadRequestException("cartId is required");
        Optional<Cart> opt = cartRepository.findById(cartId);
        if (opt.isEmpty()) {
            return new CartResponseDTO(List.of(), BigDecimal.ZERO, "USD");
        }
        Cart cart = opt.get();
        resetCartTTL(cart);
        return mapToDto(cart);
    }

    public CartResponseDTO addItem(String cartId, CartItemRequestDTO requestDto) {
        validateAddRequest(requestDto);
        
        RLock lock = redissonClient.getLock("cart:" + cartId);
        try {
            if (!lock.tryLock(LOCK_TIMEOUT_SECONDS, LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new BadRequestException("Could not acquire lock for cart: " + cartId);
            }
            
            Cart cart = cartRepository.findById(cartId).orElse(new Cart(cartId));
            
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
            resetCartTTL(cart);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        
        return getCart(cartId);
    }

    public CartResponseDTO updateItemQuantity(String cartId, CartItemRequestDTO requestDto) {
        validateUpdateRequest(requestDto);
        
        RLock lock = redissonClient.getLock("cart:" + cartId);
        try {
            if (!lock.tryLock(LOCK_TIMEOUT_SECONDS, LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new BadRequestException("Could not acquire lock for cart: " + cartId);
            }
            
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
            resetCartTTL(cart);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        
        return getCart(cartId);
    }

    public CartResponseDTO removeItem(String cartId, Long productId, Long productVariantId) {
        if (productId == null || productVariantId == null) throw new BadRequestException("productId and productVariantId are required");
        
        RLock lock = redissonClient.getLock("cart:" + cartId);
        try {
            if (!lock.tryLock(LOCK_TIMEOUT_SECONDS, LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new BadRequestException("Could not acquire lock for cart: " + cartId);
            }
            
            Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new CartNotFoundException(cartId));
            cart.getProducts().removeIf(it -> it.getProductId().equals(productId) && it.getProductVariantId().equals(productVariantId));
            cartRepository.save(cart);
            resetCartTTL(cart);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        
        return getCart(cartId);
    }

    public void clearCart(String cartId) {
        if (cartId == null || cartId.isBlank()) throw new BadRequestException("cartId is required");

        RLock lock = redissonClient.getLock("cart:" + cartId);
        try {
            if (!lock.tryLock(LOCK_TIMEOUT_SECONDS, LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new BadRequestException("Could not acquire lock for cart: " + cartId);
            }

            if (cartRepository.existsById(cartId)) {
                cartRepository.deleteById(cartId);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public long setTTL(String cartId, long ttlSeconds) {
        if (ttlSeconds <= 0) throw new BadRequestException("ttl must be positive seconds");
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new CartNotFoundException(cartId));
        cart.setTimeToLive(ttlSeconds);
        cartRepository.save(cart);
        return ttlSeconds;
    }

    private void resetCartTTL(Cart cart) {
        cart.setTimeToLive(DEFAULT_CART_TTL_SECONDS);
        cartRepository.save(cart);
    }

    private void validateAddRequest(CartItemRequestDTO requestDto) {
        if (requestDto == null) throw new BadRequestException("request body is required");
        if (requestDto.productId() == null || requestDto.productVariantId() == null) throw new BadRequestException("productId and productVariantId are required");
        if (requestDto.quantity() <= 0) throw new BadRequestException("quantity must be > 0");
    }

    private void validateUpdateRequest(CartItemRequestDTO requestDto) {
        if (requestDto == null) throw new BadRequestException("request body is required");
        if (requestDto.productId() == null || requestDto.productVariantId() == null) throw new BadRequestException("productId and productVariantId are required");
        if (requestDto.quantity() < 0) throw new BadRequestException("quantity must be >= 0");
    }

    @CircuitBreaker(name = "productClient", fallbackMethod = "fallbackGetProducts")
    private CartResponseDTO mapToDto(Cart cart) {
        List<CartItemResponseDTO> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        
        for (CartItem it : cart.getProducts()) {
            try {
                ProductResponseDTO product = productClient.getProductById(it.getProductId());
                BigDecimal unitPrice = product.price() != null ? product.price() : BigDecimal.ZERO;
                BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(it.getQuantity()));
                total = total.add(itemTotal);
                
                // F2-03: Dynamic lookup of variant details (size, color)
                ProductVariantResponseDTO variant = null;
                if (product.variants() != null) {
                    variant = product.variants().stream()
                            .filter(v -> v.id().equals(it.getProductVariantId()))
                            .findFirst()
                            .orElse(null);
                }
                
                String size = (variant != null) ? variant.size() : "N/A";
                String color = (variant != null) ? variant.color() : "N/A";
                
                items.add(new CartItemResponseDTO(
                        it.getProductId(),
                        it.getProductVariantId(),
                        it.getQuantity(),
                        product.name(),
                        null,
                        size,
                        color,
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
        
        items.sort(Comparator.comparing(CartItemResponseDTO::productId));
        return new CartResponseDTO(items, total, "USD");
    }
    
    public CartResponseDTO fallbackGetProducts(Cart cart, Exception ex) {
        log.error("Circuit breaker triggered for product service: {}", ex.getMessage());
        throw new ProductServiceException("Product service is unavailable. Please try again later.", ex);
    }
}
