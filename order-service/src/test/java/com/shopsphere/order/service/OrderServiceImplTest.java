package com.shopsphere.order.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.shopsphere.order.dto.request.OrderCancelRequest;
import com.shopsphere.order.dto.request.OrderCreateRequest;
import com.shopsphere.order.dto.request.OrderLineRequest;
import com.shopsphere.order.dto.request.OrderStatusUpdateRequest;
import com.shopsphere.order.dto.response.OrderResponse;
import com.shopsphere.order.dto.response.PagedResponse;
import com.shopsphere.order.entity.Order;
import com.shopsphere.order.entity.OrderIntegrationRecord;
import com.shopsphere.order.entity.OrderStatus;
import com.shopsphere.order.exception.InvalidOrderStateException;
import com.shopsphere.order.exception.OrderProcessingException;
import com.shopsphere.order.exception.UnauthorizedOrderAccessException;
import com.shopsphere.order.mapper.OrderMapper;
import com.shopsphere.order.repository.OrderIntegrationRecordRepository;
import com.shopsphere.order.repository.OrderLineRepository;
import com.shopsphere.order.repository.OrderRepository;
import com.shopsphere.order.repository.OrderStatusHistoryRepository;
import com.shopsphere.order.service.impl.OrderServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceImplTest {

    private OrderRepository orderRepository;
    private OrderLineRepository orderLineRepository;
    private OrderStatusHistoryRepository orderStatusHistoryRepository;
    private OrderIntegrationRecordRepository integrationRecordRepository;
    private OrderMapper orderMapper;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderLineRepository = mock(OrderLineRepository.class);
        orderStatusHistoryRepository = mock(OrderStatusHistoryRepository.class);
        integrationRecordRepository = mock(OrderIntegrationRecordRepository.class);
        orderMapper = new OrderMapper();

        orderService = new OrderServiceImpl(
                orderRepository,
                orderLineRepository,
                orderStatusHistoryRepository,
                integrationRecordRepository,
                orderMapper
        );
    }

    @Test
    @DisplayName("Should create order successfully with server-side calculation and immutable line snapshots")
    void shouldCreateOrderSuccessfully() {
        Long userId = 10L;
        OrderLineRequest lineReq = new OrderLineRequest(1L, "SKU-SHIRT-M", 2, new BigDecimal("500.00"), "Cotton Shirt");
        OrderCreateRequest request = new OrderCreateRequest(List.of(lineReq), "123 High Street, City");
        request.setShipping(new BigDecimal("50.00"));
        request.setTax(new BigDecimal("18.00"));
        request.setDiscount(new BigDecimal("20.00"));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            if (o.getId() == null) {
                o.setId(100L);
            }
            return o;
        });

        OrderResponse response = orderService.createOrder(userId, request, "IDEMP-KEY-1");

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals(OrderStatus.PENDING, response.getStatus());

        // subtotal = 2 * 500.00 = 1000.00
        assertEquals(new BigDecimal("1000.00"), response.getSubtotal());
        // total = 1000 - 20 + 50 + 18 = 1048.00
        assertEquals(new BigDecimal("1048.00"), response.getTotal());

        verify(orderRepository, atLeastOnce()).save(any(Order.class));
        verify(integrationRecordRepository).save(any(OrderIntegrationRecord.class));
    }

    @Test
    @DisplayName("Should return existing order when repeated idempotency key is submitted")
    void shouldReturnExistingOrderForRepeatedIdempotencyKey() {
        Long userId = 10L;
        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderLineRequest(1L, "SKU-1", 1, new BigDecimal("100.00"), "Item")), "Address");
        String key = "DUPLICATE-KEY";

        Order existing = new Order();
        existing.setId(200L);
        existing.setOrderNumber("ORD-200");
        existing.setUserId(userId);
        existing.setStatus(OrderStatus.CONFIRMED);
        existing.setSubtotal(new BigDecimal("100.00"));
        existing.setTotal(new BigDecimal("100.00"));

        OrderIntegrationRecord record = new OrderIntegrationRecord(200L, "ORDER_PLACEMENT", key, "SUCCESS", "Created");
        when(integrationRecordRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(record));
        when(orderRepository.findById(200L)).thenReturn(Optional.of(existing));

        OrderResponse response = orderService.createOrder(userId, request, key);

        assertNotNull(response);
        assertEquals(200L, response.getId());
        assertEquals("ORD-200", response.getOrderNumber());
    }

    @Test
    @DisplayName("Should fail when unit price is missing or invalid")
    void shouldFailWhenPriceIsInvalid() {
        Long userId = 10L;
        OrderLineRequest lineReq = new OrderLineRequest(1L, "SKU-BAD", 1, null, "Item");
        OrderCreateRequest request = new OrderCreateRequest(List.of(lineReq), "Address");

        assertThrows(OrderProcessingException.class, () -> orderService.createOrder(userId, request, null));
    }

    @Test
    @DisplayName("Should retrieve only the authenticated customer's orders unless admin")
    void shouldGetCustomerOrders() {
        Long userId = 10L;
        Order o = new Order();
        o.setId(1L);
        o.setOrderNumber("ORD-1");
        o.setUserId(userId);
        o.setStatus(OrderStatus.CONFIRMED);
        o.setSubtotal(new BigDecimal("100.00"));
        o.setTotal(new BigDecimal("100.00"));

        Page<Order> page = new PageImpl<>(List.of(o));
        when(orderRepository.findByUserId(eq(userId), any(Pageable.class))).thenReturn(page);

        PagedResponse<OrderResponse> result = orderService.getOrders(userId, "CUSTOMER", null, 0, 10, "createdAt", "desc");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("ORD-1", result.getContent().get(0).getOrderNumber());
    }

    @Test
    @DisplayName("Should prevent customer from viewing another customer's order")
    void shouldDenyAccessToOtherCustomerOrder() {
        Long ownerId = 10L;
        Long otherUserId = 20L;

        Order o = new Order();
        o.setId(50L);
        o.setUserId(ownerId);

        when(orderRepository.findById(50L)).thenReturn(Optional.of(o));

        assertThrows(UnauthorizedOrderAccessException.class, () ->
                orderService.getOrderById(50L, otherUserId, "CUSTOMER")
        );
    }

    @Test
    @DisplayName("Should cancel order successfully")
    void shouldCancelOrder() {
        Long userId = 10L;
        Order order = new Order();
        order.setId(100L);
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(new BigDecimal("250.00"));
        order.setSubtotal(new BigDecimal("250.00"));

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse cancelledResponse = orderService.cancelOrder(100L, userId, "CUSTOMER", new OrderCancelRequest("Changed my mind"));

        assertNotNull(cancelledResponse);
        assertEquals(OrderStatus.CANCELLED, cancelledResponse.getStatus());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should reject illegal order state transitions")
    void shouldRejectIllegalStateTransition() {
        Order order = new Order();
        order.setId(100L);
        order.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderStatus.CONFIRMED, "Force confirm");

        assertThrows(InvalidOrderStateException.class, () ->
                orderService.updateOrderStatus(100L, request, "ADMIN")
        );
    }
}
