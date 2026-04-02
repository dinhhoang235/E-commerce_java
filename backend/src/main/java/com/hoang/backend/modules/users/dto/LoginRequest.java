package com.hoang.backend.modules.users.dto;

/**
 * DTO dữ liệu đăng nhập bằng username hoặc email.
 */

public record LoginRequest(String username_or_email, String password) {
}