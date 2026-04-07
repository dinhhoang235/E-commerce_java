package com.hoang.backend.modules.reviews.dto;

public record ReviewResponse(
        Long id,
        Long product,
        Long user,
        String user_name,
        String user_first_name,
        String user_last_name,
        int rating,
        String title,
        String comment,
        String created_at,
        String updated_at,
        boolean is_verified_purchase
) {
}
