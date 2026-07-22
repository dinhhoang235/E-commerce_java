package com.hoang.backend.modules.products.service;

import static com.hoang.backend.modules.products.service.ProductUtils.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoang.backend.common.InMemoryCacheService;
import com.hoang.backend.common.exceptions.UnauthorizedException;
import com.hoang.backend.common.exceptions.UserNotFoundException;
import com.hoang.backend.common.storage.MinioService;
import com.hoang.backend.modules.products.dto.ProductResponse;
import com.hoang.backend.modules.products.entity.Category;
import com.hoang.backend.modules.products.entity.Product;
import com.hoang.backend.modules.products.repository.CategoryRepository;
import com.hoang.backend.modules.products.repository.ProductRepository;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCommandService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AppUserRepository appUserRepository;
    private final InMemoryCacheService cacheService;
    private final ObjectMapper objectMapper;
    private final MinioService minioService;

    public ProductResponse createProduct(String authenticatedUsername, Map<String, Object> payload,
                                          MultipartFile imageFile, ProductService productService) throws IOException {
        requireAdmin(authenticatedUsername);

        Product product = new Product();
        applyProductPayload(product, payload, imageFile);

        Product saved = productRepository.save(product);
        invalidateProductCache(null);
        return productService.getProduct(saved.getId());
    }

    public ProductResponse updateProduct(String authenticatedUsername, Long id,
                                          Map<String, Object> payload,
                                          MultipartFile imageFile, ProductService productService) throws IOException {
        requireAdmin(authenticatedUsername);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        applyProductPayload(product, payload, imageFile);

        productRepository.save(product);
        invalidateProductCache(id);
        return productService.getProduct(id);
    }

    public void deleteProduct(String authenticatedUsername, Long id) {
        requireAdmin(authenticatedUsername);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));
        product.setActive(false);
        productRepository.save(product);
        invalidateProductCache(id);
    }

    private void applyProductPayload(Product product, Map<String, Object> payload, MultipartFile imageFile) throws IOException {
        if (payload.containsKey("name") || product.getId() == null) {
            product.setName(requireNonBlank(payload.get("name"), "Product name is required."));
        }

        if (payload.containsKey("category_id") || product.getId() == null) {
            Long categoryId = parseLong(payload.get("category_id"));
            if (categoryId == null) {
                throw new IllegalArgumentException("category_id is required.");
            }
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found."));
            product.setCategory(category);
        }

        if (payload.containsKey("description")) {
            product.setDescription(safeString(payload.get("description")));
        }
        if (payload.containsKey("full_description")) {
            product.setFullDescription(safeString(payload.get("full_description")));
        }
        if (payload.containsKey("badge")) {
            product.setBadge(cleanNullableString(payload.get("badge")));
        }
        if (payload.containsKey("rating")) {
            Double rating = parseDoubleObject(payload.get("rating"));
            product.setRating(rating == null ? 0.0 : rating);
        }
        if (payload.containsKey("reviews")) {
            Integer reviews = parseInt(payload.get("reviews"));
            product.setReviews(reviews == null ? 0 : Math.max(reviews, 0));
        }
        if (payload.containsKey("features")) {
            product.setFeatures(serializeFeatures(payload.get("features"), objectMapper));
        }

        if (payload.containsKey("image") && cleanNullableString(payload.get("image")) != null) {
            product.setImage(cleanNullableString(payload.get("image")));
        }
        if (payload.containsKey("image") && cleanNullableString(payload.get("image")) == null && imageFile == null) {
            product.setImage(null);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String categorySlug = product.getCategory() == null ? "uncategorized" : safeString(product.getCategory().getSlug());
            product.setImage(minioService.storeImage("products", categorySlug, imageFile));
        }
    }

    private void requireAdmin(String usernameOrEmail) {
        AppUser user = appUserRepository.findByUsernameIgnoreCase(usernameOrEmail)
                .or(() -> appUserRepository.findByEmailIgnoreCase(usernameOrEmail))
                .orElseThrow(() -> new UserNotFoundException(usernameOrEmail));
        if (!Boolean.TRUE.equals(user.getIsStaff())) {
            throw new UnauthorizedException();
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
        } else {
            cacheService.clearPattern("product:*");
        }
    }
}
