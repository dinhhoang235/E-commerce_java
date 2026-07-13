package com.hoang.backend.modules.products.service;

import static com.hoang.backend.modules.products.service.ProductUtils.*;

import com.hoang.backend.common.InMemoryCacheService;
import com.hoang.backend.modules.products.dto.ProductVariantResponse;
import com.hoang.backend.modules.products.entity.Product;
import com.hoang.backend.modules.products.entity.ProductColor;
import com.hoang.backend.modules.products.entity.ProductVariant;
import com.hoang.backend.modules.products.repository.ProductColorRepository;
import com.hoang.backend.modules.products.repository.ProductRepository;
import com.hoang.backend.modules.products.repository.ProductVariantRepository;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductVariantService {

    private static final long PRODUCT_VARIANTS_CACHE_TTL = 900;

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ProductColorRepository productColorRepository;
    private final AppUserRepository appUserRepository;
    private final InMemoryCacheService cacheService;

    @Transactional(readOnly = true)
    public List<ProductVariantResponse> listVariants(Optional<Long> productId) {
        if (productId.isPresent()) {
            Long value = productId.get();
            return getOrCache("product:" + value + ":variants:list", PRODUCT_VARIANTS_CACHE_TTL, () ->
                    productVariantRepository.findByProductIdOrderByCreatedAtDesc(value).stream()
                            .map(ProductMapper::toVariantResponse)
                            .toList()
            );
        }

        List<ProductVariant> variants = productId
                .map(productVariantRepository::findByProductIdOrderByCreatedAtDesc)
                .orElseGet(productVariantRepository::findAllByOrderByCreatedAtDesc);

        return variants.stream().map(ProductMapper::toVariantResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductVariantResponse getVariant(Long id) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found."));
        return ProductMapper.toVariantResponse(variant);
    }

    public ProductVariantResponse createVariant(String authenticatedUsername, Map<String, Object> payload) {
        requireAdmin(authenticatedUsername);

        Long productId = parseLong(payload.get("product_id"));
        if (productId == null) {
            productId = parseLong(payload.get("product"));
        }
        Long colorId = parseLong(payload.get("color_id"));
        BigDecimal price = parseDecimal(payload.get("price"));
        Integer stock = parseInt(payload.get("stock"));
        String storage = normalizeStorage(cleanNullableString(payload.get("storage")));

        if (productId == null) {
            throw new IllegalArgumentException("product_id is required.");
        }
        if (colorId == null) {
            throw new IllegalArgumentException("color_id is required.");
        }
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("price must be a non-negative number.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));
        ProductColor color = productColorRepository.findById(colorId)
                .orElseThrow(() -> new IllegalArgumentException("Color not found."));

        ensureUniqueVariant(productId, colorId, storage, null);

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setColor(color);
        variant.setStorage(storage);
        variant.setPrice(price);
        variant.setStock(stock == null ? 0 : Math.max(stock, 0));
        variant.setSold(0);
        variant.setIsInStock((stock == null ? 0 : stock) > 0);

        ProductVariant saved = productVariantRepository.save(variant);
        invalidateProductCache(saved.getProduct().getId());
        return ProductMapper.toVariantResponse(saved);
    }

    public ProductVariantResponse updateVariant(String authenticatedUsername, Long id, Map<String, Object> payload) {
        requireAdmin(authenticatedUsername);

        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found."));

        Long productId = payload.containsKey("product_id") ? parseLong(payload.get("product_id")) : variant.getProduct().getId();
        if (payload.containsKey("product") && productId == null) {
            productId = parseLong(payload.get("product"));
        }

        Long colorId = payload.containsKey("color_id") ? parseLong(payload.get("color_id")) : variant.getColor().getId();
        String storage = payload.containsKey("storage")
                ? normalizeStorage(cleanNullableString(payload.get("storage")))
                : normalizeStorage(variant.getStorage());

        ensureUniqueVariant(productId, colorId, storage, id);

        if (productId != null && !Objects.equals(productId, variant.getProduct().getId())) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found."));
            variant.setProduct(product);
        }

        if (colorId != null && !Objects.equals(colorId, variant.getColor().getId())) {
            ProductColor color = productColorRepository.findById(colorId)
                    .orElseThrow(() -> new IllegalArgumentException("Color not found."));
            variant.setColor(color);
        }

        if (payload.containsKey("storage")) {
            variant.setStorage(storage);
        }

        if (payload.containsKey("price")) {
            BigDecimal price = parseDecimal(payload.get("price"));
            if (price == null || price.signum() < 0) {
                throw new IllegalArgumentException("price must be a non-negative number.");
            }
            variant.setPrice(price);
        }

        if (payload.containsKey("stock")) {
            Integer stock = parseInt(payload.get("stock"));
            if (stock == null) {
                throw new IllegalArgumentException("stock must be a number.");
            }
            variant.setStock(Math.max(stock, 0));
            variant.setIsInStock(stock > 0);
        }

        if (payload.containsKey("sold")) {
            Integer sold = parseInt(payload.get("sold"));
            if (sold == null) {
                throw new IllegalArgumentException("sold must be a number.");
            }
            variant.setSold(Math.max(sold, 0));
        }

        ProductVariant saved = productVariantRepository.save(variant);
        invalidateProductCache(saved.getProduct().getId());
        return ProductMapper.toVariantResponse(saved);
    }

    public void deleteVariant(String authenticatedUsername, Long id) {
        requireAdmin(authenticatedUsername);

        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found."));
        Long productId = variant.getProduct().getId();
        productVariantRepository.delete(variant);
        invalidateProductCache(productId);
    }

    public ProductVariantResponse reduceStock(String authenticatedUsername, Long variantId, Integer quantity) {
        requireAdmin(authenticatedUsername);

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found."));

        int safeQuantity = quantity == null ? 0 : quantity;
        if (safeQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        if (variant.getStock() < safeQuantity) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + variant.getStock() + ", Requested: " + safeQuantity);
        }

        variant.setStock(variant.getStock() - safeQuantity);
        variant.setSold(variant.getSold() + safeQuantity);
        variant.setIsInStock(variant.getStock() > 0);

        ProductVariant saved = productVariantRepository.save(variant);
        invalidateProductCache(saved.getProduct().getId());
        return ProductMapper.toVariantResponse(saved);
    }

    public ProductVariantResponse increaseStock(String authenticatedUsername, Long variantId, Integer quantity) {
        requireAdmin(authenticatedUsername);

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found."));

        int safeQuantity = quantity == null ? 0 : quantity;
        if (safeQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }

        variant.setStock(variant.getStock() + safeQuantity);
        variant.setSold(Math.max(variant.getSold() - safeQuantity, 0));
        variant.setIsInStock(variant.getStock() > 0);

        ProductVariant saved = productVariantRepository.save(variant);
        invalidateProductCache(saved.getProduct().getId());
        return ProductMapper.toVariantResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductVariantResponse> getProductVariants(Long productId) {
        return getOrCache("product:" + productId + ":variants", PRODUCT_VARIANTS_CACHE_TTL, () -> {
            if (!productRepository.existsById(productId)) {
                throw new IllegalArgumentException("Product not found.");
            }

            return productVariantRepository.findByProductId(productId).stream()
                    .sorted(Comparator
                            .comparing((ProductVariant variant) -> variant.getColor().getName(), Comparator.nullsLast(String::compareToIgnoreCase))
                            .thenComparing(variant -> normalizeStorage(variant.getStorage()), Comparator.nullsLast(String::compareToIgnoreCase))
                            .thenComparing(ProductVariant::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .map(ProductMapper::toVariantResponse)
                    .toList();
        });
    }

    private void ensureUniqueVariant(Long productId, Long colorId, String storage, Long existingVariantId) {
        if (productId == null || colorId == null) {
            return;
        }
        Optional<ProductVariant> existing = productVariantRepository.findByProductIdAndColorIdAndStorage(productId, colorId, storage);
        if (existing.isPresent() && !Objects.equals(existing.get().getId(), existingVariantId)) {
            throw new IllegalArgumentException("A variant with this combination already exists.");
        }
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
        } else {
            cacheService.clearPattern("product:*");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getOrCache(String cacheKey, long ttlSeconds, java.util.function.Supplier<T> supplier) {
        Object cachedValue = cacheService.get(cacheKey);
        if (cachedValue != null) {
            return (T) cachedValue;
        }
        T computed = supplier.get();
        cacheService.set(cacheKey, computed, ttlSeconds);
        return computed;
    }
}
