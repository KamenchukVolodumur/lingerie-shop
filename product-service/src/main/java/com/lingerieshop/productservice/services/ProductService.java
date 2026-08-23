package com.lingerieshop.productservice.services;

import com.lingerieshop.productservice.entities.Category;
import com.lingerieshop.productservice.entities.Product;
import com.lingerieshop.productservice.entities.ProductVariant;
import com.lingerieshop.productservice.repositories.CategoryRepository;
import com.lingerieshop.productservice.repositories.ProductRepository;
import com.lingerieshop.productservice.dto.ProductCreateDTO;
import com.lingerieshop.productservice.dto.ProductVariantCreateDTO;
import com.lingerieshop.productservice.dto.ProductResponseDTO;
import com.lingerieshop.productservice.dto.ProductVariantResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private Product getProductEntityByID(Long id){
        return productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long productId) {
         return mapToProductResponseDTO(getProductEntityByID(productId));
    }

    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream().map(this::mapToProductResponseDTO).toList();
    }


    @Transactional
    public ProductResponseDTO createProduct(ProductCreateDTO productDto) {
        validateSlugIsAvailable(productDto.slug(), null);

        Category category = getCategoryOrThrow(productDto.categoryId());

        Product product = new Product();
        product.setName(productDto.name());
        product.setDescription(productDto.description());
        product.setSlug(productDto.slug());
        product.setPrice(productDto.price());
        product.setCategory(category);

        if (productDto.variants() != null) {
            List<ProductVariant> variants = new ArrayList<>();
            productDto.variants().forEach(vdto -> {
                ProductVariant variant = new ProductVariant();
                variant.setSize(vdto.size());
                variant.setColor(vdto.color());
                variant.setStock(vdto.stockQuantity());
                variant.setProduct(product);
                variants.add(variant);
            });
            product.setVariants(variants);
        }

        Product result = productRepository.save(product);
        return mapToProductResponseDTO(result);
    }

    @Transactional
    public ProductResponseDTO updateProduct(Long productId, ProductCreateDTO updatedProductDto) {
        Product existingProduct = getProductEntityByID(productId);

        validateSlugIsAvailable(updatedProductDto.slug(), productId);

        Category category = getCategoryOrThrow(updatedProductDto.categoryId());

        existingProduct.setName(updatedProductDto.name());
        existingProduct.setDescription(updatedProductDto.description());
        existingProduct.setSlug(updatedProductDto.slug());
        existingProduct.setPrice(updatedProductDto.price());
        existingProduct.setCategory(category);

        existingProduct.getVariants().clear();
        if (updatedProductDto.variants() != null) {
            updatedProductDto.variants().forEach(vdto -> {
                ProductVariant variant = new ProductVariant();
                variant.setSize(vdto.size());
                variant.setColor(vdto.color());
                variant.setStock(vdto.stockQuantity());
                variant.setProduct(existingProduct);
                existingProduct.getVariants().add(variant);
            });
        }

        Product result = productRepository.save(existingProduct);
        return mapToProductResponseDTO(result);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = getProductEntityByID(productId);
        productRepository.delete(product);
    }

    @Transactional
    public List<ProductResponseDTO> getProductsByCategory(Long categoryId) {
        getCategoryOrThrow(categoryId);
        return productRepository.findAllByCategory_CategoryId(categoryId).stream().map(this::mapToProductResponseDTO).toList();
    }


    private void validateSlugIsAvailable(String slug, Long currentProductId) {
        boolean slugAlreadyExists;
        if (currentProductId != null){
            slugAlreadyExists = productRepository.existsBySlugAndIdNot(slug, currentProductId);
        }else{
            slugAlreadyExists = productRepository.existsBySlug(slug);
        }

        if (slugAlreadyExists) {
            throw new IllegalArgumentException("Product with this slug already exists");
        }
    }

    private Category getCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category was not found"));
    }


    private ProductResponseDTO mapToProductResponseDTO(Product product) {
        List<ProductVariantResponseDTO> variantDTOs = null;

        if (product.getVariants() != null) {
            variantDTOs = product.getVariants().stream()
                    .map(this::mapToVariantResponseDTO) // Мапимо кожен варіант
                    .toList();
        }

        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory() != null ? product.getCategory().getCategoryId() : null,
                new ArrayList<ProductVariantResponseDTO>(variantDTOs)
        );
    }

    //  Допоміжний мапер для розміру/варіанту
    private ProductVariantResponseDTO mapToVariantResponseDTO(ProductVariant variant) {
        return new ProductVariantResponseDTO(
                variant.getId(),
                variant.getSize(),
                variant.getColor(),
                variant.getStock(),
                variant.getProduct().getId()
        );
    }
}

