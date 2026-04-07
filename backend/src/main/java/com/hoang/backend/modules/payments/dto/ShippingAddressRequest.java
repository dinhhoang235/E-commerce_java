package com.hoang.backend.modules.payments.dto;

public record ShippingAddressRequest(
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        String city,
        String state,
        String zipCode,
        String country
) {
}
