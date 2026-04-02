package com.hoang.backend.modules.users.dto;

/**
 * DTO dữ liệu đăng ký tài khoản mới.
 */

public record RegisterRequest(
        String username,
        String password,
        String confirm_password,
        String email,
        String first_name,
        String last_name,
        String phone
) {
}