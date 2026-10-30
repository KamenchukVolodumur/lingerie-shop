import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { cartService } from '../api/cartService';
import { orderService } from '../api/orderService';
import { CreditCard, Truck, CheckCircle } from 'lucide-react';
import NovaPoshtaAutocomplete from '../components/checkout/NovaPoshtaAutocomplete';
import { formatUah } from '../utils/currency';

const CHECKOUT_ATTEMPT_STORAGE_KEY = 'checkout-attempt';

function Checkout() {
  const { cartItems, cartTotal, clearCart } = useCart();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    customerName: '',
    email: '',
    deliveryProvider: 'NOVA_POSHTA',
    deliveryCityRef: '',
    deliveryCityName: '',
    deliveryBranchRef: '',
    deliveryBranchName: '',
    shippingPhone: '',
    paymentMethod: 'CARD' // CARD or CASH
  });
  
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [error, setError] = useState('');

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    
    // Якщо змінюється провайдер доставки, очищаємо попередні дані
    if (name === 'deliveryProvider') {
      setFormData(prev => ({
        ...prev,
        [name]: value,
        deliveryCityRef: '',
        deliveryCityName: '',
        deliveryBranchRef: '',
        deliveryBranchName: ''
      }));
      return;
    }
    
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsSubmitting(true);

    try {
      if (cartItems.length === 0) {
        throw new Error("Ваш кошик порожній");
      }

      const cartFingerprint = cartItems
        .map(item => `${item.id}:${item.productVariantId}:${item.size}:${item.color.name}:${item.quantity}`)
        .sort()
        .join('|');
      const storedAttempt = JSON.parse(sessionStorage.getItem(CHECKOUT_ATTEMPT_STORAGE_KEY) || 'null');
      const checkoutAttempt = storedAttempt?.cartFingerprint === cartFingerprint
        ? storedAttempt
        : {
            cartFingerprint,
            cartId: crypto.randomUUID(),
            idempotencyKey: crypto.randomUUID()
          };

      sessionStorage.setItem(CHECKOUT_ATTEMPT_STORAGE_KEY, JSON.stringify(checkoutAttempt));
      const { cartId, idempotencyKey } = checkoutAttempt;

      // 2. Синхронізуємо локальний кошик з cart-service
      const syncPromises = cartItems.map(item => {
        const variantId = item.productVariantId
          || item.rawVariants?.find(
            v => v.size === item.size && (v.color === item.color.name || (v.color == null && item.color.name === "Стандартний"))
          )?.id;

        if (!variantId) {
          throw new Error(`Не вдалося знайти варіант для товару: ${item.name}`);
        }

        return cartService.addItem(cartId, item.id, variantId, item.quantity);
      });

      await Promise.all(syncPromises);

      // 3. Створюємо замовлення через order-service
      const orderRequest = {
        userId: 1, // Захардкоджено поки немає повної авторизації
        cartId: cartId,
        paymentMethod: formData.paymentMethod,
        customerName: formData.customerName,
        email: formData.email,
        deliveryProvider: formData.deliveryProvider,
        deliveryCity: formData.deliveryProvider === 'NOVA_POSHTA' ? formData.deliveryCityName : formData.deliveryCityRef,
        deliveryBranch: formData.deliveryProvider === 'NOVA_POSHTA' ? formData.deliveryBranchName : formData.deliveryBranchRef,
        shippingPhone: formData.shippingPhone
      };

      const createdOrder = await orderService.createOrder(orderRequest, idempotencyKey);

      if (formData.paymentMethod === 'CARD') {
        if (!createdOrder.paymentUrl) {
          throw new Error("Не вдалося отримати посилання на оплату");
        }

        window.location.assign(createdOrder.paymentUrl);
        return;
      }

      // Успіх!
      setIsSuccess(true);
      clearCart();
      sessionStorage.removeItem(CHECKOUT_ATTEMPT_STORAGE_KEY);
      
      // Повертаємось на головну через 3 секунди
      setTimeout(() => {
        navigate('/');
      }, 3000);

    } catch (err) {
      setError(
        err.response?.data?.message || 
        err.response?.data?.error || 
        (err.response?.data ? JSON.stringify(err.response.data) : null) || 
        err.message || 
        "Сталася помилка при оформленні замовлення"
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isSuccess) {
    return (
      <div className="flex-grow bg-gray-50 flex items-center justify-center p-4 min-h-[60vh]">
        <div className="bg-white p-10 max-w-md w-full text-center shadow-sm">
          <div className="flex justify-center mb-6">
            <CheckCircle size={64} className="text-green-500" strokeWidth={1} />
          </div>
          <h2 className="text-2xl font-light uppercase tracking-widest mb-4">Дякуємо!</h2>
          <p className="text-gray-600 mb-8">Ваше замовлення успішно оформлено. Ми зв'яжемося з вами найближчим часом.</p>
          <button 
            onClick={() => navigate('/')}
            className="border-b border-gray-900 pb-1 text-sm font-medium uppercase tracking-widest hover:text-gray-500 transition-colors"
          >
            Повернутися до покупок
          </button>
        </div>
      </div>
    );
  }

  if (cartItems.length === 0) {
    return (
      <div className="flex-grow bg-white flex flex-col items-center justify-center min-h-[60vh]">
        <h2 className="text-xl font-light uppercase tracking-widest mb-6">Ваш кошик порожній</h2>
        <button 
          onClick={() => navigate('/')}
          className="bg-gray-900 text-white px-8 py-4 text-sm uppercase tracking-widest hover:bg-gray-800 transition-colors"
        >
          Перейти до каталогу
        </button>
      </div>
    );
  }

  return (
    <div className="flex-grow bg-white">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8 py-12 lg:py-20">
        <h1 className="text-2xl font-light tracking-widest uppercase mb-12 text-center lg:text-left">
          Оформлення замовлення
        </h1>

        <div className="lg:grid lg:grid-cols-12 lg:gap-16 xl:gap-24">
          
          {/* Форма */}
          <div className="lg:col-span-7">
            <form onSubmit={handleSubmit} className="space-y-10">
              
              {/* Контакти та Доставка */}
              <section>
                <h2 className="text-sm font-medium tracking-widest uppercase text-gray-900 mb-6 flex items-center">
                  <Truck size={18} className="mr-3 text-gray-400" />
                  Контакти та Доставка
                </h2>
                <div className="grid grid-cols-1 gap-6">
                  <div>
                    <label className="block text-xs uppercase tracking-widest text-gray-500 mb-2">ПІБ</label>
                    <input
                      type="text"
                      name="customerName"
                      value={formData.customerName}
                      onChange={handleInputChange}
                      required
                      className="w-full border-b border-gray-300 py-3 text-gray-900 focus:outline-none focus:border-gray-900 transition-colors bg-transparent placeholder-gray-400"
                      placeholder="Олена Коваленко"
                    />
                  </div>
                  <div>
                    <label className="block text-xs uppercase tracking-widest text-gray-500 mb-2">Email</label>
                    <input
                      type="email"
                      name="email"
                      value={formData.email}
                      onChange={handleInputChange}
                      required
                      className="w-full border-b border-gray-300 py-3 text-gray-900 focus:outline-none focus:border-gray-900 transition-colors bg-transparent placeholder-gray-400"
                      placeholder="example@gmail.com"
                    />
                  </div>
                  <div>
                    <label className="block text-xs uppercase tracking-widest text-gray-500 mb-2">Телефон</label>
                    <input
                      type="tel"
                      name="shippingPhone"
                      value={formData.shippingPhone}
                      onChange={handleInputChange}
                      required
                      className="w-full border-b border-gray-300 py-3 text-gray-900 focus:outline-none focus:border-gray-900 transition-colors bg-transparent placeholder-gray-400"
                      placeholder="+380..."
                    />
                  </div>
                  
                  <div className="pt-4 pb-2">
                    <label className="block text-xs uppercase tracking-widest text-gray-500 mb-3">Спосіб доставки</label>
                    <div className="flex space-x-4">
                      <label className="flex items-center space-x-2 cursor-pointer">
                        <input
                          type="radio"
                          name="deliveryProvider"
                          value="NOVA_POSHTA"
                          checked={formData.deliveryProvider === 'NOVA_POSHTA'}
                          onChange={handleInputChange}
                          className="w-4 h-4 text-gray-900 border-gray-300 focus:ring-gray-900"
                        />
                        <span className="text-sm text-gray-700">Нова Пошта</span>
                      </label>
                      <label className="flex items-center space-x-2 cursor-pointer">
                        <input
                          type="radio"
                          name="deliveryProvider"
                          value="UKRPOSHTA"
                          checked={formData.deliveryProvider === 'UKRPOSHTA'}
                          onChange={handleInputChange}
                          className="w-4 h-4 text-gray-900 border-gray-300 focus:ring-gray-900"
                        />
                        <span className="text-sm text-gray-700">Укрпошта</span>
                      </label>
                    </div>
                  </div>

                  {formData.deliveryProvider === 'NOVA_POSHTA' ? (
                    <>
                      <NovaPoshtaAutocomplete
                        label="Місто доставки (Нова Пошта)"
                        type="city"
                        placeholder="Почніть вводити назву міста..."
                        value={formData.deliveryCityRef}
                        onChange={(ref, name) => {
                          setFormData(prev => ({
                            ...prev,
                            deliveryCityRef: ref,
                            deliveryCityName: name,
                            deliveryBranchRef: '',
                            deliveryBranchName: ''
                          }));
                        }}
                      />

                      <NovaPoshtaAutocomplete
                        label="Відділення"
                        type="branch"
                        cityRef={formData.deliveryCityRef}
                        placeholder={formData.deliveryCityRef ? "Оберіть відділення..." : "Спочатку оберіть місто"}
                        value={formData.deliveryBranchRef}
                        onChange={(ref, name) => {
                          setFormData(prev => ({
                            ...prev,
                            deliveryBranchRef: ref,
                            deliveryBranchName: name
                          }));
                        }}
                      />
                    </>
                  ) : (
                    <>
                      <div>
                        <label className="block text-xs uppercase tracking-widest text-gray-500 mb-2">Місто та область</label>
                        <input
                          type="text"
                          name="deliveryCityRef"
                          value={formData.deliveryCityRef}
                          onChange={handleInputChange}
                          required
                          className="w-full border-b border-gray-300 py-3 text-gray-900 focus:outline-none focus:border-gray-900 transition-colors bg-transparent placeholder-gray-400"
                          placeholder="Наприклад: м. Київ, Київська обл."
                        />
                      </div>
                      <div>
                        <label className="block text-xs uppercase tracking-widest text-gray-500 mb-2">Вулиця, будинок, номер відділення або індекс</label>
                        <input
                          type="text"
                          name="deliveryBranchRef"
                          value={formData.deliveryBranchRef}
                          onChange={handleInputChange}
                          required
                          className="w-full border-b border-gray-300 py-3 text-gray-900 focus:outline-none focus:border-gray-900 transition-colors bg-transparent placeholder-gray-400"
                          placeholder="Наприклад: Відділення 01001, вул. Хрещатик 22"
                        />
                      </div>
                    </>
                  )}
                </div>
              </section>

              {/* Оплата */}
              <section>
                <h2 className="text-sm font-medium tracking-widest uppercase text-gray-900 mb-6 flex items-center">
                  <CreditCard size={18} className="mr-3 text-gray-400" />
                  Оплата
                </h2>
                <div className="space-y-4">
                  <label className={`flex items-center p-4 border transition-colors cursor-pointer ${formData.paymentMethod === 'CARD' ? 'border-gray-900 bg-gray-50' : 'border-gray-200 hover:border-gray-300'}`}>
                    <input
                      type="radio"
                      name="paymentMethod"
                      value="CARD"
                      checked={formData.paymentMethod === 'CARD'}
                      onChange={handleInputChange}
                      className="accent-gray-900 w-4 h-4"
                    />
                    <span className="ml-4 text-sm font-medium">Оплата карткою</span>
                  </label>
                  <label className={`flex items-center p-4 border transition-colors cursor-pointer ${formData.paymentMethod === 'CASH_ON_DELIVERY' ? 'border-gray-900 bg-gray-50' : 'border-gray-200 hover:border-gray-300'}`}>
                    <input
                      type="radio"
                      name="paymentMethod"
                      value="CASH_ON_DELIVERY"
                      checked={formData.paymentMethod === 'CASH_ON_DELIVERY'}
                      onChange={handleInputChange}
                      className="accent-gray-900 w-4 h-4"
                    />
                    <span className="ml-4 text-sm font-medium">Накладений платіж</span>
                  </label>
                </div>
              </section>

              {error && (
                <div className="bg-red-50 text-red-600 p-4 text-sm">
                  {error}
                </div>
              )}

              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full bg-gray-900 text-white py-5 text-sm font-medium uppercase tracking-widest hover:bg-gray-800 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
              >
                {isSubmitting ? 'Обробка...' : 'Підтвердити замовлення'}
              </button>
            </form>
          </div>

          {/* Підсумок */}
          <div className="lg:col-span-5 mt-12 lg:mt-0">
            <div className="bg-gray-50 p-8 sticky top-24">
              <h2 className="text-sm font-medium tracking-widest uppercase text-gray-900 mb-8">
                Ваше замовлення
              </h2>
              
              <div className="space-y-6 max-h-[40vh] overflow-y-auto pr-2 custom-scrollbar">
                {cartItems.map((item, index) => (
                  <div key={`${item.id}-${item.size}-${item.color.name}-${index}`} className="flex space-x-4">
                    <div className="w-20 h-24 bg-gray-200 flex-shrink-0">
                      <img src={item.color.image} alt={item.name} className="w-full h-full object-cover" />
                    </div>
                    <div className="flex-1 flex flex-col justify-between">
                      <div>
                        <h3 className="text-sm font-medium text-gray-900 line-clamp-2">{item.name}</h3>
                        <p className="text-xs text-gray-500 mt-1 uppercase tracking-wider">
                          {item.color.name} / {item.size}
                        </p>
                      </div>
                      <div className="flex justify-between items-end mt-2">
                        <p className="text-xs text-gray-500">Кількість: {item.quantity}</p>
                        <p className="text-sm font-medium text-gray-900">{formatUah(item.price)}</p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              <div className="mt-8 pt-8 border-t border-gray-200 space-y-4">
                <div className="flex justify-between text-sm text-gray-500">
                  <span>Доставка</span>
                  <span>За тарифами перевізника</span>
                </div>
                <div className="flex justify-between text-lg font-medium text-gray-900 uppercase">
                  <span>До сплати</span>
                  <span>{formatUah(cartTotal)}</span>
                </div>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}

export default Checkout;
