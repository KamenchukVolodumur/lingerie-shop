import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import { CheckCircle } from 'lucide-react';
import { useCart } from '../context/CartContext';

function OrderSuccess() {
  const { clearCart } = useCart();

  useEffect(() => {
    clearCart();
  }, [clearCart]);

  return (
    <div className="flex-grow bg-gray-50 flex items-center justify-center p-4 min-h-[60vh]">
      <div className="bg-white p-10 max-w-md w-full text-center shadow-sm">
        <div className="flex justify-center mb-6">
          <CheckCircle size={64} className="text-green-500" strokeWidth={1} />
        </div>
        <h1 className="text-2xl font-light uppercase tracking-widest mb-4">Дякуємо за замовлення!</h1>
        <p className="text-gray-600 mb-8">
          Кошти зарезервовано. Очікуйте на підтвердження наявності менеджером.
        </p>
        <Link
          to="/"
          className="border-b border-gray-900 pb-1 text-sm font-medium uppercase tracking-widest hover:text-gray-500 transition-colors"
        >
          Повернутися до покупок
        </Link>
      </div>
    </div>
  );
}

export default OrderSuccess;
