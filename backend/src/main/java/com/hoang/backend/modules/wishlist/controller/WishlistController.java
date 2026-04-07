package com.hoang.backend.modules.wishlist.controller;

import com.hoang.backend.modules.wishlist.dto.AddToWishlistRequest;
import com.hoang.backend.modules.wishlist.dto.WishlistActionResponse;
import com.hoang.backend.modules.wishlist.dto.WishlistCheckResponse;
import com.hoang.backend.modules.wishlist.dto.WishlistCountResponse;
import com.hoang.backend.modules.wishlist.dto.WishlistResponse;
import com.hoang.backend.modules.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping(value = {"", "/"})
    public ResponseEntity<WishlistResponse> list(Authentication authentication) {
        return ResponseEntity.ok(wishlistService.getWishlist(authentication.getName()));
    }

    @PostMapping(value = {"/add_item", "/add_item/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WishlistActionResponse> addItem(
            Authentication authentication,
            @RequestBody AddToWishlistRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wishlistService.addItem(authentication.getName(), request));
    }

    @DeleteMapping(value = {"/remove_item", "/remove_item/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WishlistActionResponse> removeItem(
            Authentication authentication,
            @RequestBody AddToWishlistRequest request
    ) {
        return ResponseEntity.ok(wishlistService.removeItem(authentication.getName(), request == null ? null : request.product_id()));
    }

    @DeleteMapping(value = {"/clear", "/clear/"})
    public ResponseEntity<WishlistActionResponse> clear(Authentication authentication) {
        return ResponseEntity.ok(wishlistService.clear(authentication.getName()));
    }

    @GetMapping(value = {"/count", "/count/"})
    public ResponseEntity<WishlistCountResponse> count(Authentication authentication) {
        return ResponseEntity.ok(wishlistService.count(authentication.getName()));
    }

    @PostMapping(value = {"/toggle_item", "/toggle_item/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WishlistActionResponse> toggleItem(
            Authentication authentication,
            @RequestBody AddToWishlistRequest request
    ) {
        WishlistActionResponse response = wishlistService.toggleItem(authentication.getName(), request);
        if ("added".equals(response.action())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = {"/check_item", "/check_item/"})
    public ResponseEntity<WishlistCheckResponse> checkItem(
            Authentication authentication,
            @RequestParam(name = "product_id", required = false) Long productId
    ) {
        return ResponseEntity.ok(wishlistService.checkItem(authentication.getName(), productId));
    }
}
