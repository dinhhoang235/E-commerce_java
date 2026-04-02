package com.hoang.backend.modules.users.dto;

/**
 * DTO nhận dữ liệu địa chỉ từ client.
 */

public record AddressRequest(
        String first_name,
        String last_name,
        String phone,
        String address_line1,
        String city,
        String state,
        String zip_code,
        String country,
        Boolean is_default
) {
}