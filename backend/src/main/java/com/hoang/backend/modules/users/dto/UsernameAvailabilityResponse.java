package com.hoang.backend.modules.users.dto;

/**
 * DTO phản hồi trạng thái khả dụng của username.
 */

public record UsernameAvailabilityResponse(String username, boolean available) {
}