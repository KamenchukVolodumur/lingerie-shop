import { Link } from 'react-router-dom';
import { Clock3, PackageCheck, UserRound } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const activeOrders = [
  { number: 'ORD-DEMO-001', date: 'Сьогодні', status: 'Очікує підтвердження', total: '1 250 грн' },
  { number: 'ORD-DEMO-002', date: '15 вересня 2026', status: 'Передано до доставки', total: '890 грн' }
];

const orderHistory = [
  { number: 'ORD-DEMO-003', date: '2 вересня 2026', status: 'Виконано', total: '1 490 грн' },
  { number: 'ORD-DEMO-004', date: '18 серпня 2026', status: 'Виконано', total: '760 грн' }
];

function OrderList({ orders, active }) {
  return (
    <div className="border border-gray-200 bg-white">
      {orders.map((order) => (
        <div key={order.number} className="p-5 sm:p-6 border-b border-gray-100 last:border-b-0">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <p className="text-sm font-medium tracking-wide text-gray-900">{order.number}</p>
              <p className="mt-1 text-sm text-gray-500">{order.date}</p>
            </div>
            <div className="sm:text-right">
              <p className={`text-xs uppercase tracking-widest ${active ? 'text-amber-600' : 'text-green-600'}`}>
                {order.status}
              </p>
              <p className="mt-1 text-sm font-medium text-gray-900">{order.total}</p>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

function Account() {
  const { isAuthenticated, user } = useAuth();
  const customerName = isAuthenticated ? user?.name : 'Клієнт';
  const customerEmail = isAuthenticated ? user?.email : 'email буде показано після входу';

  return (
    <main className="flex-grow bg-gray-50">
      <div className="max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8 py-12 lg:py-20">
        <div className="flex flex-col gap-6 border-b border-gray-200 pb-10 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="text-xs uppercase tracking-[0.2em] text-gray-500">Особистий кабінет</p>
            <h1 className="mt-3 text-3xl font-light uppercase tracking-widest text-gray-900">
              Вітаємо, {customerName}
            </h1>
          </div>
          {!isAuthenticated && (
            <Link to="/login" className="border-b border-gray-900 pb-1 text-sm font-medium uppercase tracking-widest hover:text-gray-500 transition-colors">
              Увійти
            </Link>
          )}
        </div>

        <div className="mt-10 grid gap-8 lg:grid-cols-3">
          <section className="border border-gray-200 bg-white p-6 lg:col-span-1">
            <UserRound size={24} strokeWidth={1.25} className="text-gray-700" />
            <h2 className="mt-5 text-sm font-medium uppercase tracking-widest text-gray-900">Мої дані</h2>
            <p className="mt-5 text-sm font-medium text-gray-900">{customerName}</p>
            <p className="mt-1 text-sm text-gray-500">{customerEmail}</p>
            <button type="button" disabled className="mt-8 text-xs uppercase tracking-widest text-gray-400">
              Редагування скоро буде доступне
            </button>
          </section>

          <section className="lg:col-span-2">
            <div className="mb-5 flex items-center gap-3">
              <Clock3 size={20} strokeWidth={1.25} />
              <h2 className="text-sm font-medium uppercase tracking-widest text-gray-900">Актуальні замовлення</h2>
            </div>
            <OrderList orders={activeOrders} active />
          </section>
        </div>

        <section className="mt-14">
          <div className="mb-5 flex items-center gap-3">
            <PackageCheck size={20} strokeWidth={1.25} />
            <h2 className="text-sm font-medium uppercase tracking-widest text-gray-900">Історія замовлень</h2>
          </div>
          <OrderList orders={orderHistory} />
          <p className="mt-4 text-xs text-gray-400">
            Наразі це демонстраційні дані. Реальні замовлення буде підключено до order-service після інтеграції авторизації.
          </p>
        </section>
      </div>
    </main>
  );
}

export default Account;
