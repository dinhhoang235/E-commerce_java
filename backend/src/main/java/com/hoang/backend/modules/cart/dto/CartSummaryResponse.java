package com.hoang.backend.modules.cart.dto;

import java.math.BigDecimal;

public record CartSummaryResponse(
        int total_items,
        BigDecimal total_price
) {
}