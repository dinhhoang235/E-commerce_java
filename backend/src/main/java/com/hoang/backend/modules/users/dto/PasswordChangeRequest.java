package com.hoang.backend.modules.users.dto;

/**
 * DTO dữ liệu đổi mật khẩu.
 */

public record PasswordChangeRequest(String old_password, String new_password, String confirm_password) {
}