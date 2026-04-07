package com.hoang.backend.modules.reviews.controller;

import com.hoang.backend.modules.reviews.dto.ReviewCreateRequest;
import com.hoang.backend.modules.reviews.dto.ReviewResponse;
import com.hoang.backend.modules.reviews.dto.ReviewUpdateRequest;
import com.hoang.backend.modules.reviews.service.ReviewService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping(value = {"", "/"})
    public ResponseEntity<List<ReviewResponse>> list(@RequestParam(name = "product_id", required = false) Long productId) {
        return ResponseEntity.ok(reviewService.list(Optional.ofNullable(productId)));
    }

    @GetMapping(value = {"/product_reviews", "/product_reviews/"})
    public ResponseEntity<?> productReviews(@RequestParam(name = "product_id", required = false) Long productId) {
        if (productId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "product_id parameter is required."));
        }
        return ResponseEntity.ok(reviewService.list(Optional.of(productId)));
    }

    @PostMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReviewResponse> create(Authentication authentication, @RequestBody ReviewCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(authentication.getName(), request));
    }

    @PutMapping(value = {"/{id}", "/{id}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReviewResponse> update(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody ReviewUpdateRequest request
    ) {
        return ResponseEntity.ok(reviewService.update(authentication.getName(), id, request));
    }

    @PatchMapping(value = {"/{id}", "/{id}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReviewResponse> partialUpdate(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody ReviewUpdateRequest request
    ) {
        return ResponseEntity.ok(reviewService.update(authentication.getName(), id, request));
    }

    @DeleteMapping(value = {"/{id}", "/{id}/"})
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        reviewService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
