import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { Minus, Plus } from 'lucide-react';
import { productService } from '../api/productService';
import { translateKey, translateValue } from '../utils/translations';

function ProductDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToCart } = useCart();
  
  const [product, setProduct] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  
  const [selectedSize, setSelectedSize] = useState('');
  const [selectedColor, setSelectedColor] = useState(null);
  const [selectedVariantId, setSelectedVariantId] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [isAdded, setIsAdded] = useState(false);

  useEffect(() => {
    const loadProduct = async () => {
      try {
        setIsLoading(true);
        const data = await productService.getProductById(id);
        setProduct(data);
        
        // Встановлюємо дефолтний колір і розмір
        if (data && data.colors && data.colors.length > 0) {
          const defaultColor = data.colors[0];
          setSelectedColor(defaultColor);
            
          if (defaultColor.availableSizes && defaultColor.availableSizes.length > 0) {
            setSelectedSize(defaultColor.availableSizes[0].size);
            setSelectedVariantId(defaultColor.availableSizes[0].variantId);
          }
        }
      } catch (error) {
        console.error("Failed to fetch product details:", error);
      } finally {
        setIsLoading(false);
      }
    };
    loadProduct();
  }, [id]);

  if (isLoading) {
    return (
      <div className="flex-grow bg-white flex items-center justify-center min-h-[60vh]">
        <span className="text-sm text-gray-500 uppercase tracking-widest">Завантаження...</span>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="flex-grow bg-white flex flex-col items-center justify-center min-h-[60vh] space-y-4">
        <h2 className="text-2xl font-light">Товар не знайдено</h2>
        <button onClick={() => navigate('/')} className="text-sm underline hover:text-gray-500">
          Повернутися на головну
        </button>
      </div>
    );
  }

  const isOutOfStock = product.inStock === false;
  // Sizes available for the currently selected color
  const availableSizesForColor = selectedColor?.availableSizes || [];

  const handleAddToCart = () => {
    if (!selectedSize || !selectedColor || !selectedVariantId) return;
    
    addToCart({
      ...product,
      size: selectedSize,
      color: selectedColor,
      productVariantId: selectedVariantId
    }, quantity);
    setIsAdded(true);
    setTimeout(() => setIsAdded(false), 2000);
  };

  const handleDecrease = () => {
    if (quantity > 1) setQuantity(quantity - 1);
  };

  const handleIncrease = () => {
    setQuantity(quantity + 1);
  };

  return (
    <div className="flex-grow bg-white">
      <div className="max-w-[1600px] mx-auto px-4 sm:px-6 lg:px-8 py-12 lg:py-16">
        <div className="lg:grid lg:grid-cols-2 lg:gap-x-16 xl:gap-x-24">
          
          <div className="flex flex-col space-y-4">
            <div className="aspect-[3/4] bg-gray-100 overflow-hidden w-full">
              <img
                key={selectedColor?.image}
                src={selectedColor?.image}
                alt={`${product.name} - ${selectedColor?.name}`}
                className={`w-full h-full object-cover object-center animate-in fade-in duration-500 ${isOutOfStock ? 'opacity-70 grayscale' : ''}`}
              />
            </div>
            <div className="aspect-[3/4] bg-gray-100 overflow-hidden w-full">
              <img
                key={selectedColor?.hoverImage}
                src={selectedColor?.hoverImage}
                alt={`${product.name} - Деталі`}
                className={`w-full h-full object-cover object-center animate-in fade-in duration-500 ${isOutOfStock ? 'opacity-70 grayscale' : ''}`}
              />
            </div>
          </div>

          <div className="mt-12 lg:mt-0 lg:sticky lg:top-32 lg:self-start lg:h-fit">
            <h1 className="text-2xl sm:text-3xl font-light text-gray-900 tracking-wide uppercase">
              {product.name}
            </h1>
            <p className={`mt-4 text-xl font-medium ${isOutOfStock ? 'text-gray-400 line-through' : 'text-gray-900'}`}>
              {product.price}
            </p>

            {/* Colors */}
            <div className="mt-8 border-t border-gray-100 pt-8">
              <h3 className="text-sm uppercase tracking-widest font-medium text-gray-900 mb-4">Колір: {selectedColor?.name}</h3>
              <div className="flex space-x-3">
                {product.colors.map((color) => (
                  <button
                    key={color.name}
                    onClick={() => {
                      setSelectedColor(color);
                      // Auto-select first available size for new color
                      if (color.availableSizes && color.availableSizes.length > 0) {
                         setSelectedSize(color.availableSizes[0].size);
                        setSelectedVariantId(color.availableSizes[0].variantId);
                      } else {
                        setSelectedSize('');
                        setSelectedVariantId(null);
                      }
                    }}
                    className={`relative w-8 h-8 rounded-full border flex items-center justify-center transition-all ${
                      selectedColor?.name === color.name ? 'border-gray-900' : 'border-gray-200 hover:border-gray-400'
                    }`}
                    title={color.name}
                  >
                    <span 
                      className="w-6 h-6 rounded-full block border border-black/10" 
                      style={{ backgroundColor: color.hex }}
                    />
                  </button>
                ))}
              </div>
            </div>

            {/* Sizes */}
            <div className="mt-8 border-t border-gray-100 pt-8">
              <h3 className="text-sm uppercase tracking-widest font-medium text-gray-900 flex justify-between">
                Розмір
                {selectedColor?.availableSizes?.length === 0 && (
                  <span className="text-red-500 text-xs">Немає розмірів для цього кольору</span>
                )}
              </h3>
              <div className="mt-4 grid grid-cols-5 gap-2 sm:gap-4">
                {availableSizesForColor.map((sizeOption) => {
                 return (
                    <button
                      key={`${selectedColor?.name}-${sizeOption.variantId}`}
                      onClick={() => {
                        setSelectedSize(sizeOption.size);
                        setSelectedVariantId(sizeOption.variantId);
                      }}
                      className={`py-3 px-1 border text-sm font-medium transition-colors ${
                        selectedSize === sizeOption.size
                          ? 'border-gray-900 text-gray-900 bg-gray-50'
                          : 'border-gray-200 text-gray-600 hover:border-gray-400'
                      }`}
                    >
                      {sizeOption.size}
                    </button>
                  );
                })}
              </div>
            </div>

            <div className="mt-8">
              <h3 className="text-sm uppercase tracking-widest font-medium text-gray-900 mb-4">Кількість</h3>
              <div className="flex items-center border border-gray-200 w-fit">
                <button onClick={handleDecrease} disabled={isOutOfStock} className="p-3 text-gray-500 hover:bg-gray-50 transition-colors disabled:opacity-50">
                  <Minus size={16} />
                </button>
                <span className="px-6 py-2 text-sm font-medium text-gray-900 w-12 text-center select-none">
                  {quantity}
                </span>
                <button onClick={handleIncrease} disabled={isOutOfStock} className="p-3 text-gray-500 hover:bg-gray-50 transition-colors disabled:opacity-50">
                  <Plus size={16} />
                </button>
              </div>
            </div>

            <div className="mt-8">
              <button
                onClick={handleAddToCart}
                disabled={isOutOfStock || !selectedSize}
                className={`w-full py-5 px-8 flex items-center justify-center text-sm font-medium uppercase tracking-widest transition-all duration-300 ${
                  isOutOfStock || !selectedSize
                    ? 'bg-gray-200 text-gray-500 cursor-not-allowed'
                    : isAdded 
                      ? 'bg-green-600 text-white' 
                      : 'bg-gray-900 text-white hover:bg-gray-800'
                }`}
              >
                {isOutOfStock ? 'Немає в наявності' : !selectedSize ? 'Виберіть розмір' : isAdded ? 'Додано' : 'Додати в кошик'}
              </button>
            </div>

            <div className="mt-12 border-t border-gray-100 pt-8 space-y-6">
              <div>
                <h3 className="text-sm font-medium text-gray-900 uppercase tracking-widest mb-3">Опис</h3>
                <p className="text-gray-600 text-sm leading-relaxed whitespace-pre-line">
                  {product.description || "Опис товару з бекенду."}
                </p>
              </div>

              {/* Dynamic Attributes */}
              {product.attributes && Object.keys(product.attributes).length > 0 && (
                <div className="border-t border-gray-100 pt-6">
                  <h3 className="text-sm font-medium text-gray-900 uppercase tracking-widest mb-4">Характеристики</h3>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-y-4 gap-x-6">
                    {Object.entries(product.attributes).map(([key, value]) => (
                      <div key={key} className="flex flex-col border-b border-gray-50 pb-2">
                        <span className="text-xs text-gray-500 uppercase tracking-wide">{translateKey(key)}</span>
                        <span className="text-sm text-gray-900 mt-1">{translateValue(value)}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}

export default ProductDetails;
