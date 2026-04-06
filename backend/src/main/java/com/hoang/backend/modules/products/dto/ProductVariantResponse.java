package com.hoang.backend.modules.products.dto;

import java.math.BigDecimal;

public record ProductVariantResponse(
        Long id,
        ProductShortResponse product,
        Long product_id,
        ProductColorResponse color,
        Long color_id,
        String storage,
        BigDecimal price,
        int stock,
        int sold,
        boolean is_in_stock,
        int total_stock,
        String created_at,
        String updated_at
) {
}
