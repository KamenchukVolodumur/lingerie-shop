import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import ProductGrid from '../components/product/ProductGrid';
import CategorySidebar from '../components/category/CategorySidebar';
import { categoryService } from '../api/categoryService';

function CategoryPage() {
  const { categoryId } = useParams();
  const [categoryName, setCategoryName] = useState('Товари');
  const [activeFilters, setActiveFilters] = useState({});

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

      <div className="max-w-[1600px] mx-auto px-4 sm:px-6 lg:px-8 py-16 lg:py-24 flex flex-col md:flex-row">
        {/* Sidebar */}
        <CategorySidebar 
          categoryId={categoryId} 
          onFiltersChange={(filters) => setActiveFilters(filters)} 
        />
        
        {/* Сітка товарів з переданим categoryId та фільтрами */}
        <div className="flex-1">
          <ProductGrid categoryId={categoryId} filters={activeFilters} hideTitle={true} wrapperClasses="" />
        </div>
      </div>
    </main>
  );
}

export default CategoryPage;
