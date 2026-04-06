package com.hoang.backend.modules.products.dto;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        String image,
        boolean is_active,
        int sort_order,
        Long parent,
        Long parent_id,
        int product_count,
        String created_at,
        String updated_at
) {
}
