package com.lingerieshop.productservice.config;

import com.lingerieshop.productservice.entities.Category;
import com.lingerieshop.productservice.entities.Product;
import com.lingerieshop.productservice.entities.ProductVariant;
import com.lingerieshop.productservice.repositories.CategoryRepository;
import com.lingerieshop.productservice.repositories.ProductRepository;
import com.lingerieshop.productservice.repositories.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.demo-catalog", havingValue = "true", matchIfMissing = true)
public class CatalogSeedDataRunner implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        productRepository.initializeMissingVersions();
        productVariantRepository.initializeMissingVersions();
        seedCategories();
        seedProducts();
    }

    private void seedCategories() {
        ensureCategory("Бюстгальтери", "bras",
                "Базові, мереживні та підтримуючі моделі для щоденного носіння.", null);
        ensureCategory("Push-up бюстгальтери", "push-up-bras",
                "Моделі з додатковою підтримкою та виразним декольте.", "bras");
        ensureCategory("Бралети", "bralettes",
                "М'які та комфортні бралети без жорстких кісточок.", "bras");
        ensureCategory("Трусики", "panties",
                "Щоденні та святкові моделі різної посадки.", null);
        ensureCategory("Стрінги", "thongs",
                "Легкі моделі для непомітної посадки під одягом.", "panties");
        ensureCategory("Сліпи", "briefs",
                "Класичні моделі з комфортною середньою посадкою.", "panties");
        ensureCategory("Комплекти", "sets",
                "Готові поєднання бюстгальтера та трусиків для швидкого вибору.", null);
        ensureCategory("Нічний одяг", "sleepwear",
                "Піжами, халати та домашній одяг у фірмовому стилі.", null);
        ensureCategory("Піжами", "pajamas",
                "Комплекти для сну та відпочинку з м'яких тканин.", "sleepwear");
        ensureCategory("Халати", "robes",
                "Легкі халати для дому та подарункових наборів.", "sleepwear");
    }

    private void seedProducts() {
        ensureProduct(
                "Luna Push-Up Bra",
                "luna-push-up-bra",
                "Мереживний push-up бюстгальтер з м'якими чашками та гладкою посадкою.",
                new BigDecimal("1499.00"),
                "push-up-bras",
                List.of(
                        variant("Black", "75B", 8),
                        variant("Black", "80B", 5),
                        variant("Burgundy", "75C", 2)
                )
        );

        ensureProduct(
                "Aurora Lace Bra",
                "aurora-lace-bra",
                "Бралет з еластичного мережива для щоденного комфорту.",
                new BigDecimal("1299.00"),
                "bralettes",
                List.of(
                        variant("Ivory", "S", 9),
                        variant("Ivory", "M", 4),
                        variant("Powder Pink", "L", 0)
                )
        );

        ensureProduct(
                "Velvet Support Bra",
                "velvet-support-bra",
                "Бюстгальтер з щільнішою підтримкою для повсякденного образу.",
                new BigDecimal("1399.00"),
                "push-up-bras",
                List.of(
                        variant("Mocha", "80C", 7),
                        variant("Mocha", "85C", 3),
                        variant("Graphite", "80D", 1)
                )
        );

        ensureProduct(
                "Ruby String",
                "ruby-string",
                "Тонкі стрінги з мікрофібри та мереживною вставкою.",
                new BigDecimal("549.00"),
                "thongs",
                List.of(
                        variant("Ruby", "S", 12),
                        variant("Ruby", "M", 8),
                        variant("Black", "M", 6)
                )
        );

        ensureProduct(
                "Cloud Slip Briefs",
                "cloud-slip-briefs",
                "Класичні сліпи з м'якого бавовняного полотна.",
                new BigDecimal("599.00"),
                "briefs",
                List.of(
                        variant("Milk", "M", 10),
                        variant("Milk", "L", 7),
                        variant("Lavender", "XL", 2)
                )
        );

        ensureProduct(
                "Midnight Bloom Set",
                "midnight-bloom-set",
                "Комплект з мереживного бюстгальтера та трусиків у вечірньому стилі.",
                new BigDecimal("2299.00"),
                "sets",
                List.of(
                        variant("Black", "S", 6),
                        variant("Black", "M", 4),
                        variant("Emerald", "M", 0)
                )
        );

        ensureProduct(
                "Ivory Whisper Set",
                "ivory-whisper-set",
                "Світлий комплект для романтичних образів та подарунків.",
                new BigDecimal("2399.00"),
                "sets",
                List.of(
                        variant("Ivory", "S", 5),
                        variant("Ivory", "M", 5),
                        variant("Blush", "L", 3)
                )
        );

        ensureProduct(
                "Silk Touch Pajama Set",
                "silk-touch-pajama-set",
                "Атласна піжама з сорочкою на ґудзиках і прямими штанами.",
                new BigDecimal("2499.00"),
                "pajamas",
                List.of(
                        variant("Champagne", "S", 4),
                        variant("Champagne", "M", 4),
                        variant("Navy", "L", 2)
                )
        );

        ensureProduct(
                "Rose Mist Robe",
                "rose-mist-robe",
                "Легкий халат з поясом і сатиновим блиском.",
                new BigDecimal("1899.00"),
                "robes",
                List.of(
                        variant("Rose", "S/M", 7),
                        variant("Rose", "L/XL", 5),
                        variant("Black", "S/M", 1)
                )
        );
    }

    private void ensureCategory(String name, String slug, String description, String parentSlug) {
        Category category = categoryRepository.findBySlug(slug).orElseGet(Category::new);
        category.setCategoryName(name);
        category.setSlug(slug);
        category.setDescription(description);
        category.setParent(parentSlug == null ? null : categoryRepository.findBySlug(parentSlug)
                .orElseThrow(() -> new IllegalStateException("Parent category not found: " + parentSlug)));
        categoryRepository.save(category);
    }

    private void ensureProduct(
            String name,
            String slug,
            String description,
            BigDecimal price,
            String categorySlug,
            List<VariantSeed> variants
    ) {
        Product product = productRepository.findBySlug(slug).orElseGet(Product::new);
        product.setName(name);
        product.setSlug(slug);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(categoryRepository.findBySlug(categorySlug)
                .orElseThrow(() -> new IllegalStateException("Category not found: " + categorySlug)));

        List<ProductVariant> productVariants = product.getVariants();
        if (productVariants == null) {
            productVariants = new ArrayList<>();
            product.setVariants(productVariants);
        } else {
            productVariants.clear();
        }

        for (VariantSeed variantSeed : variants) {
            ProductVariant variant = new ProductVariant();
            variant.setColor(variantSeed.color());
            variant.setSize(variantSeed.size());
            variant.setStock(variantSeed.stock());
            variant.setProduct(product);
            productVariants.add(variant);
        }

        productRepository.save(product);
    }

    private VariantSeed variant(String color, String size, int stock) {
        return new VariantSeed(color, size, stock);
    }

    private record VariantSeed(String color, String size, int stock) {
    }
}
