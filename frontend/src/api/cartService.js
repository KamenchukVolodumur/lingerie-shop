import axios from 'axios';

// cart-service endpoint is /api/carts (no v1)
export const cartService = {
  addItem: async (cartId, productId, productVariantId, quantity) => {
    const response = await axios.post(`/api/carts/${cartId}/items`, {
      productId,
      productVariantId,
      quantity
    });
    return response.data;
  },
  
  // Just in case we need it
  getCart: async (cartId) => {
    const response = await axios.get(`/api/carts/${cartId}`);
    return response.data;
  }
};
