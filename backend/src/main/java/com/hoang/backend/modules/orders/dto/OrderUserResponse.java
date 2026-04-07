package com.hoang.backend.modules.orders.dto;

public record OrderUserResponse(
        Long id,
        String username,
        String first_name,
        String last_name,
        String email,
        OrderUserAccountResponse account
) {
}
