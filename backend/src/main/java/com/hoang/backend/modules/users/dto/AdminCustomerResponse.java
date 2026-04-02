package com.hoang.backend.modules.users.dto;

/**
 * DTO dữ liệu khách hàng hiển thị cho màn hình admin.
 */

public record AdminCustomerResponse(
        Long id,
        String name,
        String email,
        String phone,
        String location,
        int orders,
        double totalSpent,
        String joinDate,
        String status
) {
}