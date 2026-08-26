import React, { useState, useEffect } from 'react';
import { adminOrderService } from '../../api/adminOrderService';
import { Search, MoreVertical, CheckCircle, Truck, Package, XCircle } from 'lucide-react';

function AdminOrders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const data = await adminOrderService.getAllOrders(0, 50);
      setOrders(data.content || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleStatusUpdate = async (id, action) => {
    try {
      switch (action) {
        case 'CONFIRM':
          await adminOrderService.confirmOrder(id);
          break;
        case 'PAY':
          await adminOrderService.payOrder(id);
          break;
        case 'SHIP':
          await adminOrderService.shipOrder(id);
          break;
        case 'COMPLETE':
          await adminOrderService.completeOrder(id);
          break;
        case 'CANCEL':
          await adminOrderService.cancelOrder(id);
          break;
        default:
          break;
      }
      fetchOrders(); // Refresh after action
    } catch (err) {
      alert(`Помилка: ${err.response?.data?.message || err.message}`);
    }
  };

  const getStatusBadge = (status) => {
    const styles = {
      NEW: 'bg-blue-100 text-blue-800',
      CONFIRMED: 'bg-purple-100 text-purple-800',
      SHIPPED: 'bg-orange-100 text-orange-800',
      COMPLETED: 'bg-green-100 text-green-800',
      CANCELLED: 'bg-red-100 text-red-800'
    };
    
    const labels = {
      NEW: 'Нове',
      CONFIRMED: 'Підтверджено',
      SHIPPED: 'Відправлено',
      COMPLETED: 'Виконано',
      CANCELLED: 'Скасовано'
    };

    return (
      <span className={`px-3 py-1 rounded-full text-xs font-medium tracking-wider ${styles[status]}`}>
        {labels[status] || status}
      </span>
    );
  };

  if (loading) return <div className="p-8">Завантаження замовлень...</div>;
  if (error) return <div className="p-8 text-red-500">Помилка: {error}</div>;

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-2xl font-light tracking-widest uppercase">Замовлення</h1>
        <div className="relative">
          <input 
            type="text" 
            placeholder="Пошук (номер)"
            className="pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:border-gray-900"
          />
          <Search size={18} className="absolute left-3 top-2.5 text-gray-400" />
        </div>
      </div>

      <div className="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-gray-50 border-b border-gray-200">
              <th className="p-4 text-xs font-medium text-gray-500 uppercase tracking-widest">Номер</th>
              <th className="p-4 text-xs font-medium text-gray-500 uppercase tracking-widest">Дата</th>
              <th className="p-4 text-xs font-medium text-gray-500 uppercase tracking-widest">Клієнт</th>
              <th className="p-4 text-xs font-medium text-gray-500 uppercase tracking-widest">Сума</th>
              <th className="p-4 text-xs font-medium text-gray-500 uppercase tracking-widest">Статус</th>
              <th className="p-4 text-xs font-medium text-gray-500 uppercase tracking-widest text-right">Дії</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => (
              <tr key={order.id} className="border-b border-gray-100 hover:bg-gray-50 transition-colors">
                <td className="p-4 text-sm font-medium text-gray-900">
                  {order.orderNumber}
                </td>
                <td className="p-4 text-sm text-gray-500">
                  {new Date(order.createdAt).toLocaleDateString('uk-UA', { 
                    year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' 
                  })}
                </td>
                <td className="p-4">
                  <div className="text-sm font-medium text-gray-900">Користувач #{order.userId}</div>
                  <div className="text-xs text-gray-500">{order.shippingPhone}</div>
                </td>
                <td className="p-4 text-sm font-medium text-gray-900">
                  ${order.totalAmount}
                  <div className="text-xs text-gray-400 font-normal mt-1">{order.paymentMethod}</div>
                </td>
                <td className="p-4">
                  {getStatusBadge(order.status)}
                  <div className="mt-2 text-xs text-gray-400">
                    Оплата: {order.paymentStatus === 'PAID' ? '✅ Оплачено' : (order.paymentStatus === 'PENDING' ? '⏳ Очікує' : order.paymentStatus)}
                  </div>
                </td>
                <td className="p-4 text-right">
                  <div className="flex justify-end space-x-2">
                    {order.status === 'NEW' && (
                      <button onClick={() => handleStatusUpdate(order.id, 'CONFIRM')} title="Підтвердити" className="p-2 text-blue-600 hover:bg-blue-50 rounded-full">
                        <CheckCircle size={18} />
                      </button>
                    )}
                    {order.status === 'CONFIRMED' && (
                      <button onClick={() => handleStatusUpdate(order.id, 'SHIP')} title="Відправити" className="p-2 text-orange-600 hover:bg-orange-50 rounded-full">
                        <Truck size={18} />
                      </button>
                    )}
                    {order.status === 'SHIPPED' && (
                      <button onClick={() => handleStatusUpdate(order.id, 'COMPLETE')} title="Виконано" className="p-2 text-green-600 hover:bg-green-50 rounded-full">
                        <Package size={18} />
                      </button>
                    )}
                    {order.paymentStatus === 'PENDING' && order.status !== 'CANCELLED' && (
                      <button onClick={() => handleStatusUpdate(order.id, 'PAY')} title="Позначити як оплачено" className="p-2 text-gray-600 hover:bg-gray-100 rounded-full">
                        $
                      </button>
                    )}
                    {(order.status === 'NEW' || order.status === 'CONFIRMED') && (
                      <button onClick={() => handleStatusUpdate(order.id, 'CANCEL')} title="Скасувати" className="p-2 text-red-600 hover:bg-red-50 rounded-full">
                        <XCircle size={18} />
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
            {orders.length === 0 && (
              <tr>
                <td colSpan="6" className="p-8 text-center text-gray-500">
                  Замовлень поки немає
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default AdminOrders;
