import React from 'react';
import { X, ShoppingBag, Minus, Plus } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useCart } from '../../context/CartContext';

function CartDrawer() {
  const { isCartOpen, setIsCartOpen, cartItems, cartTotal, removeFromCart, updateQuantity, cartCount } = useCart();

  if (!isCartOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      <div 
        className="fixed inset-0 bg-black/20 backdrop-blur-sm transition-opacity" 
        onClick={() => setIsCartOpen(false)} 
      />
      <div className="relative w-full max-w-md bg-white shadow-2xl flex flex-col h-full animate-in slide-in-from-right duration-300">
        <div className="flex items-center justify-between p-6 border-b border-gray-100">
          <h2 className="text-lg font-medium tracking-wide uppercase">Кошик ({cartCount})</h2>
          <button onClick={() => setIsCartOpen(false)} className="text-gray-400 hover:text-gray-500 transition-colors">
            <X size={24} strokeWidth={1.5} />
          </button>
        </div>
        
        <div className="flex-1 overflow-y-auto p-6 no-scrollbar">
          {cartItems.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full text-center text-gray-500">
              <ShoppingBag size={48} strokeWidth={1} className="mb-4 text-gray-300" />
              <p>Ваш кошик порожній</p>
              <button 
                onClick={() => setIsCartOpen(false)}
                className="mt-6 text-sm uppercase tracking-widest border-b border-gray-900 text-gray-900 pb-1 hover:text-gray-500 hover:border-gray-500 transition-colors"
              >
                Продовжити покупки
              </button>
            </div>
          ) : (
            <ul className="space-y-8">
              {cartItems.map((item, index) => (
                <li key={index} className="flex space-x-4">
                  <div className="h-32 w-24 flex-shrink-0 overflow-hidden bg-gray-100">
                    <img 
                      src={item.color?.image || item.image} // Fallback just in case
                      alt={item.name} 
                      className="h-full w-full object-cover object-center" 
                    />
                  </div>
                  <div className="flex flex-1 flex-col justify-between">
                    <div>
                      <div className="flex justify-between">
                        <h3 className="text-sm font-medium text-gray-900 pr-4">{item.name}</h3>
                        <button 
                          type="button" 
                          onClick={() => removeFromCart(index)}
                          className="text-gray-400 hover:text-gray-900 transition-colors"
                        >
                          <X size={18} strokeWidth={1.5} />
                        </button>
                      </div>
                      <p className="mt-1 text-sm text-gray-500">
                        Розмір: {item.size || 'M'} <br/>
                        {item.color && <span>Колір: {item.color.name}</span>}
                      </p>
                    </div>
                    
                    <div className="flex items-center justify-between mt-4">
                      <div className="flex items-center border border-gray-200">
                        <button 
                          onClick={() => updateQuantity(index, item.quantity - 1)}
                          className="p-1.5 text-gray-500 hover:bg-gray-50 transition-colors"
                        >
                          <Minus size={14} />
                        </button>
                        <span className="text-xs font-medium w-8 text-center select-none">{item.quantity}</span>
                        <button 
                          onClick={() => updateQuantity(index, item.quantity + 1)}
                          className="p-1.5 text-gray-500 hover:bg-gray-50 transition-colors"
                        >
                          <Plus size={14} />
                        </button>
                      </div>
                      <p className="text-sm font-medium text-gray-900">{item.price}</p>
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>

        {cartItems.length > 0 && (
          <div className="border-t border-gray-100 p-6 bg-gray-50">
                <div className="flex justify-between items-center mb-6">
                  <span className="text-sm uppercase tracking-widest text-gray-500">Загалом</span>
                  <span className="text-lg font-medium text-gray-900">${cartTotal}</span>
                </div>
                <Link
                  to="/checkout"
                  onClick={() => setIsCartOpen(false)}
                  className="block w-full bg-gray-900 text-white text-center py-4 text-sm uppercase tracking-widest hover:bg-gray-800 transition-colors"
                >
                  Оформити замовлення
                </Link>
              </div>
        )}
      </div>
    </div>
  );
}

export default CartDrawer;
