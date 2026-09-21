import React, { useState, useEffect, useMemo } from 'react';
import { adminOrderService } from '../../api/adminOrderService';
import { productService } from '../../api/productService';
import { 
  TrendingUp, 
  ShoppingBag, 
  AlertCircle, 
  Package, 
  Clock,
  CheckCircle,
  Tag,
  Filter
} from 'lucide-react';
import { Link } from 'react-router-dom';
import { formatUah } from '../../utils/currency';

const MONTH_NAMES = {
  "01": "Січень", "02": "Лютий", "03": "Березень", "04": "Квітень",
  "05": "Травень", "06": "Червень", "07": "Липень", "08": "Серпень",
  "09": "Вересень", "10": "Жовтень", "11": "Листопад", "12": "Грудень"
};

function AdminDashboard() {
  const [rawOrders, setRawOrders] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  
  const [selectedMonth, setSelectedMonth] = useState('ALL'); // 'ALL' або 'YYYY-MM'

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      
      // Завантажуємо останні 500 замовлень для побудови аналітики та всі товари
      const [ordersData, productsData] = await Promise.all([
        adminOrderService.getAllOrders(0, 500),
        productService.getAllProducts()
      ]);

      setRawOrders(ordersData.content || []);
      setProducts(productsData || []);

    } catch (err) {
      console.error("Error loading dashboard data:", err);
    } finally {
      setLoading(false);
    }
  };

  // 1. Отримуємо список унікальних місяців із замовлень для фільтру
  const availableMonths = useMemo(() => {
    const monthsSet = new Set();
    rawOrders.forEach(order => {
      if (order.createdAt) {
        // order.createdAt має вигляд "2026-08-26T12:34:56..."
        const yyyyMM = order.createdAt.substring(0, 7); 
        monthsSet.add(yyyyMM);
      }
    });
    // Сортуємо від новіших до старіших
    return Array.from(monthsSet).sort((a, b) => b.localeCompare(a));
  }, [rawOrders]);

  // 2. Фільтруємо замовлення за обраним місяцем
  const filteredOrders = useMemo(() => {
    if (selectedMonth === 'ALL') return rawOrders;
    return rawOrders.filter(order => order.createdAt && order.createdAt.startsWith(selectedMonth));
  }, [rawOrders, selectedMonth]);

  // 3. Підраховуємо динамічну статистику
  const stats = useMemo(() => {
    const validStatuses = ['CONFIRMED', 'SHIPPED', 'DELIVERED', 'COMPLETED'];
    
    // Лише успішні замовлення
    const successfulOrders = filteredOrders.filter(order => validStatuses.includes(order.status));
    
    const revenue = successfulOrders.reduce((sum, order) => sum + (parseFloat(order.totalAmount) || 0), 0);
    
    const itemsSold = successfulOrders.reduce((totalItems, order) => {
      const orderItems = order.items || [];
      const orderQuantity = orderItems.reduce((sum, item) => sum + (item.quantity || 1), 0);
      return totalItems + orderQuantity;
    }, 0);

    const newOrdersCount = filteredOrders.filter(order => order.status === 'NEW').length;

    return {
      totalRevenue: revenue,
      newOrders: newOrdersCount,
      itemsSold: itemsSold,
      recentOrders: filteredOrders.slice(0, 5) // Останні 5 з вибраного періоду
    };
  }, [filteredOrders]);

  // 4. Підраховуємо глобальну статистику (не залежить від місяця)
  const globalStats = useMemo(() => {
    const lowStock = [];
    products.forEach(product => {
      if (product.rawVariants) {
        product.rawVariants.forEach(variant => {
          if (variant.stock > 0 && variant.stock <= 5) {
            lowStock.push({
              productName: product.name,
              size: variant.size,
              color: variant.color,
              stock: variant.stock
            });
          }
        });
      }
    });

    return {
      totalProducts: products.length,
      lowStockItems: lowStock
    };
  }, [products]);


  const getStatusBadge = (status) => {
    switch (status) {
      case 'NEW':
        return <span className="px-2 py-1 bg-yellow-100 text-yellow-800 rounded-full text-xs font-medium">Нове</span>;
      case 'CONFIRMED':
        return <span className="px-2 py-1 bg-blue-100 text-blue-800 rounded-full text-xs font-medium">Підтверджено</span>;
      case 'SHIPPED':
        return <span className="px-2 py-1 bg-purple-100 text-purple-800 rounded-full text-xs font-medium">Відправлено</span>;
      case 'COMPLETED':
      case 'DELIVERED':
        return <span className="px-2 py-1 bg-green-100 text-green-800 rounded-full text-xs font-medium">Виконано</span>;
      case 'CANCELLED':
        return <span className="px-2 py-1 bg-red-100 text-red-800 rounded-full text-xs font-medium">Скасовано</span>;
      default:
        return <span className="px-2 py-1 bg-gray-100 text-gray-800 rounded-full text-xs font-medium">{status}</span>;
    }
  };

  const formatMonthLabel = (yyyyMM) => {
    const [year, month] = yyyyMM.split('-');
    return `${MONTH_NAMES[month]} ${year}`;
  };

  if (loading) {
    return <div className="p-8 flex justify-center text-gray-500">Завантаження статистики...</div>;
  }

  return (
    <div className="space-y-8">
      {/* Header with Filter */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center space-y-4 sm:space-y-0">
        <h1 className="text-2xl font-light tracking-widest uppercase">Аналітика</h1>
        
        <div className="flex items-center space-x-2 bg-white px-4 py-2 rounded-lg border border-gray-200 shadow-sm">
          <Filter size={16} className="text-gray-400" />
          <select 
            value={selectedMonth}
            onChange={(e) => setSelectedMonth(e.target.value)}
            className="text-sm border-none focus:ring-0 bg-transparent text-gray-700 font-medium cursor-pointer outline-none"
          >
            <option value="ALL">За весь час</option>
            {availableMonths.map(month => (
              <option key={month} value={month}>{formatMonthLabel(month)}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Dynamic Stats Grid (Depends on Month Filter) */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        
        {/* Revenue Card */}
        <div className="bg-white p-6 rounded-xl border border-gray-100 shadow-sm flex items-start space-x-4">
          <div className="p-3 bg-green-50 text-green-600 rounded-lg">
            <TrendingUp size={24} />
          </div>
          <div>
            <p className="text-sm text-gray-500 uppercase tracking-wider mb-1">Дохід</p>
            <h3 className="text-2xl font-medium">{formatUah(stats.totalRevenue)}</h3>
          </div>
        </div>

        {/* Items Sold Card */}
        <div className="bg-white p-6 rounded-xl border border-gray-100 shadow-sm flex items-start space-x-4">
          <div className="p-3 bg-indigo-50 text-indigo-600 rounded-lg">
            <Tag size={24} />
          </div>
          <div>
            <p className="text-sm text-gray-500 uppercase tracking-wider mb-1">Продано товарів</p>
            <h3 className="text-2xl font-medium">{stats.itemsSold} <span className="text-sm font-normal text-gray-400">шт.</span></h3>
          </div>
        </div>

        {/* New Orders Card */}
        <div className="bg-white p-6 rounded-xl border border-gray-100 shadow-sm flex items-start space-x-4">
          <div className="p-3 bg-blue-50 text-blue-600 rounded-lg">
            <ShoppingBag size={24} />
          </div>
          <div>
            <p className="text-sm text-gray-500 uppercase tracking-wider mb-1">Нові замовлення</p>
            <h3 className="text-2xl font-medium">{stats.newOrders}</h3>
            {stats.newOrders > 0 && (
              <p className="text-xs text-blue-600 mt-1 flex items-center">
                <Clock size={12} className="mr-1" /> Очікують обробки
              </p>
            )}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Recent Orders Table (Dynamic) */}
        <div className="lg:col-span-2 bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden flex flex-col">
          <div className="p-6 border-b border-gray-100 flex justify-between items-center">
            <h2 className="text-lg font-medium tracking-wide">
              {selectedMonth === 'ALL' ? 'Останні замовлення' : `Замовлення (${formatMonthLabel(selectedMonth)})`}
            </h2>
            <Link to="/admin/orders" className="text-sm text-blue-600 hover:underline">Всі замовлення</Link>
          </div>
          <div className="overflow-x-auto flex-1">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-100">
                  <th className="p-4 text-xs font-medium text-gray-500 uppercase">№ / Дата</th>
                  <th className="p-4 text-xs font-medium text-gray-500 uppercase">Клієнт</th>
                  <th className="p-4 text-xs font-medium text-gray-500 uppercase">Сума</th>
                  <th className="p-4 text-xs font-medium text-gray-500 uppercase">Статус</th>
                </tr>
              </thead>
              <tbody>
                {stats.recentOrders.map(order => (
                  <tr key={order.id} className="border-b border-gray-50 hover:bg-gray-50 transition-colors">
                    <td className="p-4">
                      <p className="text-sm font-medium">#{order.orderNumber}</p>
                      <p className="text-xs text-gray-400 mt-1">
                        {new Date(order.createdAt).toLocaleDateString('uk-UA')}
                      </p>
                    </td>
                    <td className="p-4 text-sm text-gray-600">{order.shippingPhone}</td>
                    <td className="p-4 text-sm font-medium">{formatUah(order.totalAmount)}</td>
                    <td className="p-4">{getStatusBadge(order.status)}</td>
                  </tr>
                ))}
                {stats.recentOrders.length === 0 && (
                  <tr>
                    <td colSpan="4" className="p-8 text-center text-gray-500">
                      У цьому періоді немає замовлень
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Global Inventory Status (Static) */}
        <div className="space-y-6">
          {/* Total Products (Global) */}
          <div className="bg-white p-6 rounded-xl border border-gray-100 shadow-sm flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-500 uppercase tracking-wider mb-1">Асортимент</p>
              <h3 className="text-2xl font-medium">{globalStats.totalProducts} <span className="text-sm font-normal text-gray-400">товарів</span></h3>
            </div>
            <div className="p-3 bg-gray-50 text-gray-400 rounded-lg">
              <Package size={24} />
            </div>
          </div>

          {/* Low Stock List (Global) */}
          <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden flex flex-col h-[380px]">
            <div className="p-6 border-b border-gray-100 flex items-center justify-between">
              <h2 className="text-lg font-medium tracking-wide">Увага: Мало на складі</h2>
              <div className="p-2 bg-red-50 text-red-600 rounded-lg">
                <AlertCircle size={20} />
              </div>
            </div>
            <div className="p-0 overflow-y-auto custom-scrollbar flex-1">
              {globalStats.lowStockItems.length > 0 ? (
                <ul className="divide-y divide-gray-100">
                  {globalStats.lowStockItems.map((item, idx) => (
                    <li key={idx} className="p-4 hover:bg-gray-50 flex justify-between items-center transition-colors">
                      <div>
                        <p className="text-sm font-medium text-gray-900">{item.productName}</p>
                        <p className="text-xs text-gray-500 mt-1">
                          Колір: {item.color} | Розмір: {item.size}
                        </p>
                      </div>
                      <div className="bg-red-50 text-red-700 px-3 py-1 rounded-full text-xs font-bold border border-red-100 shadow-sm">
                        {item.stock} шт
                      </div>
                    </li>
                  ))}
                </ul>
              ) : (
                <div className="h-full flex flex-col items-center justify-center text-gray-500 p-8">
                  <CheckCircle size={32} className="text-green-400 mb-3" />
                  <p>Зі складом все гаразд!</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

    </div>
  );
}

export default AdminDashboard;
