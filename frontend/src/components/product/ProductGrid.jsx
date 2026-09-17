import React, { useState, useEffect, useMemo } from 'react';
import ProductCard from './ProductCard';
import { productService } from '../../api/productService';

const EMPTY_FILTERS = {};

function ProductGrid({ categoryId, filters, hideTitle = false, wrapperClasses = "max-w-[1600px] mx-auto px-4 sm:px-6 lg:px-8 py-16 lg:py-24" }) {
  const [products, setProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  // Серіалізуємо фільтри в рядок, щоб React міг порівнювати їх по значенню, а не по посиланню
  const filtersKey = JSON.stringify(filters || EMPTY_FILTERS);
  const stableFilters = useMemo(() => filters || EMPTY_FILTERS, [filtersKey]);

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        setIsLoading(true);
        let data;
        if (categoryId) {
          data = await productService.getProductsForCategoryAndChildren(categoryId, stableFilters);
        } else {
          data = await productService.getAllProducts();
        }
        setProducts(data);
      } catch (error) {
        console.error("Failed to load products:", error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchProducts();
  }, [categoryId, filtersKey]);

  return (
    <div className={wrapperClasses}>
      {!hideTitle && (
        <div className="flex justify-between items-end mb-10">
          <h2 className="text-2xl font-light tracking-wide uppercase text-gray-900">
            Щойно надійшли
          </h2>
          <a href="#" className="text-sm font-medium uppercase tracking-widest text-gray-500 hover:text-gray-900 transition-colors border-b border-transparent hover:border-gray-900 pb-1 hidden sm:block">
            Дивитись всі
          </a>
        </div>
      )}
      
      {isLoading ? (
         <div className="flex justify-center items-center h-64">
           <span className="text-sm text-gray-500 uppercase tracking-widest">Завантаження товарів...</span>
         </div>
      ) : products.length === 0 ? (
         <div className="text-center text-gray-500 h-64 flex flex-col items-center justify-center">
           <p className="mb-2">Товарів у цій категорії поки немає.</p>
           <p className="text-xs">(Або не знайдено за поточними фільтрами)</p>
         </div>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-x-4 gap-y-12 sm:gap-x-6 sm:gap-y-16">
          {products.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      )}
      
      {!isLoading && products.length > 0 && !hideTitle && (
        <div className="mt-12 text-center sm:hidden">
          <a href="#" className="inline-block border border-gray-900 text-gray-900 px-8 py-3 text-sm font-medium uppercase tracking-widest hover:bg-gray-900 hover:text-white transition-colors">
            Дивитись всі
          </a>
        </div>
      )}
    </div>
  );
}

export default ProductGrid;
