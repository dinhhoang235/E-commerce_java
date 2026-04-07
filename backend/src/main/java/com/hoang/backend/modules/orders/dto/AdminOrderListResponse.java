package com.hoang.backend.modules.orders.dto;

import java.util.List;

public record AdminOrderListResponse(
        long count,
        List<OrderResponse> results
) {
}
