package com.hoang.backend.modules.cart.dto;

import com.hoang.backend.modules.products.dto.ProductVariantResponse;
import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        ProductVariantResponse product_variant,
        Long product_variant_id,
        int quantity,
        BigDecimal total_price,
        String created_at
) {
}