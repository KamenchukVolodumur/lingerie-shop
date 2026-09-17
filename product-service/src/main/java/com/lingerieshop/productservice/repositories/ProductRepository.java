package com.lingerieshop.productservice.repositories;

import com.lingerieshop.productservice.entities.Product;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    boolean existsByCategory_CategoryId(Long categoryId);

    @Query("SELECT p FROM Product p JOIN FETCH p.category LEFT JOIN FETCH p.variants WHERE p.category.categoryId = :categoryId")
    List<Product> findAllByCategory_CategoryId(@org.springframework.data.repository.query.Param("categoryId") Long categoryId);
    
    // F1-03: Pagination support
    Page<Product> findAll(Pageable pageable);
    
    // F1-02: Batch endpoint support
    @Query("SELECT p FROM Product p JOIN FETCH p.category LEFT JOIN FETCH p.variants WHERE p.id IN :ids")
    List<Product> findAllById(@org.springframework.data.repository.query.Param("ids") Iterable<Long> ids);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"variants", "category"})
    Optional<Product> findBySlug(String slug);

    @Modifying
    @Query("update Product p set p.version = 0 where p.version is null")
    int initializeMissingVersions();

    @Query("SELECT DISTINCT p FROM Product p JOIN FETCH p.category LEFT JOIN FETCH p.variants WHERE p.category.categoryId IN :categoryIds")
    List<Product> findAllByCategory_CategoryIdIn(@org.springframework.data.repository.query.Param("categoryIds") List<Long> categoryIds);

    @Query("SELECT DISTINCT p FROM Product p JOIN FETCH p.category LEFT JOIN FETCH p.variants WHERE p.id IN :ids")
    List<Product> findWithVariantsByIdIn(@org.springframework.data.repository.query.Param("ids") List<Long> ids);

    interface AttributeFilterProjection {
        String getAttributeKey();
        String getAttributeValue();
    }

    @Query(value = """
        SELECT DISTINCT
            key as attributeKey,
            value as attributeValue
        FROM
            products p,
            jsonb_each_text(p.attributes)
        WHERE
            p.category_id IN :categoryIds
    """, nativeQuery = true)
    List<AttributeFilterProjection> findDistinctAttributesByCategoryIds(@org.springframework.data.repository.query.Param("categoryIds") List<Long> categoryIds);

    @Query(value = "SELECT p.id FROM products p WHERE p.category_id IN :categoryIds AND p.attributes @> cast(:attributesJson as jsonb)", nativeQuery = true)
    List<Long> findProductIdsByAttributes(@org.springframework.data.repository.query.Param("categoryIds") List<Long> categoryIds, @org.springframework.data.repository.query.Param("attributesJson") String attributesJson);
}
