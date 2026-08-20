package com.lingerieshop.productservice.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(nullable = false)
    private String categoryName;

    @Column(nullable = false, unique = true)
    private String slug; // URL-ідентифікатор ("bras", "push-up")

    @Column(length = 500)
    private String description;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    // ⭐️ Список ДОЧІРНІХ категорій (підкатегорій)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> children = new ArrayList<>();

    // Зв'язок з товарами (товари прив'язуються до конкретної категорії)
    @OneToMany(mappedBy = "category")
    private List<Product> products;

}
