package com.hoang.backend.modules.orders.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        String id,
        String customer,
        String email,
        List<String> products,
        BigDecimal total,
        BigDecimal subtotal,
        BigDecimal shipping_cost,
        BigDecimal total_with_shipping,
        String status,
        String date,
        ShippingInfoResponse shipping,
        List<OrderItemResponse> items,
        boolean is_paid,
        String payment_status,
        boolean has_pending_payment,
        boolean can_continue_payment,
        OrderUserResponse user
) {
}
