package com.shopsphere.order.service;

import com.shopsphere.order.dto.request.OrderCancelRequest;
import com.shopsphere.order.dto.request.OrderCreateRequest;
import com.shopsphere.order.dto.request.OrderStatusUpdateRequest;
import com.shopsphere.order.dto.response.OrderResponse;
import com.shopsphere.order.dto.response.PagedResponse;
import com.shopsphere.order.entity.OrderStatus;

public interface OrderService {

    OrderResponse createOrder(Long userId, OrderCreateRequest request, String idempotencyKey);

    PagedResponse<OrderResponse> getOrders(Long userId, String userRole, OrderStatus status,
                                          int page, int size, String sortBy, String sortDir);

    OrderResponse getOrderById(Long orderId, Long userId, String userRole);

    OrderResponse cancelOrder(Long orderId, Long userId, String userRole, OrderCancelRequest request);

    OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request, String actor);
}
