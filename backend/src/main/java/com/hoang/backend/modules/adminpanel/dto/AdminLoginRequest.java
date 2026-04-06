package com.hoang.backend.modules.adminpanel.dto;

/**
 * Payload đăng nhập cho admin panel.
 */
public record AdminLoginRequest(String email, String password) {
}
