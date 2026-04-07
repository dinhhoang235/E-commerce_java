package com.hoang.backend.modules.wishlist.dto;

public record WishlistItemResponse(
        Long id,
        WishlistProductResponse product,
        String added_at
) {
}
