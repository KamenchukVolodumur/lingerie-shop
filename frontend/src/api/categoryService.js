import api from './axiosConfig';

export const categoryService = {
  // Отримати всі категорії плоским списком
  getAllCategories: async () => {
    const response = await api.get('/categories');
    return response.data;
  },

  // Отримати дерево категорій (фронтенд-трансформація)
  getCategoryTree: async () => {
    const categories = await categoryService.getAllCategories();
    return buildTree(categories);
  },

  createCategory: async (categoryData) => {
    const response = await api.post('/categories', categoryData);
    return response.data;
  },

  updateCategory: async (id, categoryData) => {
    const response = await api.put(`/categories/${id}`, categoryData);
    return response.data;
  },

  deleteCategory: async (id) => {
    await api.delete(`/categories/${id}`);
  }
};

// Допоміжна функція для побудови дерева з плоского масиву
function buildTree(flatCategories) {
  const tree = [];
  const map = {};

  // Спочатку створюємо мапу всіх категорій
  flatCategories.forEach(cat => {
    map[cat.categoryId] = { ...cat, children: [] };
  });

  // Потім розкладаємо їх по батьках
  flatCategories.forEach(cat => {
    if (cat.parentId && map[cat.parentId]) {
      map[cat.parentId].children.push(map[cat.categoryId]);
    } else {
      tree.push(map[cat.categoryId]); // Якщо немає батька, це коренева категорія
    }
  });

  return tree;
}
