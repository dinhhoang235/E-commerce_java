package com.hoang.backend.common.constants;

public final class PaymentStatus {
    public static final String PENDING = "pending";
    public static final String SUCCESS = "success";
    public static final String FAILED = "failed";
    public static final String REFUNDED = "refunded";
    public static final String EXPIRED = "expired";
    public static final String NO_PAYMENT = "no_payment";

    private PaymentStatus() {}
}
