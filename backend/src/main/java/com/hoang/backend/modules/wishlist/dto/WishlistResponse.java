package com.hoang.backend.modules.wishlist.dto;

import java.util.List;

public record WishlistResponse(
        Long id,
        List<WishlistItemResponse> items,
        long total_items,
        String created_at,
        String updated_at
) {
}
