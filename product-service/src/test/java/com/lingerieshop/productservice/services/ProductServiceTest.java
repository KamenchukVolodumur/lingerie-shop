package com.lingerieshop.productservice.services;

import com.lingerieshop.productservice.dto.StockAdjustmentRequestDTO;
import com.lingerieshop.productservice.entities.ProductVariant;
import com.lingerieshop.productservice.repositories.CategoryRepository;
import com.lingerieshop.productservice.repositories.ProductRepository;
import com.lingerieshop.productservice.repositories.ProductVariantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    ProductVariantRepository productVariantRepository;

    @InjectMocks
    ProductService productService;

    @Test
    void deductStock_reducesAvailableQuantity() {
        ProductVariant variant = new ProductVariant();
        variant.setId(10L);
        variant.setStock(7);
        when(productVariantRepository.findById(10L)).thenReturn(Optional.of(variant));

        productService.deductStock(List.of(new StockAdjustmentRequestDTO(10L, 3)));

        assertEquals(4, variant.getStock());
    }

    @Test
    void deductStock_throwsWhenStockIsInsufficient() {
        ProductVariant variant = new ProductVariant();
        variant.setId(10L);
        variant.setStock(2);
        when(productVariantRepository.findById(10L)).thenReturn(Optional.of(variant));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> productService.deductStock(List.of(new StockAdjustmentRequestDTO(10L, 3)))
        );

        assertEquals("Insufficient stock for variant 10: requested 3, available 2", exception.getMessage());
    }

    @Test
    void releaseStock_increasesAvailableQuantity() {
        ProductVariant variant = new ProductVariant();
        variant.setId(10L);
        variant.setStock(2);
        when(productVariantRepository.findById(10L)).thenReturn(Optional.of(variant));

        productService.releaseStock(List.of(new StockAdjustmentRequestDTO(10L, 5)));

        assertEquals(7, variant.getStock());
    }
}
