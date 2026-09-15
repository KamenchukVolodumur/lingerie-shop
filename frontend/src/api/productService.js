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

  // Отримати товари категорії ТА ВСІХ її підкатегорій (фронтенд агрегація)
  getProductsForCategoryAndChildren: async (categoryId) => {
    try {
      // 1. Отримуємо всі категорії, щоб знайти дочірні
      const allCategories = await categoryService.getAllCategories();
      
      // 2. Знаходимо всі ID (батьківська + всі вкладені)
      const categoryIdsToFetch = new Set([parseInt(categoryId)]);
      
      const findChildren = (parentId) => {
        allCategories.forEach(cat => {
          if (cat.parentId === parentId) {
            categoryIdsToFetch.add(cat.categoryId);
            findChildren(cat.categoryId); // Рекурсивно для глибшої вкладеності
          }
        });
      };
      findChildren(parseInt(categoryId));

      // 3. Робимо паралельні запити для кожної знайденої категорії
      const promises = Array.from(categoryIdsToFetch).map(id => 
        api.get(`/products/category/${id}`)
      );
      const responses = await Promise.all(promises);
      
      // 4. Збираємо всі товари разом
      let allProducts = [];
      responses.forEach(res => {
        if (res.data && Array.isArray(res.data)) {
          allProducts = [...allProducts, ...res.data];
        }
      });

      // 5. Видаляємо дублікати (на випадок якщо бекенд повернув один і той самий товар)
      const uniqueProductsMap = new Map();
      allProducts.forEach(prod => {
        uniqueProductsMap.set(prod.id, prod);
      });
      const uniqueProducts = Array.from(uniqueProductsMap.values());

      return uniqueProducts.map(mapProductFromBackend);
    } catch (error) {
      console.error("Error fetching products for category tree:", error);
      return [];
    }
  }
};
