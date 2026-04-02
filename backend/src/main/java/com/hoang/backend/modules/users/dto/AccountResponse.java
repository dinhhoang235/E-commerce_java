package com.hoang.backend.modules.users.dto;

/**
 * DTO trả về thông tin account hiện tại của người dùng.
 */

public record AccountResponse(
        Long id,
        String username,
        String first_name,
        String last_name,
        String avatar,
        String email,
        String phone,
        AddressResponse address
) {
}