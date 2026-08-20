package com.lingerieshop.productservice.repositories;

import com.lingerieshop.productservice.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    boolean existsByCategory_CategoryId(Long categoryId);

    List<Product> findAllByCategory_CategoryId(Long categoryId);
}
