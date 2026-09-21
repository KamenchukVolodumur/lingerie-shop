import React, { useState, useEffect, useRef } from 'react';
import { Upload, X, Plus, Trash2, Search, ChevronDown, Image as ImageIcon, Loader2 } from 'lucide-react';
import { categoryService } from '../../api/categoryService';
import { translateKey, translateValue } from '../../utils/translations';
import { productService } from '../../api/productService';

function AdminProductForm({ onClose, onSubmit, initialData }) {
  const [categories, setCategories] = useState([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');

  const [formData, setFormData] = useState({
    name: initialData?.name || '',
    description: initialData?.description || '',
    price: initialData?.price ? Number(initialData.price) : '',
    categoryId: initialData?.categoryId || '',
    imageUrl: initialData?.imageUrl || ''
  });
  const [isUploadingImage, setIsUploadingImage] = useState(false);

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

  const handleImageUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    try {
      setIsUploadingImage(true);
      setError('');
      const response = await productService.uploadImage(file);
      setFormData(prev => ({ ...prev, imageUrl: response.url }));
    } catch (err) {
      setError('Помилка завантаження фотографії. Спробуйте ще раз.');
      console.error(err);
    } finally {
      setIsUploadingImage(false);
    }
  };

  const addVariant = () => {
    setVariants([...variants, { size: 'M', color: 'Чорний', stock: 10 }]);
  };

  const removeVariant = (index) => {
    if (variants.length > 1) {
      setVariants(variants.filter((_, i) => i !== index));
    }
  };

  const [attributes, setAttributes] = useState(() => {
    if (initialData?.attributes && Object.keys(initialData.attributes).length > 0) {
      return Object.entries(initialData.attributes).map(([key, value]) => ({ key, value }));
    }
    return [];
  });

  // Існуючі фільтри з бази для автокомпліту
  const [existingFilters, setExistingFilters] = useState({});
  const [openDropdown, setOpenDropdown] = useState(null); // "key-0", "value-2", etc.

  // Підтягуємо існуючі атрибути коли змінюється категорія
  useEffect(() => {
    if (formData.categoryId) {
      categoryService.getCategoryFilters(formData.categoryId).then(data => {
        setExistingFilters(data || {});
      }).catch(() => setExistingFilters({}));
    }
  }, [formData.categoryId]);

  const handleAttributeChange = (index, field, value) => {
    const newAttributes = [...attributes];
    newAttributes[index][field] = value;
    setAttributes(newAttributes);
  };

  const selectSuggestion = (index, field, value) => {
    handleAttributeChange(index, field, value);
    setOpenDropdown(null);
  };

  const addAttribute = () => {
    setAttributes([...attributes, { key: '', value: '' }]);
  };

  const removeAttribute = (index) => {
    setAttributes(attributes.filter((_, i) => i !== index));
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
        imageUrl: formData.imageUrl,
        price: parseFloat(formData.price),
        categoryId: parseInt(formData.categoryId),
        variants: variants.map(v => ({
          size: v.size,
          color: v.color,
          stockQuantity: parseInt(v.stock) || 0
        })),
        attributes: attributes.reduce((acc, curr) => {
          if (curr.key && curr.value) acc[curr.key] = curr.value;
          return acc;
        }, {})
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
                <label className="block text-xs uppercase tracking-widest text-gray-500 mb-2">Ціна (грн)</label>
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

            {/* Завантаження фото */}
            <div>
              <label className="block text-xs uppercase tracking-widest text-gray-500 mb-2">Фотографія товару</label>
              <div className="flex items-center space-x-4">
                {formData.imageUrl ? (
                  <div className="relative w-24 h-24 border border-gray-200 rounded overflow-hidden">
                    <img src={formData.imageUrl} alt="Preview" className="w-full h-full object-cover" />
                    <button 
                      type="button" 
                      onClick={() => setFormData(prev => ({...prev, imageUrl: ''}))}
                      className="absolute top-1 right-1 bg-white rounded-full p-1 shadow hover:text-red-500"
                    >
                      <X size={14} />
                    </button>
                  </div>
                ) : (
                  <div className="w-24 h-24 border-2 border-dashed border-gray-300 rounded flex flex-col items-center justify-center text-gray-400 bg-gray-50">
                    {isUploadingImage ? <Loader2 className="animate-spin" size={24} /> : <ImageIcon size={24} />}
                  </div>
                )}
                
                <div className="flex-1">
                  <label className="cursor-pointer bg-white border border-gray-300 text-gray-700 px-4 py-2 text-sm uppercase tracking-widest hover:bg-gray-50 transition-colors inline-block">
                    {isUploadingImage ? 'Завантаження...' : 'Оберіть файл'}
                    <input 
                      type="file" 
                      accept="image/*" 
                      onChange={handleImageUpload} 
                      className="hidden" 
                      disabled={isUploadingImage}
                    />
                  </label>
                  <p className="text-xs text-gray-400 mt-2">JPG, PNG до 5MB</p>
                </div>
              </div>
            </div>
          </div>

          {/* Характеристики (Атрибути) */}
          <div className="space-y-6">
            <div className="flex justify-between items-center border-b border-gray-100 pb-2">
              <h3 className="text-sm font-medium tracking-widest uppercase text-gray-900">Характеристики (Атрибути)</h3>
              <button 
                type="button" 
                onClick={addAttribute}
                className="text-xs uppercase tracking-widest font-medium text-gray-600 hover:text-gray-900 flex items-center"
              >
                <Plus size={14} className="mr-1" /> Додати параметр
              </button>
            </div>
            
            {Object.keys(existingFilters).length > 0 && attributes.length === 0 && (
              <div className="flex flex-wrap gap-2">
                <span className="text-xs text-gray-400 mr-1 self-center">Швидке додавання:</span>
                {Object.keys(existingFilters).map(key => (
                  <button
                    key={key}
                    type="button"
                    onClick={() => setAttributes(prev => [...prev, { key, value: '' }])}
                    className="px-3 py-1 text-xs border border-gray-300 text-gray-600 hover:border-gray-900 hover:text-gray-900 transition-colors rounded-sm"
                  >
                    + {key}
                  </button>
                ))}
              </div>
            )}

            <div className="space-y-4">
              {attributes.length === 0 ? (
                <p className="text-sm text-gray-500 italic">Немає характеристик. Натисніть "Додати параметр" або оберіть існуючий вище.</p>
              ) : (
                attributes.map((attr, index) => {
                  const keySuggestions = Object.keys(existingFilters).filter(
                    k => k.toLowerCase().includes(attr.key.toLowerCase()) && k !== attr.key
                  );
                  const valueSuggestions = attr.key && existingFilters[attr.key]
                    ? [...existingFilters[attr.key]].filter(
                        v => v.toLowerCase().includes(attr.value.toLowerCase()) && v !== attr.value
                      )
                    : [];

                  return (
                    <div key={index} className="flex items-start space-x-4 bg-gray-50 p-4 border border-gray-100 rounded-sm">
                      {/* Ключ з автокомплітом */}
                      <div className="flex-1 relative">
                        <label className="block text-xs text-gray-500 mb-1">Ключ (напр. material)</label>
                        <input
                          type="text"
                          value={attr.key}
                          onChange={(e) => {
                            handleAttributeChange(index, 'key', e.target.value);
                            setOpenDropdown(`key-${index}`);
                          }}
                          onFocus={() => setOpenDropdown(`key-${index}`)}
                          onBlur={() => setTimeout(() => setOpenDropdown(null), 150)}
                          placeholder="Ключ"
                          required
                          className="w-full border border-gray-300 p-2 text-sm focus:outline-none focus:border-gray-900 bg-white"
                        />
                        {openDropdown === `key-${index}` && keySuggestions.length > 0 && (
                          <div className="absolute z-20 top-full left-0 right-0 bg-white border border-gray-200 shadow-lg max-h-32 overflow-y-auto">
                            {keySuggestions.map(suggestion => (
                              <button
                                key={suggestion}
                                type="button"
                                onMouseDown={() => selectSuggestion(index, 'key', suggestion)}
                                className="w-full text-left px-3 py-2 text-sm hover:bg-gray-100 transition-colors"
                              >
                                {suggestion}
                              </button>
                            ))}
                          </div>
                        )}
                      </div>
                      {/* Значення з автокомплітом */}
                      <div className="flex-1 relative">
                        <label className="block text-xs text-gray-500 mb-1">Значення (напр. cotton)</label>
                        <input
                          type="text"
                          value={attr.value}
                          onChange={(e) => {
                            handleAttributeChange(index, 'value', e.target.value);
                            setOpenDropdown(`value-${index}`);
                          }}
                          onFocus={() => setOpenDropdown(`value-${index}`)}
                          onBlur={() => setTimeout(() => setOpenDropdown(null), 150)}
                          placeholder="Значення"
                          required
                          className="w-full border border-gray-300 p-2 text-sm focus:outline-none focus:border-gray-900 bg-white"
                        />
                        {openDropdown === `value-${index}` && valueSuggestions.length > 0 && (
                          <div className="absolute z-20 top-full left-0 right-0 bg-white border border-gray-200 shadow-lg max-h-32 overflow-y-auto">
                            {valueSuggestions.map(suggestion => (
                              <button
                                key={suggestion}
                                type="button"
                                onMouseDown={() => selectSuggestion(index, 'value', suggestion)}
                                className="w-full text-left px-3 py-2 text-sm hover:bg-gray-100 transition-colors"
                              >
                                {suggestion}
                              </button>
                            ))}
                          </div>
                        )}
                      </div>
                      <div className="pt-5">
                        <button 
                          type="button" 
                          onClick={() => removeAttribute(index)}
                          className="p-2 text-gray-400 hover:text-red-600 transition-colors"
                        >
                          <Trash2 size={18} />
                        </button>
                      </div>
                    </div>
                  );
                })
              )}
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
