package com.hoang.backend.modules.orders.dto;

public record OrderCreateItemRequest(
        Long product_variant_id,
        Integer quantity
) {
}
