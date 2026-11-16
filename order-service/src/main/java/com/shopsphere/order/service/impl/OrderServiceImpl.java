package com.shopsphere.order.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopsphere.order.dto.request.OrderCancelRequest;
import com.shopsphere.order.dto.request.OrderCreateRequest;
import com.shopsphere.order.dto.request.OrderLineRequest;
import com.shopsphere.order.dto.request.OrderStatusUpdateRequest;
import com.shopsphere.order.dto.response.OrderResponse;
import com.shopsphere.order.dto.response.PagedResponse;
import com.shopsphere.order.entity.Order;
import com.shopsphere.order.entity.OrderIntegrationRecord;
import com.shopsphere.order.entity.OrderLine;
import com.shopsphere.order.entity.OrderStatus;
import com.shopsphere.order.exception.InvalidOrderStateException;
import com.shopsphere.order.exception.OrderProcessingException;
import com.shopsphere.order.exception.ResourceNotFoundException;
import com.shopsphere.order.exception.UnauthorizedOrderAccessException;
import com.shopsphere.order.mapper.OrderMapper;
import com.shopsphere.order.repository.OrderIntegrationRecordRepository;
import com.shopsphere.order.repository.OrderLineRepository;
import com.shopsphere.order.repository.OrderRepository;
import com.shopsphere.order.repository.OrderStatusHistoryRepository;
import com.shopsphere.order.service.OrderService;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderIntegrationRecordRepository integrationRecordRepository;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderLineRepository orderLineRepository,
                            OrderStatusHistoryRepository orderStatusHistoryRepository,
                            OrderIntegrationRecordRepository integrationRecordRepository,
                            OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderLineRepository = orderLineRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.integrationRecordRepository = integrationRecordRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    public OrderResponse createOrder(Long userId, OrderCreateRequest request, String idempotencyKey) {
        if (userId == null) {
            throw new IllegalArgumentException("Authenticated user ID cannot be null");
        }
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order items cannot be empty");
        }

        String effectiveIdempotencyKey = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? idempotencyKey : request.getIdempotencyKey();

        // 1. Idempotency Check (FR-01, Business Rules)
        if (effectiveIdempotencyKey != null && !effectiveIdempotencyKey.isBlank()) {
            Optional<OrderIntegrationRecord> existingRecord = integrationRecordRepository.findByIdempotencyKey(effectiveIdempotencyKey);
            if (existingRecord.isPresent()) {
                log.info("Idempotent replay detected for key: {}", effectiveIdempotencyKey);
                Order existingOrder = orderRepository.findById(existingRecord.get().getOrderId())
                        .orElseThrow(() -> new ResourceNotFoundException("Order associated with idempotency key not found"));
                return orderMapper.toResponse(existingOrder);
            }
        }

        // 2. Assemble Order & OrderLines with immutable snapshots (FR-01, FR-02)
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
        order.setShippingAddress(request.getShippingAddress());

        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderLineRequest itemReq : request.getItems()) {
            BigDecimal unitPrice = itemReq.getUnitPrice();
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new OrderProcessingException("A valid positive unit price is required for SKU: " + itemReq.getSku());
            }

            String nameSnapshot = (itemReq.getProductName() != null && !itemReq.getProductName().isBlank())
                    ? itemReq.getProductName()
                    : "Product-" + itemReq.getSku();

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineTotal);

            OrderLine line = new OrderLine(
                    order,
                    itemReq.getProductId(),
                    itemReq.getSku(),
                    nameSnapshot,
                    unitPrice.setScale(2, RoundingMode.HALF_UP),
                    itemReq.getQuantity(),
                    lineTotal
            );
            order.addOrderLine(line);
        }

        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        BigDecimal shipping = request.getShipping() != null ? request.getShipping() : BigDecimal.ZERO;
        BigDecimal tax = request.getTax() != null ? request.getTax() : BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(discount).add(shipping).add(tax).setScale(2, RoundingMode.HALF_UP);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        order.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        order.setDiscount(discount.setScale(2, RoundingMode.HALF_UP));
        order.setShipping(shipping.setScale(2, RoundingMode.HALF_UP));
        order.setTax(tax.setScale(2, RoundingMode.HALF_UP));
        order.setTotal(total);

        // Record initial status
        order.addStatusHistory(null, OrderStatus.PENDING, "Order placed by customer", "CUSTOMER");
        order = orderRepository.save(order);

        // 3. Record idempotency if key was provided
        if (effectiveIdempotencyKey != null && !effectiveIdempotencyKey.isBlank()) {
            OrderIntegrationRecord record = new OrderIntegrationRecord(
                    order.getId(),
                    "ORDER_PLACEMENT",
                    effectiveIdempotencyKey,
                    "SUCCESS",
                    "Order created successfully: " + order.getOrderNumber()
            );
            integrationRecordRepository.save(record);
        }

        log.info("Order successfully created: id={}, orderNumber={}, total={}", order.getId(), order.getOrderNumber(), order.getTotal());
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getOrders(Long userId, String userRole, OrderStatus status,
                                                  int page, int size, String sortBy, String sortDir) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortProperty = (sortBy != null && !sortBy.isBlank()) ? sortBy : "createdAt";
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProperty));

        Page<Order> orderPage;
        boolean isAdmin = userRole != null && (userRole.equalsIgnoreCase("ADMIN") || userRole.equalsIgnoreCase("ROLE_ADMIN"));

        if (isAdmin) {
            orderPage = (status != null)
                    ? orderRepository.findByStatus(status, pageable)
                    : orderRepository.findAll(pageable);
        } else {
            if (userId == null) {
                throw new UnauthorizedOrderAccessException("User ID is required to fetch orders");
            }
            orderPage = (status != null)
                    ? orderRepository.findByUserIdAndStatus(userId, status, pageable)
                    : orderRepository.findByUserId(userId, pageable);
        }

        List<OrderResponse> content = orderPage.getContent().stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long userId, String userRole) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        boolean isAdmin = userRole != null && (userRole.equalsIgnoreCase("ADMIN") || userRole.equalsIgnoreCase("ROLE_ADMIN"));
        if (!isAdmin && !order.getUserId().equals(userId)) {
            throw new UnauthorizedOrderAccessException("You do not have permission to view order id: " + orderId);
        }

        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponse cancelOrder(Long orderId, Long userId, String userRole, OrderCancelRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        boolean isAdmin = userRole != null && (userRole.equalsIgnoreCase("ADMIN") || userRole.equalsIgnoreCase("ROLE_ADMIN"));
        if (!isAdmin && !order.getUserId().equals(userId)) {
            throw new UnauthorizedOrderAccessException("You do not have permission to cancel order id: " + orderId);
        }

        OrderStatus currentStatus = order.getStatus();
        if (!currentStatus.canTransitionTo(OrderStatus.CANCELLED)) {
            throw new InvalidOrderStateException("Order with status " + currentStatus + " cannot be cancelled");
        }

        String reason = (request != null && request.getReason() != null && !request.getReason().isBlank())
                ? request.getReason() : "Order cancelled by " + (isAdmin ? "ADMIN" : "CUSTOMER");

        order.setStatus(OrderStatus.CANCELLED);
        order.addStatusHistory(currentStatus, OrderStatus.CANCELLED, reason, isAdmin ? "ADMIN" : "CUSTOMER");

        order = orderRepository.save(order);
        log.info("Order id={} successfully cancelled. Reason: {}", orderId, reason);
        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request, String actor) {
        if (request == null || request.getStatus() == null) {
            throw new IllegalArgumentException("Target order status is required");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        OrderStatus fromStatus = order.getStatus();
        OrderStatus toStatus = request.getStatus();

        if (!fromStatus.canTransitionTo(toStatus)) {
            throw new InvalidOrderStateException("Illegal transition from " + fromStatus + " to " + toStatus);
        }

        String reason = (request.getReason() != null && !request.getReason().isBlank())
                ? request.getReason() : "Order status updated to " + toStatus;

        order.setStatus(toStatus);
        order.addStatusHistory(fromStatus, toStatus, reason, (actor != null && !actor.isBlank()) ? actor : "SYSTEM");

        order = orderRepository.save(order);
        log.info("Order id={} status transitioned from {} to {}", orderId, fromStatus, toStatus);
        return orderMapper.toResponse(order);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
