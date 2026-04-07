package com.hoang.backend.modules.orders.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderStatsResponse(
        long total_orders,
        long pending_orders,
        long processing_orders,
        long shipped_orders,
        long completed_orders,
        BigDecimal total_revenue,
        Long recent_orders_30_days,
        BigDecimal recent_revenue_30_days,
        List<StatusCountResponse> status_breakdown,
        BigDecimal average_order_value
) {
}
