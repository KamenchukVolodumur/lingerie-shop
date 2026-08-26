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

    List<Product> findAllByCategory_CategoryId(Long categoryId);
    
    // F1-03: Pagination support
    Page<Product> findAll(Pageable pageable);
    
    // F1-02: Batch endpoint support
    List<Product> findAllById(Iterable<Long> ids);

    Optional<Product> findBySlug(String slug);

    @Modifying
    @Query("update Product p set p.version = 0 where p.version is null")
    int initializeMissingVersions();
}
