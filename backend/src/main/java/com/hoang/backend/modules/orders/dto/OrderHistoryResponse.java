package com.hoang.backend.modules.orders.dto;

import java.util.List;

public record OrderHistoryResponse(
        List<OrderResponse> orders,
        long total,
        int page,
        int page_size,
        boolean has_next
) {
}
