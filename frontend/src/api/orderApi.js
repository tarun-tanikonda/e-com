import axiosClient from './axiosClient';

export const orderApi = {
  /**
   * Place an order from cart items
   * @param {Object} data { items: [{ productId, sku, quantity, unitPrice, productName }], shippingAddress, currency }
   */
  createOrder: async (data) => {
    const idempotencyKey = `idemp-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;
    const response = await axiosClient.post('/api/v1/orders', data, {
      headers: {
        'Idempotency-Key': idempotencyKey,
      },
    });
    return response.data;
  },

  /**
   * Fetch authenticated user's order history
   * @param {Object} params { page, size, status, sortBy, sortDir }
   */
  getOrders: async (params = {}) => {
    const response = await axiosClient.get('/api/v1/orders', { params });
    return response.data;
  },

  /**
   * Get single order details by ID
   * @param {number|string} orderId
   */
  getOrderById: async (orderId) => {
    const response = await axiosClient.get(`/api/v1/orders/${orderId}`);
    return response.data;
  },

  /**
   * Request order cancellation
   * @param {number|string} orderId
   * @param {string} reason
   */
  cancelOrder: async (orderId, reason = 'Cancelled by customer') => {
    const response = await axiosClient.post(`/api/v1/orders/${orderId}/cancel`, {
      reason,
    });
    return response.data;
  },
};

export default orderApi;
