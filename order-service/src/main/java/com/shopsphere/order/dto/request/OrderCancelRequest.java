package com.shopsphere.order.dto.request;

public class OrderCancelRequest {

    private String reason;

    public OrderCancelRequest() {
    }

    public OrderCancelRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
