import React, { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import { categoryService } from '../../api/categoryService';

function AdminCategoryForm({ onClose, onSubmit, initialData }) {
  const [categories, setCategories] = useState([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');

  const [formData, setFormData] = useState({
    categoryName: initialData?.categoryName || '',
    description: initialData?.description || '',
    parentId: initialData?.parentId || ''
  });

  useEffect(() => {
    // Завантажуємо категорії для вибору батьківської
    categoryService.getAllCategories()
      .then(data => {
        // Відфільтрувати саму себе, щоб не вибрати себе як батька
        const availableCategories = (data || []).filter(c => c.categoryId !== initialData?.categoryId);
        setCategories(availableCategories);
      })
      .catch(err => console.error("Error loading categories", err));
  }, [initialData]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsSubmitting(true);

    try {
      const generatedSlug = formData.categoryName.toLowerCase().trim().replace(/[^\w\s-]/g, '').replace(/[\s_-]+/g, '-').replace(/^-+|-+$/g, '');

      const categoryCreateDTO = {
        categoryName: formData.categoryName,
        slug: initialData?.slug || generatedSlug || 'category-' + Date.now(),
        description: formData.description || 'Немає опису',
        parentId: formData.parentId ? parseInt(formData.parentId) : null
      };

      await onSubmit(categoryCreateDTO);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Сталася помилка при збереженні');
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-xl max-h-[90vh] overflow-y-auto custom-scrollbar">
        <div className="flex justify-between items-center p-6 border-b border-gray-100 sticky top-0 bg-white z-10">
          <h2 className="text-xl font-light uppercase tracking-widest">
            {initialData ? 'Редагувати категорію' : 'Додати нову категорію'}
          </h2>
          <button onClick={onClose} className="p-2 text-gray-400 hover:text-gray-900 transition-colors">
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-6">
          <div className="space-y-4">
            <div>
              <label className="block text-xs uppercase tracking-widest text-gray-500 mb-2">Назва категорії</label>
              <input
                type="text"
                name="categoryName"
                value={formData.categoryName}
                onChange={handleChange}
                required
                className="w-full border border-gray-300 p-3 text-sm focus:outline-none focus:border-gray-900 transition-colors"
              />
            </div>
            
            <div>
              <label className="block text-xs uppercase tracking-widest text-gray-500 mb-2">Батьківська категорія</label>
              <select
                name="parentId"
                value={formData.parentId}
                onChange={handleChange}
                className="w-full border border-gray-300 p-3 text-sm focus:outline-none focus:border-gray-900 transition-colors bg-white"
              >
                <option value="">-- Немає (Коренева категорія) --</option>
                {categories.map(cat => (
                  <option key={cat.categoryId} value={cat.categoryId}>
                    {cat.categoryName}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs uppercase tracking-widest text-gray-500 mb-2">Опис (необов'язково)</label>
              <input
                type="text"
                name="description"
                value={formData.description}
                onChange={handleChange}
                className="w-full border border-gray-300 p-3 text-sm focus:outline-none focus:border-gray-900 transition-colors"
              />
            </div>
          </div>

          {error && (
            <div className="bg-red-50 text-red-600 p-4 text-sm rounded-sm">
              {error}
            </div>
          )}

          <div className="pt-6 border-t border-gray-100 flex justify-end space-x-4">
            <button 
              type="button"
              onClick={onClose}
              className="px-6 py-3 text-sm font-medium uppercase tracking-widest text-gray-600 hover:text-gray-900 transition-colors border border-transparent hover:border-gray-200"
            >
              Скасувати
            </button>
            <button 
              type="submit"
              disabled={isSubmitting}
              className="bg-gray-900 text-white px-8 py-3 text-sm font-medium uppercase tracking-widest hover:bg-gray-800 transition-colors disabled:bg-gray-400"
            >
              {isSubmitting ? 'Збереження...' : 'Зберегти'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default AdminCategoryForm;
