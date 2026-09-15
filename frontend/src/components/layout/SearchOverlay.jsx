import React, { useState, useEffect, useRef } from 'react';
import { Search, X, Loader2 } from 'lucide-react';
import { productService } from '../../api/productService';
import ProductCard from '../product/ProductCard';

function SearchOverlay({ isOpen, onClose }) {
  const [query, setQuery] = useState('');
  const [products, setProducts] = useState([]);
  const [filteredProducts, setFilteredProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const inputRef = useRef(null);

  useEffect(() => {
    if (isOpen) {
      // Focus input when opened
      setTimeout(() => {
        if (inputRef.current) inputRef.current.focus();
      }, 100);
      
      // Load products if not loaded yet
      if (products.length === 0) {
        setIsLoading(true);
        productService.getAllProducts(0, 500)
          .then(data => {
            setProducts(data || []);
            setIsLoading(false);
          })
          .catch(err => {
            console.error("Error loading products for search", err);
            setIsLoading(false);
          });
      }
    } else {
      // Reset when closed
      setQuery('');
      setFilteredProducts([]);
    }
  }, [isOpen, products.length]);

  useEffect(() => {
    if (!query.trim()) {
      setFilteredProducts([]);
      return;
    }

    const lowerQuery = query.toLowerCase();
    const results = products.filter(p => {
      const nameMatch = p?.name?.toLowerCase()?.includes(lowerQuery) || false;
      const descMatch = p?.description?.toLowerCase()?.includes(lowerQuery) || false;
      return nameMatch || descMatch;
    });
    setFilteredProducts(results);
  }, [query, products]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 bg-white/95 backdrop-blur-md overflow-y-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        
        {/* Header / Search Input */}
        <div className="flex items-center justify-between border-b border-gray-200 pb-6 mb-8">
          <div className="flex items-center flex-1">
            <Search size={28} className="text-gray-400 mr-4" />
            <input
              ref={inputRef}
              type="text"
              placeholder="Що ви шукаєте?"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              className="w-full text-3xl font-light outline-none bg-transparent placeholder-gray-300 text-gray-900"
            />
          </div>
          <button 
            onClick={onClose}
            className="p-2 text-gray-400 hover:text-gray-900 transition-colors ml-4"
          >
            <X size={32} strokeWidth={1} />
          </button>
        </div>

        {/* Results Area */}
        <div className="min-h-[50vh]">
          {isLoading ? (
            <div className="flex flex-col items-center justify-center h-40 text-gray-400">
              <Loader2 className="animate-spin mb-2" size={32} />
              <p className="text-sm tracking-widest uppercase">Завантаження каталогу...</p>
            </div>
          ) : query.trim() === '' ? (
            <div className="text-center text-gray-400 mt-20">
              <p className="text-lg font-light">Введіть назву товару або ключове слово для пошуку</p>
            </div>
          ) : filteredProducts.length > 0 ? (
            <div>
              <p className="text-sm text-gray-500 uppercase tracking-widest mb-6">
                Знайдено {filteredProducts.length} товарів
              </p>
              <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-x-6 gap-y-10">
                {filteredProducts.map(product => (
                  <div key={product.id} onClick={onClose}>
                    <ProductCard product={product} />
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="text-center text-gray-400 mt-20">
              <p className="text-lg font-light mb-2">На жаль, за вашим запитом нічого не знайдено.</p>
              <p className="text-sm">Спробуйте змінити слова пошуку або перевірте правильність написання.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default SearchOverlay;
