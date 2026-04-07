package com.hoang.backend.modules.wishlist.dto;

public record WishlistActionResponse(
        String message,
        WishlistItemResponse item,
        String action
) {
}
