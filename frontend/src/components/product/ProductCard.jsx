import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useCart } from '../../context/CartContext';

function ProductCard({ product }) {
  const [isHovered, setIsHovered] = useState(false);
  const { addToCart } = useCart();
  
  const defaultColor = product.colors[0];
  const isOutOfStock = product.inStock === false;

  const handleQuickAdd = (e) => {
    e.preventDefault(); 
    const defaultSize = defaultColor?.availableSizes?.[0];
    if (!isOutOfStock && defaultColor && defaultSize) {
      addToCart({
        ...product,
        size: defaultSize.size,
        color: defaultColor,
        productVariantId: defaultSize.variantId
      }, 1);
    }
  };

  return (
    <div 
      className="group relative flex flex-col"
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <Link to={`/product/${product.id}`} className="relative w-full aspect-[3/4] bg-gray-100 overflow-hidden block">
        <img
          src={isHovered ? defaultColor.hoverImage : defaultColor.image}
          alt={product.name}
          className={`w-full h-full object-cover object-center transition-all duration-700 ease-in-out ${isOutOfStock ? 'opacity-70 grayscale' : ''}`}
        />
        
        {/* Out of stock badge */}
        {isOutOfStock && (
          <div className="absolute top-4 left-4">
            <span className="bg-white/90 text-gray-900 text-[10px] uppercase font-semibold tracking-widest px-2 py-1">
              Немає в наявності
            </span>
          </div>
        )}

        {/* Quick add button on hover (desktop) */}
        {!isOutOfStock && (
          <div className="absolute bottom-0 left-0 right-0 p-4 translate-y-full group-hover:translate-y-0 transition-transform duration-300 ease-in-out hidden lg:block">
            <button 
              onClick={handleQuickAdd}
              className="w-full bg-white/90 backdrop-blur text-gray-900 py-3 text-sm tracking-wider uppercase font-medium hover:bg-white transition-colors shadow-sm"
            >
              Додати в кошик
            </button>
          </div>
        )}
      </Link>
      <div className="mt-4 flex justify-between">
        <div>
          <Link to={`/product/${product.id}`} className="text-sm text-gray-900 font-light hover:underline">
            {product.name}
          </Link>
          <p className="text-xs text-gray-500 mt-1">{product.colors.length} {product.colors.length === 1 ? 'Колір' : 'Кольори'}</p>
        </div>
        <p className={`text-sm font-medium ${isOutOfStock ? 'text-gray-400 line-through' : 'text-gray-900'}`}>{product.price}</p>
      </div>
      
      {/* Mobile add to cart */}
      {!isOutOfStock ? (
        <button 
          onClick={handleQuickAdd}
          className="mt-2 text-xs uppercase tracking-wider text-gray-500 text-left hover:text-gray-900 lg:hidden transition-colors"
        >
          + Додати
        </button>
      ) : (
        <span className="mt-2 text-xs uppercase tracking-wider text-gray-400 text-left lg:hidden">
          Немає в наявності
        </span>
      )}
    </div>
  );
}

export default ProductCard;
