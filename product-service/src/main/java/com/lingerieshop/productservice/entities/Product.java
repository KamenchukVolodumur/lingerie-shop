package com.lingerieshop.productservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Назва комплекту (наприклад, "Мереживний комплект Luna")

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(length = 1000)
    private String description; // Опис товару

    @Column(nullable = false)
    private BigDecimal price; // Базова ціна

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Зворотний зв'язок: один базовий товар має багато варіацій (розмірів/кольорів)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants;
}