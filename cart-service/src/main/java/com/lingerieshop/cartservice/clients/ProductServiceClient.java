package com.lingerieshop.cartservice.clients;

import com.lingerieshop.cartservice.dto.ProductResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${app.services.product-service-url}")
public interface ProductServiceClient {

    @GetMapping("/api/v1/products/{productId}")
    ProductResponseDTO getProductById(@PathVariable("productId") Long productId);
}
