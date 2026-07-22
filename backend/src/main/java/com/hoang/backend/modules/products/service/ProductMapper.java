package com.hoang.backend.modules.products.service;

import static com.hoang.backend.modules.products.service.ProductUtils.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoang.backend.modules.products.dto.CategoryResponse;
import com.hoang.backend.modules.products.dto.ProductColorResponse;
import com.hoang.backend.modules.products.dto.ProductImageResponse;
import com.hoang.backend.modules.products.dto.ProductResponse;
import com.hoang.backend.modules.products.dto.ProductShortResponse;
import com.hoang.backend.modules.products.dto.ProductVariantResponse;
import com.hoang.backend.modules.products.entity.Category;
import com.hoang.backend.modules.products.entity.Product;
import com.hoang.backend.modules.products.entity.ProductColor;
import com.hoang.backend.modules.products.entity.ProductImage;
import com.hoang.backend.modules.products.entity.ProductVariant;
import com.hoang.backend.modules.products.repository.ProductRepository;
import com.hoang.backend.modules.products.repository.ProductVariantRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ProductMapper {

    private ProductMapper() {}

    public static ProductResponse toProductResponse(Product product,
                                                     Map<Long, List<ProductVariant>> variantsMap,
                                                     ProductRepository productRepository,
                                                     ObjectMapper objectMapper) {
        List<ProductVariant> variants = variantsMap.getOrDefault(product.getId(), List.of());

        BigDecimal minPrice = variants.stream().map(ProductVariant::getPrice).min(BigDecimal::compareTo).orElse(null);
        BigDecimal maxPrice = variants.stream().map(ProductVariant::getPrice).max(BigDecimal::compareTo).orElse(null);
        int totalStock = variants.stream().map(ProductVariant::getStock).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();

        List<ProductColorResponse> availableColors = variants.stream()
                .map(ProductVariant::getColor)
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(ProductColor::getId, ProductMapper::toColorResponse, (left, right) -> left, LinkedHashMap::new),
                        map -> new ArrayList<>(map.values())
                ));

        List<String> availableStorages = variants.stream()
                .map(ProductVariant::getStorage)
                .map(ProductUtils::normalizeStorage)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();

        List<ProductVariantResponse> variantResponses = variants.stream()
                .sorted(Comparator.comparing(ProductVariant::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(ProductMapper::toVariantResponse)
                .toList();

        List<ProductImageResponse> imageResponses = product.getImages() == null ? List.of() :
                product.getImages().stream()
                        .filter(img -> Boolean.TRUE.equals(img.getActive()))
                        .sorted(Comparator
                                .comparing((ProductImage img) -> !Boolean.TRUE.equals(img.getIsPrimary()))
                                .thenComparing(ProductImage::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(ProductMapper::toImageResponse)
                        .toList();

        // Use primary image from images list as the main image, fallback to legacy field
        String mainImage = safeString(product.getImage());
        if (!imageResponses.isEmpty()) {
            var primaryOpt = imageResponses.stream()
                    .filter(ProductImageResponse::is_primary)
                    .findFirst();
            if (primaryOpt.isPresent()) {
                mainImage = primaryOpt.get().image_url();
            } else if (!imageResponses.isEmpty()) {
                mainImage = imageResponses.get(0).image_url();
            }
        }

        return new ProductResponse(
                product.getId(),
                safeString(product.getName()),
                toCategoryResponse(product.getCategory(), productRepository),
                product.getCategory() == null ? null : product.getCategory().getId(),
                mainImage,
                imageResponses,
                product.getRating() == null ? 0.0 : product.getRating(),
                product.getReviews() == null ? 0 : product.getReviews(),
                safeString(product.getBadge()),
                safeString(product.getDescription()),
                safeString(product.getFullDescription()),
                parseFeatureList(product.getFeatures(), objectMapper),
                variantResponses,
                minPrice,
                maxPrice,
                totalStock,
                availableColors,
                availableStorages,
                formatTime(product.getCreatedAt()),
                formatTime(product.getUpdatedAt())
        );
    }

    public static CategoryResponse toCategoryResponse(Category category, ProductRepository productRepository) {
        if (category == null) {
            return null;
        }
        return new CategoryResponse(
                category.getId(),
                safeString(category.getName()),
                safeString(category.getSlug()),
                safeString(category.getDescription()),
                safeString(category.getImage()),
                Boolean.TRUE.equals(category.getIsActive()),
                category.getSortOrder() == null ? 0 : category.getSortOrder(),
                category.getParent() == null ? null : category.getParent().getId(),
                category.getParent() == null ? null : category.getParent().getId(),
                category.getProductCount() == null ? 0 : category.getProductCount(),
                formatTime(category.getCreatedAt()),
                formatTime(category.getUpdatedAt())
        );
    }

    public static ProductColorResponse toColorResponse(ProductColor color) {
        return new ProductColorResponse(color.getId(), safeString(color.getName()), safeString(color.getHexCode()));
    }

    public static ProductImageResponse toImageResponse(ProductImage image) {
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

    public static ProductVariantResponse toVariantResponse(ProductVariant variant) {
        Product product = variant.getProduct();
        ProductShortResponse productShort = new ProductShortResponse(
                product.getId(),
                safeString(product.getName()),
                safeString(product.getImage()),
                safeString(product.getDescription())
        );

        return new ProductVariantResponse(
                variant.getId(),
                productShort,
                product.getId(),
                toColorResponse(variant.getColor()),
                variant.getColor() == null ? null : variant.getColor().getId(),
                normalizeStorage(variant.getStorage()),
                variant.getPrice(),
                variant.getStock() == null ? 0 : variant.getStock(),
                variant.getSold() == null ? 0 : variant.getSold(),
                Boolean.TRUE.equals(variant.getIsInStock()),
                variant.getStock() == null ? 0 : variant.getStock(),
                formatTime(variant.getCreatedAt()),
                formatTime(variant.getUpdatedAt())
        );
    }

    public static Map<Long, List<ProductVariant>> variantsByProduct(List<Product> products,
                                                                     ProductVariantRepository productVariantRepository) {
        if (products == null || products.isEmpty()) {
            return Map.of();
        }
        List<Long> productIds = products.stream().map(Product::getId).toList();
        return productVariantRepository.findByProductIdInAndActiveTrue(productIds).stream()
                .collect(Collectors.groupingBy(variant -> variant.getProduct().getId()));
    }

    public static boolean hasInStockVariant(Long productId, ProductVariantRepository productVariantRepository) {
        return productVariantRepository.findByProductIdAndActiveTrue(productId).stream()
                .anyMatch(variant -> Boolean.TRUE.equals(variant.getIsInStock()));
    }

    public static int totalSold(List<ProductVariant> variants) {
        return variants.stream()
                .map(ProductVariant::getSold)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }
}
