package com.lingerieshop.productservice.controllers;

import com.lingerieshop.productservice.dto.CategoryCreateDTO;
import com.lingerieshop.productservice.dto.CategoryResponseDTO;
import com.lingerieshop.productservice.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.lingerieshop.productservice.services.ProductService;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        CategoryResponseDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<List<CategoryResponseDTO>> getChildCategories(@PathVariable Long id) {
        List<CategoryResponseDTO> childCategories = categoryService.getChildCategories(id);
        return ResponseEntity.ok(childCategories);
    }

    @GetMapping("/{id}/filters")
    public ResponseEntity<Map<String, Set<String>>> getCategoryFilters(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getCategoryFilters(id));
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryCreateDTO categoryDto) {
        CategoryResponseDTO createdCategory = categoryService.createCategory(categoryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryCreateDTO categoryDto) {
        CategoryResponseDTO updatedCategory = categoryService.updateCategory(id, categoryDto);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
