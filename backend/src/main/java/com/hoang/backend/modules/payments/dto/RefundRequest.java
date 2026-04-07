package com.hoang.backend.modules.payments.dto;

public record RefundRequest(
        String order_id,
        String reason
) {
}
