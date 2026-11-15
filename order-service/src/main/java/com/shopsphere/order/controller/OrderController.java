package com.shopsphere.order.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopsphere.order.dto.request.OrderCancelRequest;
import com.shopsphere.order.dto.request.OrderCreateRequest;
import com.shopsphere.order.dto.request.OrderStatusUpdateRequest;
import com.shopsphere.order.dto.response.OrderResponse;
import com.shopsphere.order.dto.response.PagedResponse;
import com.shopsphere.order.entity.OrderStatus;
import com.shopsphere.order.service.OrderService;

import jakarta.validation.Valid;

/**
 * REST controller for Order operations matching ShopSphere SRS v1.0.
 * Serves routes:
 *  - POST   /api/v1/orders
 *  - GET    /api/v1/orders
 *  - GET    /api/v1/orders/{orderId}
 *  - POST   /api/v1/orders/{orderId}/cancel
 *  - PATCH  /api/v1/orders/{orderId}/status
 */
@RestController
@RequestMapping({"/api/v1/orders", "/orders"})
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * FR-01: Place an order from selected cart/items.
     */
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody OrderCreateRequest request) {

        Long effectiveUserId = resolveUserId(userId);
        log.info("Received place order request for userId={}", effectiveUserId);

        OrderResponse response = orderService.createOrder(effectiveUserId, request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * FR-06: List the authenticated customer's orders with pagination.
     */
    @GetMapping
    public ResponseEntity<PagedResponse<OrderResponse>> getOrders(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Long effectiveUserId = resolveUserId(userId);
        PagedResponse<OrderResponse> response = orderService.getOrders(
                effectiveUserId, userRole, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    /**
     * FR-06: Retrieve an order subject to ownership/role checks.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        Long effectiveUserId = resolveUserId(userId);
        OrderResponse response = orderService.getOrderById(orderId, effectiveUserId, userRole);
        return ResponseEntity.ok(response);
    }

    /**
     * FR-07: Request cancellation if allowed by current state.
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestBody(required = false) OrderCancelRequest request) {

        Long effectiveUserId = resolveUserId(userId);
        OrderResponse response = orderService.cancelOrder(orderId, effectiveUserId, userRole, request);
        return ResponseEntity.ok(response);
    }

    /**
     * FR-03: Controlled status update (Internal / Admin).
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody OrderStatusUpdateRequest request) {

        String actor = (userRole != null && !userRole.isBlank()) ? userRole : "ADMIN";
        OrderResponse response = orderService.updateOrderStatus(orderId, request, actor);
        return ResponseEntity.ok(response);
    }

    private Long resolveUserId(Long headerUserId) {
        if (headerUserId != null) {
            return headerUserId;
        }
        // In local development without gateway, default to customer id 1
        return 1L;
    }
}
