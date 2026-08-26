package com.lingerieshop.productservice.controllers;

import com.lingerieshop.productservice.dto.ProductCreateDTO;
import com.lingerieshop.productservice.dto.ProductResponseDTO;
import com.lingerieshop.productservice.dto.StockAdjustmentRequestDTO;
import com.lingerieshop.productservice.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(Pageable pageable) {
        Page<ProductResponseDTO> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    // F1-02: Batch endpoint for fetching multiple products
    @PostMapping("/batch")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByIds(@RequestBody List<Long> ids) {
        List<ProductResponseDTO> products = productService.getProductsByIds(ids);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/deduct")
    public ResponseEntity<Void> deductStock(@Valid @RequestBody List<StockAdjustmentRequestDTO> items) {
        productService.deductStock(items);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> releaseStock(@Valid @RequestBody List<StockAdjustmentRequestDTO> items) {
        productService.releaseStock(items);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductResponseDTO> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }
    
    // F2-05: Check stock availability for variant
    @GetMapping("/{productId}/variants/{variantId}/stock")
    public ResponseEntity<Map<String, Object>> checkVariantStock(
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        Map<String, Object> stockInfo = productService.checkVariantStock(productId, variantId);
        return ResponseEntity.ok(stockInfo);
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductCreateDTO productDto) {
        ProductResponseDTO createdProduct = productService.createProduct(productDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductCreateDTO productDto) {
        ProductResponseDTO updatedProduct = productService.updateProduct(id, productDto);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
