package com.hoang.backend.modules.orders.dto;

public record ShippingAddressPayload(
        String first_name,
        String last_name,
        String phone,
        String address_line1,
        String city,
        String state,
        String zip_code,
        String country
) {
}
