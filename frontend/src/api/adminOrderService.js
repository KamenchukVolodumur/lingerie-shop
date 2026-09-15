import api from './axiosConfig';

export const adminOrderService = {
  getAllOrders: async (page = 0, size = 20) => {
    const response = await api.get(`/admin/orders?page=${page}&size=${size}&sort=createdAt,desc`);
    return response.data;
  },
  
  confirmOrder: async (id) => {
    const response = await api.patch(`/admin/orders/${id}/confirm`);
    return response.data;
  },

  shipOrder: async (id) => {
    const response = await api.patch(`/admin/orders/${id}/ship`);
    return response.data;
  },

  completeOrder: async (id) => {
    const response = await api.patch(`/admin/orders/${id}/complete`);
    return response.data;
  },

  revertOrder: async (id) => {
    const response = await api.patch(`/admin/orders/${id}/revert`);
    return response.data;
  },

  payOrder: async (id) => {
    const response = await api.patch(`/admin/orders/${id}/pay`);
    return response.data;
  },

  cancelOrder: async (id) => {
    const response = await api.patch(`/admin/orders/${id}/cancel`);
    return response.data;
  }
};
