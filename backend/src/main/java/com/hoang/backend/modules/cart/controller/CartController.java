package com.hoang.backend.modules.cart.controller;

import com.hoang.backend.modules.cart.dto.AddToCartRequest;
import com.hoang.backend.modules.cart.dto.CartCountResponse;
import com.hoang.backend.modules.cart.dto.CartItemActionResponse;
import com.hoang.backend.modules.cart.dto.CartResponse;
import com.hoang.backend.modules.cart.dto.CartSummaryResponse;
import com.hoang.backend.modules.cart.dto.RemoveCartItemRequest;
import com.hoang.backend.modules.cart.dto.UpdateCartItemRequest;
import com.hoang.backend.modules.cart.service.CartService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @GetMapping(value = {"", "/"})
    public ResponseEntity<CartResponse> list(Authentication authentication) {
        return ResponseEntity.ok(cartService.getCart(authentication.getName()));
    }

    @PostMapping(value = {"/add_item", "/add_item/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CartItemActionResponse> addItem(Authentication authentication, @RequestBody AddToCartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(authentication.getName(), request));
    }

    @PutMapping(value = {"/update_item", "/update_item/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CartItemActionResponse> updateItem(Authentication authentication, @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItem(authentication.getName(), request));
    }

    @DeleteMapping(value = {"/remove_item", "/remove_item/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> removeItem(Authentication authentication, @RequestBody RemoveCartItemRequest request) {
        cartService.removeItem(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of("message", "Item removed from cart successfully"));
    }

    @DeleteMapping(value = {"/clear", "/clear/"})
    public ResponseEntity<Map<String, String>> clear(Authentication authentication) {
        cartService.clearCart(authentication.getName());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of("message", "Cart cleared successfully"));
    }

    @GetMapping(value = {"/count", "/count/"})
    public ResponseEntity<CartCountResponse> count(Authentication authentication) {
        return ResponseEntity.ok(cartService.count(authentication.getName()));
    }

    @GetMapping(value = {"/summary", "/summary/"})
    public ResponseEntity<CartSummaryResponse> summary(Authentication authentication) {
        return ResponseEntity.ok(cartService.summary(authentication.getName()));
    }
}