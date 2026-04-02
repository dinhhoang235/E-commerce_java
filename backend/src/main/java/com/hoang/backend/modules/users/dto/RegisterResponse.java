package com.hoang.backend.modules.users.dto;

/**
 * DTO phản hồi sau khi đăng ký thành công.
 */

public record RegisterResponse(
        Long id,
        String username,
        String email,
        String first_name,
        String last_name,
        String phone,
        TokenResponse token
) {
}