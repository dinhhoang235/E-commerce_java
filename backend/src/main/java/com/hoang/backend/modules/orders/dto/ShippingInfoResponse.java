package com.hoang.backend.modules.orders.dto;

import java.math.BigDecimal;

public record ShippingInfoResponse(
        String address,
        String method,
        BigDecimal cost
) {
}
