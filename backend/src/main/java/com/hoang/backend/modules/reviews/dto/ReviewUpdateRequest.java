package com.hoang.backend.modules.reviews.dto;

public record ReviewUpdateRequest(
        Integer rating,
        String title,
        String comment
) {
}
