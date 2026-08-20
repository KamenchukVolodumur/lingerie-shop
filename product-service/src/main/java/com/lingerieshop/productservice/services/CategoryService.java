package com.lingerieshop.productservice.services;

import com.lingerieshop.productservice.dto.CategoryResponseDTO;
import com.lingerieshop.productservice.entities.Category;
import com.lingerieshop.productservice.repositories.CategoryRepository;
import com.lingerieshop.productservice.repositories.ProductRepository;
import com.lingerieshop.productservice.dto.CategoryCreateDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    public Category getCategoryEntityById(Long id){
        return  categoryRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }
    public CategoryResponseDTO getCategoryById(Long categoryId) {
        return MapToCategoryResponseDTO(getCategoryEntityById(categoryId));
    }

    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::MapToCategoryResponseDTO).toList();
    }

    public List<CategoryResponseDTO> getChildCategories(Long parentCategoryId) {
        getCategoryEntityById(parentCategoryId); // 👈 Перевіряємо через приватний метод

        return categoryRepository.findAllByParent_CategoryId(parentCategoryId)
                .stream() // 👈 Відкриваємо потік
                .map(this::MapToCategoryResponseDTO) // 👈 Перетворюємо кожну сутність на DTO
                .toList(); // 👈 Збираємо назад у список
    }

    @Transactional
    public CategoryResponseDTO createCategory(CategoryCreateDTO categoryDto) { // 👈 Прибрали @Valid
        validateSlugIsAvailable(categoryDto.slug(), null);

        Category parent = getParentOrNull(categoryDto.parentId());

        Category category = new Category();
        category.setCategoryName(categoryDto.categoryName());
        category.setSlug(categoryDto.slug());
        category.setDescription(categoryDto.description());
        category.setParent(parent);

        Category savedCategory = categoryRepository.save(category);
        return MapToCategoryResponseDTO(savedCategory); // 👈 Повертаємо маплений DTO
    }

    @Transactional
    public CategoryResponseDTO updateCategory(Long categoryId, CategoryCreateDTO updatedCategoryDto) {
        Category existingCategory = getCategoryEntityById(categoryId);

        validateSlugIsAvailable(updatedCategoryDto.slug(), categoryId);

        Category parent = getParentOrNull(updatedCategoryDto.parentId());
        validateParent(existingCategory, parent);

        existingCategory.setCategoryName(updatedCategoryDto.categoryName());
        existingCategory.setSlug(updatedCategoryDto.slug());
        existingCategory.setDescription(updatedCategoryDto.description());
        existingCategory.setParent(parent);

        Category savedCategory = categoryRepository.save(existingCategory);
        return MapToCategoryResponseDTO(savedCategory);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = getCategoryEntityById(categoryId);

        if (categoryRepository.existsByParent_CategoryId(categoryId)) {
            throw new IllegalStateException("Category has child categories and cannot be deleted");
        }

        if (productRepository.existsByCategory_CategoryId(categoryId)) {
            throw new IllegalStateException("Category has products and cannot be deleted");
        }

        categoryRepository.delete(category);
    }


    private void validateSlugIsAvailable(String slug, Long currentCategoryId) {
        boolean slugAlreadyExists = currentCategoryId == null
                ? categoryRepository.existsBySlug(slug)
                : categoryRepository.existsBySlugAndCategoryIdNot(slug, currentCategoryId);

        if (slugAlreadyExists) {
            throw new IllegalArgumentException("Category with this slug already exists");
        }
    }

    private Category getParentOrNull(Long parentId) {
        if (parentId == null) {
            return null;
        }

        return getCategoryEntityById(parentId);
    }

    private void validateParent(Category category, Category parent) {
        if (parent == null) {
            return;
        }

        Category currentParent = parent;
        while (currentParent != null) {
            if (currentParent.getCategoryId().equals(category.getCategoryId())) {
                throw new IllegalArgumentException("Category cannot be its own parent or descendant");
            }

            currentParent = currentParent.getParent();
        }
    }
    CategoryResponseDTO MapToCategoryResponseDTO(Category category) {
        Long parentId = category.getParent() != null ? category.getParent().getCategoryId() : null;

        return new CategoryResponseDTO(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getSlug(),
                category.getDescription(),
                parentId // 👈 Передаємо нашу безпечну змінну
        );
    }




}
