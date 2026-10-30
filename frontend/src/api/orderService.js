import api from './axiosConfig';

export const orderService = {
  createOrder: async (orderRequestDto, idempotencyKey) => {
    // order-service endpoint is /api/v1/orders, which matches our axiosConfig baseURL
    const response = await api.post('/orders', orderRequestDto, {
      headers: { 'Idempotency-Key': idempotencyKey }
    });
    return response.data;
  }
};
