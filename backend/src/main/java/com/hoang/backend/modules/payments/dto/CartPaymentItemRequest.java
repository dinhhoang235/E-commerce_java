package com.hoang.backend.modules.payments.dto;

public record CartPaymentItemRequest(
        Long product_id,
        String name,
        Integer quantity,
        String price,
        String description
) {
}
