package com.hoang.backend.modules.adminpanel.dto;

/**
 * Thông tin admin trả về sau khi xác thực thành công.
 */
public record AdminLoginResponse(
        Long id,
        String email,
        String name,
        String role
) {
}
