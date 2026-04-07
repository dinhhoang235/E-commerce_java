package com.hoang.backend.modules.payments.dto;

public record CreateCheckoutSessionRequest(
        String order_id
) {
}
