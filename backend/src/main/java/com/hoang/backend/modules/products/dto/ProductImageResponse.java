package com.hoang.backend.modules.products.dto;

public record ProductImageResponse(
        Long id,
        Long product_id,
        String image_url,
        int sort_order,
        boolean is_primary,
        String created_at,
        String updated_at
) {
}
