package com.hoang.backend.modules.reviews.service;

import com.hoang.backend.modules.products.entity.Product;
import com.hoang.backend.modules.products.repository.ProductRepository;
import com.hoang.backend.modules.reviews.dto.ReviewCreateRequest;
import com.hoang.backend.modules.reviews.dto.ReviewResponse;
import com.hoang.backend.modules.reviews.dto.ReviewUpdateRequest;
import com.hoang.backend.modules.reviews.entity.Review;
import com.hoang.backend.modules.reviews.repository.ReviewRepository;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public List<ReviewResponse> list(Optional<Long> productId) {
        List<Review> reviews = productId
                .map(reviewRepository::findByProductIdOrderByCreatedAtDesc)
                .orElseGet(reviewRepository::findAllByOrderByCreatedAtDesc);

        return reviews.stream().map(this::toResponse).toList();
    }

    public ReviewResponse create(String authenticatedUsername, ReviewCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        Long productId = request.product();
        if (productId == null) {
            throw new IllegalArgumentException("product is required.");
        }

        validateRating(request.rating());
        String title = requireNonBlank(request.title(), "title is required.");
        String comment = requireNonBlank(request.comment(), "comment is required.");

        AppUser user = requireUser(authenticatedUsername);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        if (reviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new IllegalArgumentException("You have already reviewed this product.");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.rating());
        review.setTitle(title);
        review.setComment(comment);
        review.setIsVerifiedPurchase(false);

        Review saved = reviewRepository.save(review);
        updateProductRating(product);
        return toResponse(saved);
    }

    public ReviewResponse update(String authenticatedUsername, Long reviewId, ReviewUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        AppUser user = requireUser(authenticatedUsername);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found."));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only update your own reviews.");
        }

        if (request.rating() != null) {
            validateRating(request.rating());
            review.setRating(request.rating());
        }
        if (request.title() != null) {
            review.setTitle(requireNonBlank(request.title(), "title must not be blank."));
        }
        if (request.comment() != null) {
            review.setComment(requireNonBlank(request.comment(), "comment must not be blank."));
        }

        Review saved = reviewRepository.save(review);
        updateProductRating(saved.getProduct());
        return toResponse(saved);
    }

    public void delete(String authenticatedUsername, Long reviewId) {
        AppUser user = requireUser(authenticatedUsername);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found."));

        boolean owner = review.getUser().getId().equals(user.getId());
        boolean admin = Boolean.TRUE.equals(user.getIsStaff());
        if (!owner && !admin) {
            throw new IllegalArgumentException("You can only delete your own reviews.");
        }

        Product product = review.getProduct();
        reviewRepository.delete(review);
        updateProductRating(product);
    }

    private AppUser requireUser(String authenticatedUsername) {
        return appUserRepository.findByUsernameIgnoreCase(authenticatedUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private void validateRating(Integer rating) {
        if (rating == null) {
            throw new IllegalArgumentException("rating is required.");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5.");
        }
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private void updateProductRating(Product product) {
        long totalReviews = reviewRepository.countByProductId(product.getId());
        Double average = reviewRepository.findAverageRatingByProductId(product.getId());

        product.setReviews((int) totalReviews);
        product.setRating(average == null ? 0.0 : average);
        productRepository.save(product);
    }

    private ReviewResponse toResponse(Review review) {
        AppUser user = review.getUser();
        return new ReviewResponse(
                review.getId(),
                review.getProduct().getId(),
                user.getId(),
                safe(user.getUsername()),
                safe(user.getFirstName()),
                safe(user.getLastName()),
                review.getRating(),
                safe(review.getTitle()),
                safe(review.getComment()),
                formatTime(review.getCreatedAt()),
                formatTime(review.getUpdatedAt()),
                Boolean.TRUE.equals(review.getIsVerifiedPurchase())
        );
    }

    private String formatTime(LocalDateTime value) {
        return value == null ? null : value.format(ISO_FORMATTER);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
