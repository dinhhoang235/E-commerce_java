package com.hoang.backend.common.constants;

public final class OrderStatus {
    public static final String PENDING = "pending";
    public static final String PROCESSING = "processing";
    public static final String SHIPPED = "shipped";
    public static final String COMPLETED = "completed";
    public static final String CANCELLED = "cancelled";
    public static final String REFUNDED = "refunded";

    private OrderStatus() {}
}
