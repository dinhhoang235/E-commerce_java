package com.hoang.backend.modules.orders.dto;

public record StatusCountResponse(
        String status,
        long count
) {
}
