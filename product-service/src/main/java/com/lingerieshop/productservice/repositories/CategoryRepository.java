package com.lingerieshop.productservice.repositories;

import com.lingerieshop.productservice.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsBySlug(String slug);

    boolean existsBySlugAndCategoryIdNot(String slug, Long categoryId);

    boolean existsByParent_CategoryId(Long parentCategoryId);

    List<Category> findAllByParent_CategoryId(Long parentCategoryId);

    Optional<Category> findBySlug(String slug);

}
