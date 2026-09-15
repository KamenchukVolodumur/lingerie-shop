import React, { useState, useEffect } from 'react';
import { productService } from '../../api/productService';
import { categoryService } from '../../api/categoryService';
import { Plus, Edit2, Trash2 } from 'lucide-react';
import AdminProductForm from './AdminProductForm';

function AdminProducts() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState({});
  const [loading, setLoading] = useState(true);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [productsData, categoriesData] = await Promise.all([
        productService.getAllProducts(),
        categoryService.getAllCategories()
      ]);
      
      setProducts(productsData || []);
      
      const catMap = {};
      (categoriesData || []).forEach(cat => {
        catMap[cat.categoryId] = cat.categoryName;
      });
      setCategories(catMap);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const fetchProducts = async () => {
    try {
      const data = await productService.getAllProducts();
      setProducts(data || []);
    } catch (err) {
      console.error(err);
    }
  };

  const handleSaveProduct = async (productData) => {
    if (editingProduct) {
      await productService.updateProduct(editingProduct.id, productData);
    } else {
      await productService.createProduct(productData);
    }
    setIsFormOpen(false);
    setEditingProduct(null);
    fetchProducts(); // Refresh list
  };

  const openCreateForm = () => {
    setEditingProduct(null);
    setIsFormOpen(true);
  };

  const openEditForm = async (product) => {
    try {
      // Завантажуємо повну версію товару з варіантами
      const fullProduct = await productService.getProductById(product.id);
      setEditingProduct(fullProduct);
      setIsFormOpen(true);
    } catch (err) {
      alert('Помилка завантаження товару');
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Ви впевнені, що хочете видалити цей товар?')) {
      try {
        await productService.deleteProduct(id);
        fetchProducts(); // Refresh list
      } catch (err) {
        alert('Помилка видалення: ' + (err.response?.data?.message || err.message));
      }
    }
  };

  if (loading && products.length === 0) return <div className="p-8">Завантаження...</div>;

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-2xl font-light tracking-widest uppercase">Товари</h1>
        <button 
          onClick={openCreateForm}
          className="bg-gray-900 text-white px-6 py-2 text-sm uppercase tracking-widest hover:bg-gray-800 transition-colors flex items-center"
        >
          <Plus size={18} className="mr-2" />
          Додати товар
        </button>
      </div>

      <div className="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-gray-50 border-b border-gray-200">
              <th className="p-4 text-xs font-medium text-gray-500 uppercase tracking-widest">ID</th>
              <th className="p-4 text-xs font-medium text-gray-500 uppercase tracking-widest">Фото</th>
              <th className="p-4 text-xs font-medium text-gray-500 uppercase tracking-widest">Назва</th>
              <th className="p-4 text-xs font-medium text-gray-500 uppercase tracking-widest">Ціна</th>
              <th className="p-4 text-xs font-medium text-gray-500 uppercase tracking-widest">Категорія</th>
              <th className="p-4 text-xs font-medium text-gray-500 uppercase tracking-widest text-right">Дії</th>
            </tr>
          </thead>
          <tbody>
            {products.map((product) => (
              <tr key={product.id} className="border-b border-gray-100 hover:bg-gray-50 transition-colors">
                <td className="p-4 text-sm text-gray-500">#{product.id}</td>
                <td className="p-4">
                  <div className="w-12 h-16 bg-gray-100 overflow-hidden">
                    <img 
                      src={product.imageUrl || product.colors?.[0]?.image || 'https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?auto=format&fit=crop&q=80'} 
                      alt={product.name} 
                      className="w-full h-full object-cover"
                    />
                  </div>
                </td>
                <td className="p-4 text-sm font-medium text-gray-900">{product.name}</td>
                <td className="p-4 text-sm text-gray-500">{product.price}</td>
                <td className="p-4 text-sm text-gray-500">{categories[product.categoryId] || 'Без категорії'}</td>
                <td className="p-4 text-right">
                  <div className="flex justify-end space-x-2">
                    <button onClick={() => openEditForm(product)} className="p-2 text-blue-600 hover:bg-blue-50 rounded-full" title="Редагувати">
                      <Edit2 size={18} />
                    </button>
                    <button onClick={() => handleDelete(product.id)} className="p-2 text-red-600 hover:bg-red-50 rounded-full" title="Видалити">
                      <Trash2 size={18} />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
            {products.length === 0 && (
              <tr>
                <td colSpan="6" className="p-8 text-center text-gray-500">
                  Товарів не знайдено
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {isFormOpen && (
        <AdminProductForm 
          initialData={editingProduct}
          onClose={() => {
            setIsFormOpen(false);
            setEditingProduct(null);
          }}
          onSubmit={handleSaveProduct}
        />
      )}
    </div>
  );
}

export default AdminProducts;
