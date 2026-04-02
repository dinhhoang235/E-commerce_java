package com.hoang.backend.modules.users.dto;

/**
 * DTO trả về dữ liệu địa chỉ cho frontend.
 */

public record AddressResponse(
        Long id,
        String first_name,
        String last_name,
        String phone,
        String address_line1,
        String city,
        String state,
        String zip_code,
        String country,
        String country_label,
        String created_at,
        boolean is_default,
        String email
) {
}