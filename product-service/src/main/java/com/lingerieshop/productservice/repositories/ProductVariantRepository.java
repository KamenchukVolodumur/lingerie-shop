package com.lingerieshop.productservice.repositories;

import com.lingerieshop.productservice.entities.ProductVariant;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    @Modifying
    @Query("update ProductVariant pv set pv.version = 0 where pv.version is null")
    int initializeMissingVersions();
}
