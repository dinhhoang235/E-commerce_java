package com.hoang.backend.modules.cart.dto;

public record CartItemActionResponse(
        String message,
        CartItemResponse item
) {
}