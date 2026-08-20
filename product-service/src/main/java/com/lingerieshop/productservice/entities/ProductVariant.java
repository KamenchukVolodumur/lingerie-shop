package com.lingerieshop.productservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String color; // Колір (наприклад, "Чорний")

    @Column(nullable = false)
    private String size; // Розмір (наприклад, "75B" або "M")

    @Column(nullable = false)
    private Integer stock; // Залишок на складі (скільки штук є фізично)

    // Головний зв'язок: багато варіацій належать до одного базового товару
    // Hibernate сам створить колонку product_id у таблиці product_variants
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
