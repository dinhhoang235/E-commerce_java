package com.hoang.backend.modules.products.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        CategoryResponse category,
        Long category_id,
        String image,
        double rating,
        int reviews,
        String badge,
        String description,
        String full_description,
        List<String> features,
        List<ProductVariantResponse> variants,
        BigDecimal min_price,
        BigDecimal max_price,
        int total_stock,
        List<ProductColorResponse> available_colors,
        List<String> available_storages,
        String created_at,
        String updated_at
) {
}
