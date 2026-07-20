package com.hoang.backend.modules.products.service;

import static com.hoang.backend.modules.products.service.ProductUtils.*;

import com.hoang.backend.common.InMemoryCacheService;
import com.hoang.backend.common.storage.MinioService;
import com.hoang.backend.modules.products.dto.ProductImageResponse;
import com.hoang.backend.modules.products.entity.Product;
import com.hoang.backend.modules.products.entity.ProductImage;
import com.hoang.backend.modules.products.repository.ProductImageRepository;
import com.hoang.backend.modules.products.repository.ProductRepository;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductImageService {

    private static final int MAX_IMAGES = 10;

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final AppUserRepository appUserRepository;
    private final MinioService minioService;
    private final InMemoryCacheService cacheService;

    public List<ProductImageResponse> getProductImages(Long productId) {
        return productImageRepository.findByProductIdAndActiveTrueOrderBySortOrderAsc(productId).stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductImageResponse addImage(String authenticatedUsername, Long productId,
                                          MultipartFile imageFile, Integer sortOrder, Boolean isPrimary) throws IOException {
        requireAdmin(authenticatedUsername);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        long existingCount = productImageRepository.countByProductId(productId);
        if (existingCount >= MAX_IMAGES) {
            throw new IllegalArgumentException("Maximum " + MAX_IMAGES + " images allowed per product.");
        }

        String categorySlug = product.getCategory() == null ? "uncategorized" : safeString(product.getCategory().getSlug());
        String imageUrl = minioService.storeImage("products", categorySlug, imageFile);

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(imageUrl);
        image.setSortOrder(sortOrder != null ? sortOrder : (int) existingCount);
        image.setIsPrimary(isPrimary != null && isPrimary);
        image.setActive(true);

        if (Boolean.TRUE.equals(isPrimary)) {
            clearPrimaryForProduct(productId);
        }

        ProductImage saved = productImageRepository.save(image);

        // Sync product.image with primary
        syncProductMainImage(product);

        invalidateProductCache(productId);
        return toResponse(saved);
    }

    public List<ProductImageResponse> addMultipleImages(String authenticatedUsername, Long productId,
                                                         List<MultipartFile> imageFiles) throws IOException {
        requireAdmin(authenticatedUsername);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        long existingCount = productImageRepository.countByProductId(productId);
        if (existingCount + imageFiles.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("Maximum " + MAX_IMAGES + " images allowed per product. Currently have " + existingCount + ".");
        }

        String categorySlug = product.getCategory() == null ? "uncategorized" : safeString(product.getCategory().getSlug());
        int startOrder = (int) existingCount;

        boolean isFirst = existingCount == 0;
        var savedImages = new java.util.ArrayList<ProductImageResponse>();

        for (int i = 0; i < imageFiles.size(); i++) {
            MultipartFile file = imageFiles.get(i);
            if (file == null || file.isEmpty()) continue;

            String imageUrl = minioService.storeImage("products", categorySlug, file);

            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setImageUrl(imageUrl);
            image.setSortOrder(startOrder + i);
            image.setIsPrimary(isFirst && i == 0);
            image.setActive(true);

            ProductImage saved = productImageRepository.save(image);
            savedImages.add(toResponse(saved));
            isFirst = false;
        }

        // Sync product.image with primary
        syncProductMainImage(product);

        invalidateProductCache(productId);
        return savedImages;
    }

    public ProductImageResponse updateImage(String authenticatedUsername, Long imageId,
                                             Integer sortOrder, Boolean isPrimary) {
        requireAdmin(authenticatedUsername);

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found."));

        if (sortOrder != null) {
            image.setSortOrder(sortOrder);
        }
        if (isPrimary != null) {
            if (isPrimary) {
                clearPrimaryForProduct(image.getProduct().getId());
            }
            image.setIsPrimary(isPrimary);
        }

        ProductImage saved = productImageRepository.save(image);

        // Sync product.image with primary image
        syncProductMainImage(image.getProduct());

        invalidateProductCache(image.getProduct().getId());
        return toResponse(saved);
    }

    public void deleteImage(String authenticatedUsername, Long imageId) {
        requireAdmin(authenticatedUsername);

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found."));

        Long productId = image.getProduct().getId();
        Product product = image.getProduct();
        image.setActive(false);
        productImageRepository.save(image);

        // Sync product.image after deletion
        syncProductMainImage(product);

        invalidateProductCache(productId);
    }

    private void clearPrimaryForProduct(Long productId) {
        productImageRepository.findByProductIdOrderBySortOrderAsc(productId).stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .forEach(img -> {
                    img.setIsPrimary(false);
                    productImageRepository.save(img);
                });
    }

    private void syncProductMainImage(Product product) {
        var images = productImageRepository.findByProductIdOrderBySortOrderAsc(product.getId());
        var primary = images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getActive()) && Boolean.TRUE.equals(img.getIsPrimary()))
                .findFirst();
        if (primary.isPresent()) {
            product.setImage(primary.get().getImageUrl());
        } else if (!images.isEmpty()) {
            var firstActive = images.stream()
                    .filter(img -> Boolean.TRUE.equals(img.getActive()))
                    .findFirst();
            firstActive.ifPresent(img -> product.setImage(img.getImageUrl()));
        }
        productRepository.save(product);
    }

    private ProductImageResponse toResponse(ProductImage image) {
        return new ProductImageResponse(
                image.getId(),
                image.getProduct().getId(),
                safeString(image.getImageUrl()),
                image.getSortOrder() == null ? 0 : image.getSortOrder(),
                Boolean.TRUE.equals(image.getIsPrimary()),
                formatTime(image.getCreatedAt()),
                formatTime(image.getUpdatedAt())
        );
    }

    private void requireAdmin(String usernameOrEmail) {
        AppUser user = appUserRepository.findByUsernameIgnoreCase(usernameOrEmail)
                .or(() -> appUserRepository.findByEmailIgnoreCase(usernameOrEmail))
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!Boolean.TRUE.equals(user.getIsStaff())) {
            throw new IllegalArgumentException("Invalid credentials or insufficient permissions");
        }
    }

    private void invalidateProductCache(Long productId) {
        cacheService.clearPattern("products:list:*");
        cacheService.clearPattern("products:filters:*");
        cacheService.delete("products:top_sellers");
        cacheService.delete("products:new_arrivals");
        cacheService.clearPattern("products:personalized:*");
        if (productId != null) {
            cacheService.delete("product:" + productId);
            cacheService.delete("product:" + productId + ":variants");
            cacheService.delete("product:" + productId + ":variants:list");
            cacheService.delete("product:" + productId + ":recommendations");
        }
    }
}
