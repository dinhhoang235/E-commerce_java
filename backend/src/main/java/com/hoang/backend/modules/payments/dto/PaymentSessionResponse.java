package com.hoang.backend.modules.payments.dto;

public record PaymentSessionResponse(
        String checkout_url,
        String session_id,
        String order_id
) {
}
