import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

function AdminLogin() {
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    // Якщо вже залогований - пускаємо в адмінку
    if (localStorage.getItem('adminAuth') === 'true') {
      navigate('/admin/orders');
    }
  }, [navigate]);

  const handleSubmit = (e) => {
    e.preventDefault();
    // Хардкодний пароль для доступу до адмінки поки немає auth-service
    if (password === 'admin123') {
      localStorage.setItem('adminAuth', 'true');
      navigate('/admin/orders');
    } else {
      setError('Невірний пароль');
      setPassword('');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="bg-white p-10 max-w-md w-full shadow-sm border border-gray-100">
        <h1 className="text-2xl font-light tracking-widest uppercase mb-8 text-center">
          Aura Admin
        </h1>
        
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="block text-xs uppercase tracking-widest text-gray-500 mb-2">
              Пароль доступу
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full border-b border-gray-300 py-3 text-gray-900 focus:outline-none focus:border-gray-900 transition-colors bg-transparent placeholder-gray-400"
              placeholder="Введіть пароль"
              autoFocus
            />
          </div>

          {error && (
            <p className="text-sm text-red-500">{error}</p>
          )}

          <button
            type="submit"
            className="w-full bg-gray-900 text-white py-4 text-sm font-medium uppercase tracking-widest hover:bg-gray-800 transition-colors"
          >
            Увійти
          </button>
        </form>
      </div>
    </div>
  );
}

export default AdminLogin;
