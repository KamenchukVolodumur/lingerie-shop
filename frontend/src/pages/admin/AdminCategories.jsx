import React, { useState, useEffect } from 'react';
import { categoryService } from '../../api/categoryService';
import { Plus, Edit2, Trash2, Folder } from 'lucide-react';
import AdminCategoryForm from './AdminCategoryForm';

function AdminCategories() {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      setLoading(true);
      const data = await categoryService.getAllCategories();
      setCategories(data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSaveCategory = async (categoryData) => {
    if (editingCategory) {
      await categoryService.updateCategory(editingCategory.categoryId, categoryData);
    } else {
      await categoryService.createCategory(categoryData);
    }
    setIsFormOpen(false);
    setEditingCategory(null);
    fetchCategories(); // Refresh list
  };

  const openCreateForm = () => {
    setEditingCategory(null);
    setIsFormOpen(true);
  };

  const openEditForm = (category) => {
    setEditingCategory(category);
    setIsFormOpen(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Ви впевнені, що хочете видалити цю категорію? Усі товари в ній можуть втратити прив\'язку.')) {
      try {
        await categoryService.deleteCategory(id);
        fetchCategories(); // Refresh list
      } catch (err) {
        alert('Помилка видалення: ' + (err.response?.data?.message || err.message));
      }
    }
  };

  // Допоміжна функція для пошуку імені батьківської категорії
  const getParentName = (parentId) => {
    if (!parentId) return '—';
    const parent = categories.find(c => c.categoryId === parentId);
    return parent ? parent.categoryName : '—';
  };

  if (loading && categories.length === 0) return <div className="p-8">Завантаження...</div>;

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-2xl font-light tracking-widest uppercase">Категорії</h1>
        <button 
          onClick={openCreateForm}
          className="bg-gray-900 text-white px-6 py-2 text-sm uppercase tracking-widest hover:bg-gray-800 transition-colors flex items-center"
        >
          <Plus size={18} className="mr-2" />
          Додати категорію
        </button>
      </div>

      <div className="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-gray-50 border-b border-gray-200">
              <th className="p-4 text-xs font-medium text-gray-500 uppercase tracking-widest">ID</th>
              <th className="p-4 text-xs font-medium text-gray-500 uppercase tracking-widest">Назва</th>
              <th className="p-4 text-xs font-medium text-gray-500 uppercase tracking-widest">Slug</th>
              <th className="p-4 text-xs font-medium text-gray-500 uppercase tracking-widest">Батьківська</th>
              <th className="p-4 text-xs font-medium text-gray-500 uppercase tracking-widest text-right">Дії</th>
            </tr>
          </thead>
          <tbody>
            {categories.map((category) => (
              <tr key={category.categoryId} className="border-b border-gray-100 hover:bg-gray-50 transition-colors">
                <td className="p-4 text-sm text-gray-500">#{category.categoryId}</td>
                <td className="p-4 text-sm font-medium text-gray-900 flex items-center">
                  <Folder size={16} className="text-gray-400 mr-2" />
                  {category.categoryName}
                </td>
                <td className="p-4 text-sm text-gray-500">{category.slug}</td>
                <td className="p-4 text-sm text-gray-500">{getParentName(category.parentId)}</td>
                <td className="p-4 text-right">
                  <div className="flex justify-end space-x-2">
                    <button onClick={() => openEditForm(category)} className="p-2 text-blue-600 hover:bg-blue-50 rounded-full" title="Редагувати">
                      <Edit2 size={18} />
                    </button>
                    <button onClick={() => handleDelete(category.categoryId)} className="p-2 text-red-600 hover:bg-red-50 rounded-full" title="Видалити">
                      <Trash2 size={18} />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
            {categories.length === 0 && (
              <tr>
                <td colSpan="5" className="p-8 text-center text-gray-500">
                  Категорій не знайдено
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {isFormOpen && (
        <AdminCategoryForm 
          initialData={editingCategory}
          onClose={() => {
            setIsFormOpen(false);
            setEditingCategory(null);
          }}
          onSubmit={handleSaveCategory}
        />
      )}
    </div>
  );
}

export default AdminCategories;
