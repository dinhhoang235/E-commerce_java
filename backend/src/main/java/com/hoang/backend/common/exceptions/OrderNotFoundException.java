package com.hoang.backend.common.exceptions;

public class OrderNotFoundException extends IllegalArgumentException {
    public OrderNotFoundException(String orderId) {
        super("Order not found: " + orderId);
    }
}
