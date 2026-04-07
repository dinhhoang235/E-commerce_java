package com.hoang.backend.modules.orders.dto;

import java.util.List;

public record OrderCreateRequest(
        Long shipping_address_id,
        ShippingAddressPayload shipping_address,
        String shipping_method,
        List<OrderCreateItemRequest> items
) {
}
