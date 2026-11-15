package com.shopsphere.order.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.shopsphere.order.dto.response.OrderLineResponse;
import com.shopsphere.order.dto.response.OrderResponse;
import com.shopsphere.order.dto.response.OrderStatusHistoryResponse;
import com.shopsphere.order.entity.Order;
import com.shopsphere.order.entity.OrderLine;
import com.shopsphere.order.entity.OrderStatusHistory;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }

        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setUserId(order.getUserId());
        response.setStatus(order.getStatus());
        response.setSubtotal(order.getSubtotal());
        response.setDiscount(order.getDiscount());
        response.setTax(order.getTax());
        response.setShipping(order.getShipping());
        response.setTotal(order.getTotal());
        response.setCurrency(order.getCurrency());
        response.setShippingAddress(order.getShippingAddress());
        response.setReservationId(order.getReservationId());
        response.setPaymentId(order.getPaymentId());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            response.setItems(toLineResponses(order.getItems()));
        } else {
            response.setItems(new ArrayList<>());
        }

        if (order.getStatusHistories() != null && !order.getStatusHistories().isEmpty()) {
            response.setStatusHistory(toHistoryResponses(order.getStatusHistories()));
        } else {
            response.setStatusHistory(new ArrayList<>());
        }

        return response;
    }

    public OrderLineResponse toLineResponse(OrderLine line) {
        if (line == null) {
            return null;
        }

        OrderLineResponse response = new OrderLineResponse();
        response.setId(line.getId());
        response.setProductId(line.getProductId());
        response.setSkuSnapshot(line.getSkuSnapshot());
        response.setNameSnapshot(line.getNameSnapshot());
        response.setUnitPrice(line.getUnitPrice());
        response.setQuantity(line.getQuantity());
        response.setLineTotal(line.getLineTotal());

        return response;
    }

    public List<OrderLineResponse> toLineResponses(List<OrderLine> lines) {
        if (lines == null) {
            return new ArrayList<>();
        }
        return lines.stream().map(this::toLineResponse).collect(Collectors.toList());
    }

    public OrderStatusHistoryResponse toHistoryResponse(OrderStatusHistory history) {
        if (history == null) {
            return null;
        }

        OrderStatusHistoryResponse response = new OrderStatusHistoryResponse();
        response.setId(history.getId());
        response.setFromStatus(history.getFromStatus());
        response.setToStatus(history.getToStatus());
        response.setReason(history.getReason());
        response.setOccurredAt(history.getOccurredAt());
        response.setActor(history.getActor());

        return response;
    }

    public List<OrderStatusHistoryResponse> toHistoryResponses(List<OrderStatusHistory> histories) {
        if (histories == null) {
            return new ArrayList<>();
        }
        return histories.stream().map(this::toHistoryResponse).collect(Collectors.toList());
    }
}
