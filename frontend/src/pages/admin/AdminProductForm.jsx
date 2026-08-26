import React, { useState, useEffect } from 'react';
import { X, Plus, Trash2 } from 'lucide-react';
import { categoryService } from '../../api/categoryService';

function AdminProductForm({ onClose, onSubmit, initialData }) {
  const [categories, setCategories] = useState([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');

  const [formData, setFormData] = useState({
    name: initialData?.name || '',
    description: initialData?.description || '',
    price: initialData?.price ? parseFloat(initialData.price.replace('$', '')) : '',
    categoryId: initialData?.categoryId || ''
  });

  const [variants, setVariants] = useState(
    initialData?.rawVariants && initialData.rawVariants.length > 0 
      ? initialData.rawVariants 
      : [{ size: 'M', color: 'Чорний', stock: 10 }]
  );

  useEffect(() => {
    // Завантажуємо плоский список категорій для селекта
    categoryService.getAllCategories().then(data => {
      setCategories(data || []);
      if (data && data.length > 0 && !initialData?.categoryId) {
        setFormData(prev => ({ ...prev, categoryId: data[0].categoryId }));
      }
    }).catch(err => console.error("Error loading categories", err));
  }, [initialData]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleVariantChange = (index, field, value) => {
    const newVariants = [...variants];
    newVariants[index][field] = field === 'stock' ? parseInt(value) || 0 : value;
    setVariants(newVariants);
  };

  const addVariant = () => {
    setVariants([...variants, { size: 'M', color: 'Чорний', stock: 10 }]);
  };

  const removeVariant = (index) => {
    if (variants.length > 1) {
      setVariants(variants.filter((_, i) => i !== index));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsSubmitting(true);

    try {
      // Генеруємо slug з назви, якщо це створення нового товару
      const generatedSlug = formData.name.toLowerCase().trim().replace(/[^\w\s-]/g, '').replace(/[\s_-]+/g, '-').replace(/^-+|-+$/g, '');

      const productCreateDTO = {
        name: formData.name,
        slug: initialData?.slug || generatedSlug || 'product-' + Date.now(),
        description: formData.description || 'Немає опису', // Захист від @NotBlank на бекенді
        price: parseFloat(formData.price),
        categoryId: parseInt(formData.categoryId),
        variants: variants.map(v => ({
          size: v.size,
          color: v.color,
          stockQuantity: parseInt(v.stock) || 0
        }))
      };

      await onSubmit(productCreateDTO);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Сталася помилка при збереженні');
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-3xl max-h-[90vh] overflow-y-auto custom-scrollbar">
        <div className="flex justify-between items-center p-6 border-b border-gray-100 sticky top-0 bg-white z-10">
          <h2 className="text-xl font-light uppercase tracking-widest">
            {initialData ? 'Редагувати товар' : 'Додати новий товар'}
          </h2>
          <button onClick={onClose} className="p-2 text-gray-400 hover:text-gray-900 transition-colors">
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-8">
          {/* Основна інформація */}
          <div className="space-y-6">
            <h3 className="text-sm font-medium tracking-widest uppercase text-gray-900 border-b border-gray-100 pb-2">Основна інформація</h3>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-xs uppercase tracking-widest text-gray-500 mb-2">Назва товару</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  required
                  className="w-full border border-gray-300 p-3 text-sm focus:outline-none focus:border-gray-900 transition-colors"
                />
              </div>
              
              <div>
                <label className="block text-xs uppercase tracking-widest text-gray-500 mb-2">Категорія</label>
                <select
                  name="categoryId"
                  value={formData.categoryId}
                  onChange={handleChange}
                  required
                  className="w-full border border-gray-300 p-3 text-sm focus:outline-none focus:border-gray-900 transition-colors bg-white"
                >
                  {categories.map(cat => (
                    <option key={cat.categoryId} value={cat.categoryId}>
                      {cat.categoryName}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-xs uppercase tracking-widest text-gray-500 mb-2">Ціна (USD)</label>
                <input
                  type="number"
                  name="price"
                  value={formData.price}
                  onChange={handleChange}
                  required
                  min="0"
                  step="0.01"
                  className="w-full border border-gray-300 p-3 text-sm focus:outline-none focus:border-gray-900 transition-colors"
                />
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
          </div>

          {/* Варіанти */}
          <div className="space-y-6">
            <div className="flex justify-between items-center border-b border-gray-100 pb-2">
              <h3 className="text-sm font-medium tracking-widest uppercase text-gray-900">Варіанти (Розмір / Колір)</h3>
              <button 
                type="button" 
                onClick={addVariant}
                className="text-xs uppercase tracking-widest font-medium text-gray-600 hover:text-gray-900 flex items-center"
              >
                <Plus size={14} className="mr-1" /> Додати варіант
              </button>
            </div>
            
            <div className="space-y-4">
              {variants.map((variant, index) => (
                <div key={index} className="flex items-center space-x-4 bg-gray-50 p-4 border border-gray-100 rounded-sm">
                  <div className="flex-1">
                    <label className="block text-xs text-gray-500 mb-1">Розмір (напр. 80C, M)</label>
                    <input
                      type="text"
                      value={variant.size}
                      onChange={(e) => handleVariantChange(index, 'size', e.target.value)}
                      required
                      className="w-full border border-gray-300 p-2 text-sm focus:outline-none focus:border-gray-900 bg-white"
                    />
                  </div>
                  <div className="flex-1">
                    <label className="block text-xs text-gray-500 mb-1">Колір</label>
                    <input
                      type="text"
                      value={variant.color}
                      onChange={(e) => handleVariantChange(index, 'color', e.target.value)}
                      required
                      className="w-full border border-gray-300 p-2 text-sm focus:outline-none focus:border-gray-900 bg-white"
                    />
                  </div>
                  <div className="w-24">
                    <label className="block text-xs text-gray-500 mb-1">Кількість</label>
                    <input
                      type="number"
                      value={variant.stock}
                      onChange={(e) => handleVariantChange(index, 'stock', e.target.value)}
                      required
                      min="0"
                      className="w-full border border-gray-300 p-2 text-sm focus:outline-none focus:border-gray-900 bg-white"
                    />
                  </div>
                  <div className="pt-5">
                    <button 
                      type="button" 
                      onClick={() => removeVariant(index)}
                      className="p-2 text-gray-400 hover:text-red-600 transition-colors disabled:opacity-30"
                      disabled={variants.length === 1}
                    >
                      <Trash2 size={18} />
                    </button>
                  </div>
                </div>
              ))}
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
              {isSubmitting ? 'Збереження...' : 'Зберегти товар'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default AdminProductForm;
