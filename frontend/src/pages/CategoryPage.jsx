import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import ProductGrid from '../components/product/ProductGrid';
import { categoryService } from '../api/categoryService';

function CategoryPage() {
  const { categoryId } = useParams();
  const [categoryName, setCategoryName] = useState('Товари');

  useEffect(() => {
    // Спробуємо знайти назву категорії, щоб вивести її в заголовок
    const fetchCategoryName = async () => {
      try {
        const categories = await categoryService.getAllCategories();
        const category = categories.find(c => c.categoryId === parseInt(categoryId));
        if (category) {
          setCategoryName(category.categoryName);
        }
      } catch (error) {
        console.error("Failed to fetch categories for title", error);
      }
    };
    fetchCategoryName();
  }, [categoryId]);

  return (
    <main className="flex-grow">
      {/* Шапка категорії */}
      <div className="h-[30vh] w-full bg-gray-50 flex items-center justify-center relative border-b border-gray-100">
        <div className="relative z-10 text-center">
          <h1 className="text-3xl md:text-4xl font-light tracking-widest text-gray-900 uppercase">
            {categoryName}
          </h1>
        </div>
      </div>

      {/* Сітка товарів з переданим categoryId */}
      <ProductGrid categoryId={categoryId} hideTitle={true} />
    </main>
  );
}

export default CategoryPage;
