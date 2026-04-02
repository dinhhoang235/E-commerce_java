package com.hoang.backend.modules.users.dto;

/**
 * DTO phản hồi trạng thái khả dụng của email.
 */

public record EmailAvailabilityResponse(String email, boolean available) {
}