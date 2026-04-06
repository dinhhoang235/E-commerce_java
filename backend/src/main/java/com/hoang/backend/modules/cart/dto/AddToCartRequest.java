package com.hoang.backend.modules.cart.dto;

public record AddToCartRequest(
        Long product_variant_id,
        Integer quantity
) {
}