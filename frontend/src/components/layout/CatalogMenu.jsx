import React, { useState, useEffect } from 'react';
import { X, ChevronRight, ChevronLeft } from 'lucide-react';
import { Link } from 'react-router-dom';
import { categoryService } from '../../api/categoryService';

function CatalogMenu({ isMenuOpen, setIsMenuOpen }) {
  const [categories, setCategories] = useState([]);
  const [activeParent, setActiveParent] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const tree = await categoryService.getCategoryTree();
        setCategories(tree);
      } catch (error) {
        console.error("Failed to load categories:", error);
      } finally {
        setIsLoading(false);
      }
    };
    if (isMenuOpen && categories.length === 0) {
      fetchCategories();
    }
  }, [isMenuOpen, categories.length]);

  if (!isMenuOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex">
      {/* Overlay */}
      <div 
        className="fixed inset-0 bg-black/20 backdrop-blur-sm transition-opacity" 
        onClick={() => setIsMenuOpen(false)} 
      />
      
      {/* Menu Panel */}
      <div className="relative w-full max-w-sm bg-white shadow-2xl flex flex-col h-full animate-in slide-in-from-left duration-300">
        <div className="flex items-center justify-between p-6 border-b border-gray-100">
          <div className="flex items-center">
             {activeParent && (
               <button 
                 onClick={() => setActiveParent(null)} 
                 className="mr-3 text-gray-500 hover:text-gray-900 transition-colors"
               >
                 <ChevronLeft size={20} />
               </button>
             )}
             <h2 className="text-lg font-medium tracking-wide uppercase">
               {activeParent ? activeParent.categoryName : 'Каталог'}
             </h2>
          </div>
          <button onClick={() => setIsMenuOpen(false)} className="text-gray-400 hover:text-gray-500 transition-colors">
            <X size={24} strokeWidth={1.5} />
          </button>
        </div>
        
        <div className="flex-1 overflow-y-auto p-6">
          {isLoading ? (
            <div className="flex justify-center items-center h-32">
              <span className="text-sm text-gray-500 uppercase tracking-widest">Завантаження...</span>
            </div>
          ) : categories.length === 0 ? (
            <div className="flex justify-center items-center h-32">
              <span className="text-sm text-gray-500 text-center">Категорії відсутні або бекенд недоступний.</span>
            </div>
          ) : (
            <ul className="space-y-6">
              {!activeParent ? (
                // Головні категорії
                categories.map(cat => (
                  <li key={cat.categoryId}>
                    {cat.children && cat.children.length > 0 ? (
                      <button 
                        onClick={() => setActiveParent(cat)}
                        className="w-full flex justify-between items-center text-left text-lg font-light text-gray-900 hover:text-gray-500 transition-colors uppercase tracking-wide"
                      >
                        {cat.categoryName}
                        <ChevronRight size={18} className="text-gray-400" />
                      </button>
                    ) : (
                      <Link 
                        to={`/category/${cat.categoryId}`} 
                        onClick={() => setIsMenuOpen(false)}
                        className="block text-lg font-light text-gray-900 hover:text-gray-500 transition-colors uppercase tracking-wide"
                      >
                        {cat.categoryName}
                      </Link>
                    )}
                  </li>
                ))
              ) : (
                // Підкатегорії
                <>
                  <li>
                    <Link 
                      to={`/category/${activeParent.categoryId}`} 
                      onClick={() => setIsMenuOpen(false)}
                      className="block text-base font-medium text-gray-900 hover:text-gray-500 transition-colors mb-4"
                    >
                      Дивитись всі в '{activeParent.categoryName}'
                    </Link>
                  </li>
                  {activeParent.children.map(child => (
                    <li key={child.categoryId}>
                      <Link 
                        to={`/category/${child.categoryId}`} 
                        onClick={() => setIsMenuOpen(false)}
                        className="block text-lg font-light text-gray-600 hover:text-gray-900 transition-colors uppercase tracking-wide"
                      >
                        {child.categoryName}
                      </Link>
                    </li>
                  ))}
                </>
              )}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}

export default CatalogMenu;
