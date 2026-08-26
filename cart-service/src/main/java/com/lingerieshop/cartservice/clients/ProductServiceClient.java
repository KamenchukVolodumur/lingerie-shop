package com.lingerieshop.cartservice.clients;

import com.lingerieshop.cartservice.dto.ProductResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "product-service", url = "${app.services.product-service-url}")
public interface ProductServiceClient {

    @GetMapping("/api/v1/products/{productId}")
    ProductResponseDTO getProductById(@PathVariable("productId") Long productId);
    
    // F1-02: Batch endpoint
    @PostMapping("/api/v1/products/batch")
    List<ProductResponseDTO> getProductsByIds(@RequestBody List<Long> ids);
    
    // F2-05: Check stock availability for variant
    @GetMapping("/api/v1/products/{productId}/variants/{variantId}/stock")
    Map<String, Object> checkVariantStock(
        @PathVariable("productId") Long productId,
        @PathVariable("variantId") Long variantId
    );
}
