package com.lingerieshop.orderservice.clients;

import com.lingerieshop.orderservice.dto.CartResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cart-service", url = "${app.services.cart-service-url}")
public interface CartServiceClient {

    @GetMapping("/api/carts/{cartId}")
    CartResponseDTO getCart(@PathVariable("cartId") String cartId);

    @DeleteMapping("/api/carts/{cartId}")
    void clearCart(@PathVariable("cartId") String cartId);
}
