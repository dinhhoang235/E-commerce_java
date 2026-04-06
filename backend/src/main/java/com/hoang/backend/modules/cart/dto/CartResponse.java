package com.hoang.backend.modules.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long id,
        List<CartItemResponse> items,
        int total_items,
        BigDecimal total_price,
        String created_at,
        String updated_at
) {
}