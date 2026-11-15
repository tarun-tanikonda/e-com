package com.shopsphere.order.entity;

import java.util.EnumSet;
import java.util.Set;

/**
 * Lifecycle states of an Order according to FR-03:
 * PENDING, STOCK_RESERVED, PAYMENT_PENDING, CONFIRMED, FAILED, CANCELLED, REFUNDED.
 */
public enum OrderStatus {
    PENDING,
    STOCK_RESERVED,
    PAYMENT_PENDING,
    CONFIRMED,
    FAILED,
    CANCELLED,
    REFUNDED;

    /**
     * Determines whether transitioning from this status to nextStatus is permitted.
     */
    public boolean canTransitionTo(OrderStatus nextStatus) {
        if (this == nextStatus) {
            return true;
        }

        return switch (this) {
            case PENDING -> EnumSet.of(STOCK_RESERVED, FAILED, CANCELLED).contains(nextStatus);
            case STOCK_RESERVED -> EnumSet.of(PAYMENT_PENDING, CONFIRMED, FAILED, CANCELLED).contains(nextStatus);
            case PAYMENT_PENDING -> EnumSet.of(CONFIRMED, FAILED, CANCELLED).contains(nextStatus);
            case CONFIRMED -> EnumSet.of(CANCELLED, REFUNDED).contains(nextStatus);
            case CANCELLED -> EnumSet.of(REFUNDED).contains(nextStatus);
            case FAILED, REFUNDED -> false;
        };
    }
}
