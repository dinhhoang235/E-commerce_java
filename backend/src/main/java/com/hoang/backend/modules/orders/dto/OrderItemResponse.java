package com.hoang.backend.modules.orders.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long product_variant,
        String product_variant_name,
        String product_variant_color,
        String product_variant_storage,
        int quantity,
        BigDecimal price,
        BigDecimal product_variant_price,
        String product_variant_image
) {
}
