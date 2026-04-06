package com.hoang.backend.modules.cart.dto;

public record UpdateCartItemRequest(
        Long item_id,
        Integer quantity
) {
}