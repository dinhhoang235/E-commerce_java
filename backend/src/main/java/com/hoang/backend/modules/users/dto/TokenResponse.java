package com.hoang.backend.modules.users.dto;

/**
 * DTO chứa access token và refresh token.
 */

public record TokenResponse(String access, String refresh) {
}