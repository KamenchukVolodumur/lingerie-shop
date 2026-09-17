package com.lingerieshop.productservice.services;

import com.lingerieshop.productservice.entities.Category;
import com.lingerieshop.productservice.entities.Product;
import com.lingerieshop.productservice.entities.ProductVariant;
import com.lingerieshop.productservice.dto.StockAdjustmentRequestDTO;
import com.lingerieshop.productservice.repositories.CategoryRepository;
import com.lingerieshop.productservice.repositories.ProductRepository;
import com.lingerieshop.productservice.repositories.ProductVariantRepository;
import com.lingerieshop.productservice.dto.ProductCreateDTO;
import com.lingerieshop.productservice.dto.ProductVariantCreateDTO;
import com.lingerieshop.productservice.dto.ProductResponseDTO;
import com.lingerieshop.productservice.dto.ProductVariantResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    
    private Product getProductEntityByID(Long id){
        return productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }
    
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long productId) {
         return mapToProductResponseDTO(getProductEntityByID(productId));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
        List<Long> ids = productPage.getContent().stream().map(Product::getId).toList();
        if (!ids.isEmpty()) {
            // Завантажуємо варіанти в кеш Hibernate (L1 cache) одним запитом
            productRepository.findWithVariantsByIdIn(ids); 
        }
        return productPage.map(this::mapToProductResponseDTO);
    }
    
    // F1-02: Batch endpoint for fetching multiple products
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getProductsByIds(List<Long> ids) {
        return productRepository.findAllById(ids).stream()
                .map(this::mapToProductResponseDTO)
                .toList();
    }


    @Transactional
    public ProductResponseDTO createProduct(ProductCreateDTO productDto) {
        validateSlugIsAvailable(productDto.slug(), null);

        Category category = getCategoryOrThrow(productDto.categoryId());

        Product product = new Product();
        product.setName(productDto.name());
        product.setDescription(productDto.description());
        product.setImageUrl(productDto.imageUrl());
        product.setSlug(productDto.slug());
        product.setPrice(productDto.price());
        product.setCategory(category);
        product.setAttributes(productDto.attributes() != null ? productDto.attributes() : new HashMap<>());

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

    @Transactional(rollbackFor = Exception.class)
    public ProductResponseDTO updateProduct(Long productId, ProductCreateDTO updatedProductDto) {
        Product existingProduct = getProductEntityByID(productId);

        validateSlugIsAvailable(updatedProductDto.slug(), productId);

        Category category = getCategoryOrThrow(updatedProductDto.categoryId());

        existingProduct.setName(updatedProductDto.name());
        existingProduct.setDescription(updatedProductDto.description());
        existingProduct.setImageUrl(updatedProductDto.imageUrl());
        existingProduct.setSlug(updatedProductDto.slug());
        existingProduct.setPrice(updatedProductDto.price());
        existingProduct.setCategory(category);
        existingProduct.setAttributes(updatedProductDto.attributes() != null ? updatedProductDto.attributes() : new HashMap<>());

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

    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long productId) {
        Product product = getProductEntityByID(productId);
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getProductsByCategory(Long categoryId) {
        getCategoryOrThrow(categoryId);
        return productRepository.findAllByCategory_CategoryId(categoryId).stream().map(this::mapToProductResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, java.util.Set<String>> getCategoryFilters(Long categoryId) {
        List<Long> categoryIds = getCategoryAndChildrenIds(categoryId);
        List<ProductRepository.AttributeFilterProjection> projections = productRepository.findDistinctAttributesByCategoryIds(categoryIds);
        
        Map<String, java.util.Set<String>> filters = new HashMap<>();
        for (ProductRepository.AttributeFilterProjection p : projections) {
            filters.computeIfAbsent(p.getAttributeKey(), k -> new java.util.HashSet<>()).add(p.getAttributeValue());
        }
        return filters;
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> searchProducts(Long categoryId, Map<String, String> attributes) {
        List<Long> categoryIds = getCategoryAndChildrenIds(categoryId);
        List<Product> products;
        
        if (attributes == null || attributes.isEmpty()) {
            products = productRepository.findAllByCategory_CategoryIdIn(categoryIds);
        } else {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String json = mapper.writeValueAsString(attributes);
                List<Long> productIds = productRepository.findProductIdsByAttributes(categoryIds, json);
                
                if (productIds.isEmpty()) {
                    products = new ArrayList<>();
                } else {
                    products = productRepository.findWithVariantsByIdIn(productIds);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to process attributes JSON", e);
            }
        }
        return products.stream().map(this::mapToProductResponseDTO).toList();
    }

    private List<Long> getCategoryAndChildrenIds(Long categoryId) {
        List<Category> allCategories = categoryRepository.findAll();
        java.util.Set<Long> ids = new java.util.HashSet<>();
        ids.add(categoryId);
        collectChildrenIds(categoryId, allCategories, ids);
        return new ArrayList<>(ids);
    }

    private void collectChildrenIds(Long parentId, List<Category> allCategories, java.util.Set<Long> ids) {
        for (Category cat : allCategories) {
            if (cat.getParent() != null && cat.getParent().getCategoryId().equals(parentId)) {
                ids.add(cat.getCategoryId());
                collectChildrenIds(cat.getCategoryId(), allCategories, ids);
            }
        }
    }

    @Transactional
    public void deductStock(List<StockAdjustmentRequestDTO> items) {
        normalizeStockRequests(items).forEach((variantId, quantity) -> {
            ProductVariant variant = getVariantOrThrow(variantId);
            int currentStock = variant.getStock() != null ? variant.getStock() : 0;

            if (currentStock < quantity) {
                throw new IllegalStateException(
                        "Insufficient stock for variant " + variantId + ": requested " + quantity + ", available " + currentStock);
            }

            variant.setStock(currentStock - quantity);
        });
    }

    @Transactional
    public void releaseStock(List<StockAdjustmentRequestDTO> items) {
        normalizeStockRequests(items).forEach((variantId, quantity) -> {
            ProductVariant variant = getVariantOrThrow(variantId);
            int currentStock = variant.getStock() != null ? variant.getStock() : 0;
            variant.setStock(currentStock + quantity);
        });
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

    private ProductVariant getVariantOrThrow(Long variantId) {
        return productVariantRepository.findById(variantId)
                .orElseThrow(() -> new EntityNotFoundException("Product variant not found: " + variantId));
    }

    private Map<Long, Integer> normalizeStockRequests(List<StockAdjustmentRequestDTO> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Stock adjustment items are required");
        }

        Map<Long, Integer> quantitiesByVariantId = new LinkedHashMap<>();
        for (StockAdjustmentRequestDTO item : items) {
            if (item == null) {
                throw new IllegalArgumentException("Stock adjustment item cannot be null");
            }

            if (item.productVariantId() == null) {
                throw new IllegalArgumentException("productVariantId is required");
            }

            if (item.quantity() <= 0) {
                throw new IllegalArgumentException("quantity must be greater than 0");
            }

            quantitiesByVariantId.merge(item.productVariantId(), item.quantity(), Integer::sum);
        }
        return quantitiesByVariantId;
    }


    private ProductResponseDTO mapToProductResponseDTO(Product product) {
        List<ProductVariantResponseDTO> variantDTOs = null;

        if (product.getVariants() != null) {
            variantDTOs = product.getVariants().stream()
                    .map(this::mapToVariantResponseDTO)
                    .toList();
        }

        return new ProductResponseDTO(
                product.getAttributes() != null ? product.getAttributes() : new HashMap<>(),
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getImageUrl(),
                product.getPrice(),
                product.getCategory() != null ? product.getCategory().getCategoryId() : null,
                new ArrayList<ProductVariantResponseDTO>(variantDTOs)
        );
    }

    // F2-05: Check stock availability for variant
    @Transactional(readOnly = true)
    public Map<String, Object> checkVariantStock(Long productId, Long variantId) {
        Product product = getProductEntityByID(productId);
        
        ProductVariant variant = product.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Variant not found for product " + productId));
        
        int stock = variant.getStock() != null ? variant.getStock() : 0;
        boolean available = stock > 0;
        
        return Map.of(
                "available", available,
                "stockLeft", stock,
                "productId", productId,
                "variantId", variantId,
                "size", variant.getSize(),
                "color", variant.getColor()
        );
    }

    private ProductVariantResponseDTO mapToVariantResponseDTO(ProductVariant variant) {
        return new ProductVariantResponseDTO(
                variant.getId(),
                variant.getColor(),
                variant.getSize(),
                variant.getStock(),
                variant.getProduct().getId()
        );
    }
}
