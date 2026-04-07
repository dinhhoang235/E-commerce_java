package com.hoang.backend.modules.reviews.dto;

public record ReviewCreateRequest(
        Long product,
        Integer rating,
        String title,
        String comment
) {
}
