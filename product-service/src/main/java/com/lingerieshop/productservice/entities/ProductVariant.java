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
    private String color;

    @Column(nullable = false)
    private String size;

    @Column(nullable = false)
    private Integer stock;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Version
    private Long version;  // F2-07: Optimistic Locking
}
