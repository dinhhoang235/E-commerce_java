package com.hoang.backend.modules.wishlist.service;

import com.hoang.backend.modules.products.entity.Product;
import com.hoang.backend.modules.products.entity.ProductVariant;
import com.hoang.backend.modules.products.repository.ProductRepository;
import com.hoang.backend.modules.products.repository.ProductVariantRepository;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import com.hoang.backend.modules.wishlist.dto.AddToWishlistRequest;
import com.hoang.backend.modules.wishlist.dto.WishlistActionResponse;
import com.hoang.backend.modules.wishlist.dto.WishlistCheckResponse;
import com.hoang.backend.modules.wishlist.dto.WishlistCountResponse;
import com.hoang.backend.modules.wishlist.dto.WishlistItemResponse;
import com.hoang.backend.modules.wishlist.dto.WishlistProductResponse;
import com.hoang.backend.modules.wishlist.dto.WishlistResponse;
import com.hoang.backend.modules.wishlist.entity.Wishlist;
import com.hoang.backend.modules.wishlist.entity.WishlistItem;
import com.hoang.backend.modules.wishlist.repository.WishlistItemRepository;
import com.hoang.backend.modules.wishlist.repository.WishlistRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(String authenticatedUsername) {
        Wishlist wishlist = getOrCreateWishlist(authenticatedUsername);
        return toWishlistResponse(wishlist);
    }

    public WishlistActionResponse addItem(String authenticatedUsername, AddToWishlistRequest request) {
        Long productId = requireProductId(request);

        Wishlist wishlist = getOrCreateWishlist(authenticatedUsername);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product does not exist"));

        if (wishlistItemRepository.existsByWishlistIdAndProductId(wishlist.getId(), productId)) {
            throw new IllegalArgumentException("Item is already in your wishlist");
        }

        WishlistItem item = new WishlistItem();
        item.setWishlist(wishlist);
        item.setProduct(product);

        WishlistItem saved = wishlistItemRepository.save(item);
        touchWishlist(wishlist);

        return new WishlistActionResponse(
                "Item added to wishlist successfully",
                toItemResponse(saved),
                null
        );
    }

    public WishlistActionResponse removeItem(String authenticatedUsername, Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("product_id is required");
        }

        Wishlist wishlist = wishlistRepository.findByUserId(requireUser(authenticatedUsername).getId())
                .orElseThrow(() -> new IllegalArgumentException("Wishlist not found"));

        WishlistItem item = wishlistItemRepository.findByWishlistIdAndProductId(wishlist.getId(), productId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found in wishlist"));

        wishlistItemRepository.delete(item);
        touchWishlist(wishlist);

        return new WishlistActionResponse("Item removed from wishlist successfully", null, null);
    }

    public WishlistActionResponse clear(String authenticatedUsername) {
        return wishlistRepository.findByUserId(requireUser(authenticatedUsername).getId())
                .map(wishlist -> {
                    long total = wishlistItemRepository.countByWishlistId(wishlist.getId());
                    wishlistItemRepository.deleteByWishlistId(wishlist.getId());
                    touchWishlist(wishlist);
                    return new WishlistActionResponse(total + " items removed from wishlist", null, null);
                })
                .orElseGet(() -> new WishlistActionResponse("Wishlist is already empty", null, null));
    }

    @Transactional(readOnly = true)
    public WishlistCountResponse count(String authenticatedUsername) {
        long count = wishlistRepository.findByUserId(requireUser(authenticatedUsername).getId())
                .map(wishlist -> wishlistItemRepository.countByWishlistId(wishlist.getId()))
                .orElse(0L);
        return new WishlistCountResponse(count);
    }

    public WishlistActionResponse toggleItem(String authenticatedUsername, AddToWishlistRequest request) {
        Long productId = requireProductId(request);
        Wishlist wishlist = getOrCreateWishlist(authenticatedUsername);

        return wishlistItemRepository.findByWishlistIdAndProductId(wishlist.getId(), productId)
                .map(existing -> {
                    wishlistItemRepository.delete(existing);
                    touchWishlist(wishlist);
                    return new WishlistActionResponse("Item removed from wishlist", null, "removed");
                })
                .orElseGet(() -> {
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new IllegalArgumentException("Product does not exist"));

                    WishlistItem item = new WishlistItem();
                    item.setWishlist(wishlist);
                    item.setProduct(product);

                    WishlistItem saved = wishlistItemRepository.save(item);
                    touchWishlist(wishlist);

                    return new WishlistActionResponse("Item added to wishlist", toItemResponse(saved), "added");
                });
    }

    @Transactional(readOnly = true)
    public WishlistCheckResponse checkItem(String authenticatedUsername, Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("product_id parameter is required");
        }

        boolean inWishlist = wishlistRepository.findByUserId(requireUser(authenticatedUsername).getId())
                .map(wishlist -> wishlistItemRepository.existsByWishlistIdAndProductId(wishlist.getId(), productId))
                .orElse(false);

        return new WishlistCheckResponse(inWishlist);
    }

    private Wishlist getOrCreateWishlist(String authenticatedUsername) {
        AppUser user = requireUser(authenticatedUsername);
        return wishlistRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Wishlist wishlist = new Wishlist();
                    wishlist.setUser(user);
                    return wishlistRepository.save(wishlist);
                });
    }

    private AppUser requireUser(String authenticatedUsername) {
        return appUserRepository.findByUsernameIgnoreCase(authenticatedUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private Long requireProductId(AddToWishlistRequest request) {
        if (request == null || request.product_id() == null) {
            throw new IllegalArgumentException("product_id is required");
        }
        return request.product_id();
    }

    private void touchWishlist(Wishlist wishlist) {
        wishlistRepository.save(wishlist);
    }

    private WishlistResponse toWishlistResponse(Wishlist wishlist) {
        List<WishlistItemResponse> items = wishlistItemRepository.findByWishlistIdOrderByAddedAtDesc(wishlist.getId())
                .stream()
                .map(this::toItemResponse)
                .toList();

        return new WishlistResponse(
                wishlist.getId(),
                items,
                items.size(),
                formatTime(wishlist.getCreatedAt()),
                formatTime(wishlist.getUpdatedAt())
        );
    }

    private WishlistItemResponse toItemResponse(WishlistItem item) {
        return new WishlistItemResponse(
                item.getId(),
                toProductResponse(item.getProduct()),
                formatTime(item.getAddedAt())
        );
    }

    private WishlistProductResponse toProductResponse(Product product) {
        List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());

        BigDecimal minPrice = variants.stream()
                .map(ProductVariant::getPrice)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        List<String> colors = variants.stream()
                .map(ProductVariant::getColor)
                .filter(Objects::nonNull)
                .map(color -> color.getName() == null ? "" : color.getName())
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();

        List<String> storages = variants.stream()
                .map(ProductVariant::getStorage)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();

        return new WishlistProductResponse(
                product.getId(),
                safe(product.getName()),
                minPrice,
                safe(product.getImage()),
                null,
                safeNullable(product.getBadge()),
                product.getRating() == null ? 0.0 : product.getRating(),
                product.getReviews() == null ? 0 : product.getReviews(),
                safe(product.getDescription()),
                colors,
                storages
        );
    }

    private String formatTime(LocalDateTime value) {
        return value == null ? null : value.format(ISO_FORMATTER);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safeNullable(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
