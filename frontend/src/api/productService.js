import api from './axiosConfig';
import { mapProductFromBackend } from '../utils/mapper';
import { categoryService } from './categoryService';

export const productService = {
  // Отримати всі товари з пагінацією
  getAllProducts: async (page = 0, size = 100) => {
    const response = await api.get(`/products?page=${page}&size=${size}`);
    const products = response.data.content || response.data;
    return products.map(mapProductFromBackend);
  },

  uploadImage: async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post('/products/upload-image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    return response.data; // { url: '...' }
  },

  createProduct: async (productData) => {
    const response = await api.post('/products', productData);
    return response.data;
  },

  updateProduct: async (id, productData) => {
    const response = await api.put(`/products/${id}`, productData);
    return response.data;
  },

  deleteProduct: async (id) => {
    await api.delete(`/products/${id}`);
  },

  // Отримати товар за ID
  getProductById: async (id) => {
    const response = await api.get(`/products/${id}`);
    return mapProductFromBackend(response.data);
  },

  // Отримати товари лише однієї конкретної категорії
  getProductsByCategory: async (categoryId) => {
    const response = await api.get(`/products/category/${categoryId}`);
    return response.data.map(mapProductFromBackend);
  },

  getProductsForCategoryAndChildren: async (categoryId, filters = {}) => {
    try {
      // Create query params from filters
      const params = new URLSearchParams();
      params.append('categoryId', categoryId);
      
      Object.entries(filters).forEach(([key, values]) => {
        if (values && values.length > 0) {
          // If the backend expects comma-separated or first element, adapt here.
          // Currently, searchProducts parses allParams as a Map<String, String>.
          // So if we have multiple values, we can join them by comma or just pass single.
          // Wait, if it's an array, join by comma. But backend uses `@> cast(:attributesJson as jsonb)`.
          // If we pass `fit=high_waist`, it parses to `{"fit": "high_waist"}`.
          // If we allow multiple check boxes for one attribute (e.g. material: cotton OR silk),
          // the native SQL `attributes @> '{"material": "cotton"}'` won't work easily with OR logic unless written carefully.
          // For MVP, let's just join them by comma (or just pass single value per key for now)
          // To keep it simple, we will just send the first selected value per filter, or we can send it as string.
          params.append(key, Array.isArray(values) ? values[0] : values);
        }
      });

      const response = await api.get(`/products/search?${params.toString()}`);
      
      if (response.data && Array.isArray(response.data)) {
        return response.data.map(mapProductFromBackend);
      }
      return [];
    } catch (error) {
      console.error("Error fetching products for category tree:", error);
      return [];
    }
  }
};
