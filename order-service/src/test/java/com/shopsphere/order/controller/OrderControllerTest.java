package com.shopsphere.order.controller;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.shopsphere.order.dto.request.OrderCancelRequest;
import com.shopsphere.order.dto.request.OrderCreateRequest;
import com.shopsphere.order.dto.request.OrderLineRequest;
import com.shopsphere.order.dto.request.OrderStatusUpdateRequest;
import com.shopsphere.order.dto.response.OrderResponse;
import com.shopsphere.order.dto.response.PagedResponse;
import com.shopsphere.order.entity.OrderStatus;
import com.shopsphere.order.service.OrderService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderControllerTest {

    private OrderService orderService;
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        orderController = new OrderController(orderService);
    }

    @Test
    @DisplayName("POST /api/v1/orders should return 201 Created with order response")
    void shouldPlaceOrderSuccessfully() {
        Long userId = 10L;
        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderLineRequest(1L, "SKU-1", 2, new BigDecimal("100.00"), "Item")),
                "123 Delivery Road"
        );

        OrderResponse expectedResponse = new OrderResponse();
        expectedResponse.setId(100L);
        expectedResponse.setOrderNumber("ORD-100");
        expectedResponse.setUserId(userId);
        expectedResponse.setStatus(OrderStatus.PAYMENT_PENDING);
        expectedResponse.setTotal(new BigDecimal("200.00"));

        when(orderService.createOrder(eq(userId), eq(request), eq("IDEMP-KEY-1"))).thenReturn(expectedResponse);

        ResponseEntity<OrderResponse> response = orderController.placeOrder(userId, "IDEMP-KEY-1", request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ORD-100", response.getBody().getOrderNumber());
        assertEquals(OrderStatus.PAYMENT_PENDING, response.getBody().getStatus());
        verify(orderService).createOrder(eq(userId), eq(request), eq("IDEMP-KEY-1"));
    }

    @Test
    @DisplayName("GET /api/v1/orders should return 200 OK with paginated list")
    void shouldGetOrdersWithPagination() {
        Long userId = 10L;
        PagedResponse<OrderResponse> pagedResponse = new PagedResponse<>(List.of(new OrderResponse()), 0, 10, 1, 1, true);

        when(orderService.getOrders(eq(userId), eq("CUSTOMER"), eq(null), eq(0), eq(10), eq("createdAt"), eq("desc")))
                .thenReturn(pagedResponse);

        ResponseEntity<PagedResponse<OrderResponse>> response = orderController.getOrders(
                userId, "CUSTOMER", null, 0, 10, "createdAt", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} should return 200 OK with order details")
    void shouldGetOrderById() {
        Long orderId = 100L;
        Long userId = 10L;

        OrderResponse expectedResponse = new OrderResponse();
        expectedResponse.setId(orderId);
        expectedResponse.setOrderNumber("ORD-100");

        when(orderService.getOrderById(orderId, userId, "CUSTOMER")).thenReturn(expectedResponse);

        ResponseEntity<OrderResponse> response = orderController.getOrderById(orderId, userId, "CUSTOMER");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(orderId, response.getBody().getId());
    }

    @Test
    @DisplayName("POST /api/v1/orders/{orderId}/cancel should return 200 OK with cancelled order")
    void shouldCancelOrder() {
        Long orderId = 100L;
        Long userId = 10L;
        OrderCancelRequest cancelReq = new OrderCancelRequest("Mistaken order");

        OrderResponse cancelledResponse = new OrderResponse();
        cancelledResponse.setId(orderId);
        cancelledResponse.setStatus(OrderStatus.CANCELLED);

        when(orderService.cancelOrder(orderId, userId, "CUSTOMER", cancelReq)).thenReturn(cancelledResponse);

        ResponseEntity<OrderResponse> response = orderController.cancelOrder(orderId, userId, "CUSTOMER", cancelReq);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(OrderStatus.CANCELLED, response.getBody().getStatus());
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{orderId}/status should return 200 OK with updated order status")
    void shouldUpdateOrderStatus() {
        Long orderId = 100L;
        OrderStatusUpdateRequest updateReq = new OrderStatusUpdateRequest(OrderStatus.CONFIRMED, "Payment succeeded");

        OrderResponse confirmedResponse = new OrderResponse();
        confirmedResponse.setId(orderId);
        confirmedResponse.setStatus(OrderStatus.CONFIRMED);

        when(orderService.updateOrderStatus(orderId, updateReq, "ADMIN")).thenReturn(confirmedResponse);

        ResponseEntity<OrderResponse> response = orderController.updateOrderStatus(orderId, "ADMIN", updateReq);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(OrderStatus.CONFIRMED, response.getBody().getStatus());
    }
}
