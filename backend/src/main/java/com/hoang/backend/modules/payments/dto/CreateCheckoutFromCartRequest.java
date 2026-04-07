package com.hoang.backend.modules.payments.dto;

import java.util.List;

public record CreateCheckoutFromCartRequest(
        List<CartPaymentItemRequest> cart_items,
        ShippingAddressRequest shipping_address,
        String shipping_method
) {
}
