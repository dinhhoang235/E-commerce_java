package com.hoang.backend.modules.wishlist.dto;

import java.math.BigDecimal;
import java.util.List;

public record WishlistProductResponse(
        Long id,
        String name,
        BigDecimal price,
        String image,
        BigDecimal original_price,
        String badge,
        double rating,
        int reviews,
        String description,
        List<String> colors,
        List<String> storage
) {
}
