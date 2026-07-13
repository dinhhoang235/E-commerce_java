package com.hoang.backend.modules.products.service;

import static com.hoang.backend.modules.products.service.ProductUtils.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoang.backend.common.InMemoryCacheService;
import com.hoang.backend.modules.products.dto.ProductFiltersResponse;
import com.hoang.backend.modules.products.dto.ProductResponse;
import com.hoang.backend.modules.products.entity.Category;
import com.hoang.backend.modules.products.entity.Product;
import com.hoang.backend.modules.products.entity.ProductColor;
import com.hoang.backend.modules.products.entity.ProductVariant;
import com.hoang.backend.modules.products.repository.CategoryRepository;
import com.hoang.backend.modules.products.repository.ProductRepository;
import com.hoang.backend.modules.products.repository.ProductVariantRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final long PRODUCT_LIST_CACHE_TTL = 600;
    private static final long PRODUCT_DETAIL_CACHE_TTL = 900;
    private static final long PRODUCT_RECOMMENDATIONS_CACHE_TTL = 1200;
    private static final long PRODUCT_TOP_SELLERS_CACHE_TTL = 1800;
    private static final long PRODUCT_NEW_ARRIVALS_CACHE_TTL = 900;
    private static final long PRODUCT_PERSONALIZED_CACHE_TTL = 1200;
    private static final long PRODUCT_FILTERS_CACHE_TTL = 1800;

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;
    private final InMemoryCacheService cacheService;
    private final ObjectMapper objectMapper;

    public List<ProductResponse> listProducts(Map<String, String> queryParams) {
        String cacheKey = generateCacheKey("products:list", queryParams);
        return getOrCache(cacheService, cacheKey, PRODUCT_LIST_CACHE_TTL, () -> listProductsUncached(queryParams));
    }

    private List<ProductResponse> listProductsUncached(Map<String, String> queryParams) {
        List<Product> products = productRepository.findAllByOrderByCreatedAtDesc();

        String categorySlug = trimToNull(queryParams.get("category__slug"));
        String search = trimToNull(queryParams.get("search"));
        Double minPrice = parseDouble(queryParams.get("min_price"));
        Double maxPrice = parseDouble(queryParams.get("max_price"));
        Long colorId = parseLong(queryParams.get("color"));
        String storage = normalizeStorage(trimToNull(queryParams.get("storage")));
        Boolean inStock = parseBooleanNullable(queryParams.get("in_stock"));
        Integer limit = parseInt(queryParams.get("limit"));

        Map<Long, List<ProductVariant>> variantsMap = ProductMapper.variantsByProduct(products, productVariantRepository);

        if (categorySlug != null) {
            Optional<Category> targetOptional = categoryRepository.findBySlugIgnoreCase(categorySlug);
            if (targetOptional.isEmpty()) {
                return List.of();
            }
            Category target = targetOptional.get();
            Set<Long> categoryIds = new LinkedHashSet<>();
            categoryIds.add(target.getId());
            categoryRepository.findByParentId(target.getId()).forEach(child -> categoryIds.add(child.getId()));
            products = products.stream()
                    .filter(product -> categoryIds.contains(product.getCategory().getId()))
                    .toList();
        }

        if (minPrice != null || maxPrice != null || colorId != null || storage != null || Boolean.TRUE.equals(inStock)) {
            List<Product> filtered = new ArrayList<>();
            for (Product product : products) {
                List<ProductVariant> variants = variantsMap.getOrDefault(product.getId(), List.of());
                boolean matches = variants.stream().anyMatch(variant -> {
                    if (minPrice != null && variant.getPrice().doubleValue() < minPrice) {
                        return false;
                    }
                    if (maxPrice != null && variant.getPrice().doubleValue() > maxPrice) {
                        return false;
                    }
                    if (colorId != null && !Objects.equals(variant.getColor().getId(), colorId)) {
                        return false;
                    }
                    if (storage != null && !Objects.equals(normalizeStorage(variant.getStorage()), storage)) {
                        return false;
                    }
                    if (Boolean.TRUE.equals(inStock) && !Boolean.TRUE.equals(variant.getIsInStock())) {
                        return false;
                    }
                    return true;
                });
                if (matches) {
                    filtered.add(product);
                }
            }
            products = filtered;
        }

        if (search != null) {
            String normalized = search.toLowerCase(Locale.ROOT);
            products = products.stream()
                    .filter(product -> product.getName() != null && product.getName().toLowerCase(Locale.ROOT).contains(normalized))
                    .sorted(Comparator
                            .comparingInt((Product product) -> {
                                String name = product.getName() == null ? "" : product.getName().toLowerCase(Locale.ROOT);
                                return name.startsWith(normalized) ? 1 : 2;
                            })
                            .thenComparing(Product::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                    .toList();
        }

        if (limit != null && limit > 0 && products.size() > limit) {
            products = products.subList(0, limit);
        }

        Map<Long, List<ProductVariant>> filteredVariantsMap = ProductMapper.variantsByProduct(products, productVariantRepository);
        return products.stream().map(product -> ProductMapper.toProductResponse(product, filteredVariantsMap, productRepository, objectMapper)).toList();
    }

    public ProductResponse getProduct(Long id) {
        return getOrCache(cacheService, "product:" + id, PRODUCT_DETAIL_CACHE_TTL, () -> {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found."));
            Map<Long, List<ProductVariant>> variantsMap = ProductMapper.variantsByProduct(List.of(product), productVariantRepository);
            return ProductMapper.toProductResponse(product, variantsMap, productRepository, objectMapper);
        });
    }

    public List<ProductResponse> getRecommendations(Long productId) {
        return getOrCache(cacheService, "product:" + productId + ":recommendations", PRODUCT_RECOMMENDATIONS_CACHE_TTL, () -> {
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

            Category currentCategory = product.getCategory();
            Long targetCategoryId = currentCategory.getParent() != null ? currentCategory.getParent().getId() : currentCategory.getId();

            Set<Long> categoryIds = new LinkedHashSet<>();
            categoryIds.add(targetCategoryId);
            categoryRepository.findByParentId(targetCategoryId).forEach(category -> categoryIds.add(category.getId()));

            List<Product> products = productRepository.findDistinctByCategoryIdIn(categoryIds.stream().toList()).stream()
                .filter(candidate -> !Objects.equals(candidate.getId(), productId))
                .filter(candidate -> ProductMapper.hasInStockVariant(candidate.getId(), productVariantRepository))
                .sorted(Comparator
                    .comparing(Product::getRating, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(8)
                .toList();

            Map<Long, List<ProductVariant>> variantsMap = ProductMapper.variantsByProduct(products, productVariantRepository);
            return products.stream().map(item -> ProductMapper.toProductResponse(item, variantsMap, productRepository, objectMapper)).toList();
        });
    }

    public List<ProductResponse> getTopSellers() {
        return getOrCache(cacheService, "products:top_sellers", PRODUCT_TOP_SELLERS_CACHE_TTL, () -> {
            List<Product> products = productRepository.findAll();
            Map<Long, List<ProductVariant>> variantsMap = ProductMapper.variantsByProduct(products, productVariantRepository);

            List<Product> result = products.stream()
                .sorted(Comparator
                    .comparingInt((Product product) -> ProductMapper.totalSold(variantsMap.getOrDefault(product.getId(), List.of())))
                    .reversed()
                    .thenComparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .filter(product -> ProductMapper.totalSold(variantsMap.getOrDefault(product.getId(), List.of())) > 0)
                .limit(10)
                .toList();

            if (result.isEmpty()) {
            result = products.stream()
                .sorted(Comparator.comparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .toList();
            }

            Map<Long, List<ProductVariant>> resultVariants = ProductMapper.variantsByProduct(result, productVariantRepository);
            return result.stream().map(product -> ProductMapper.toProductResponse(product, resultVariants, productRepository, objectMapper)).toList();
        });
    }

    public List<ProductResponse> getNewArrivals() {
        return getOrCache(cacheService, "products:new_arrivals", PRODUCT_NEW_ARRIVALS_CACHE_TTL, () -> {
            List<Product> products = productRepository.findAllByOrderByCreatedAtDesc().stream()
                    .filter(product -> ProductMapper.hasInStockVariant(product.getId(), productVariantRepository))
                    .limit(10)
                    .toList();

            Map<Long, List<ProductVariant>> variantsMap = ProductMapper.variantsByProduct(products, productVariantRepository);
            return products.stream().map(product -> ProductMapper.toProductResponse(product, variantsMap, productRepository, objectMapper)).toList();
        });
    }

    public List<ProductResponse> getPersonalized(List<Long> categoryIds) {
        List<Long> safeCategoryIds = categoryIds == null ? List.of() : categoryIds.stream().filter(Objects::nonNull).sorted().toList();
        String cacheKey = generateCacheKey("products:personalized", Map.of("categories", safeCategoryIds.stream().map(String::valueOf).collect(Collectors.joining(","))));
        return getOrCache(cacheService, cacheKey, PRODUCT_PERSONALIZED_CACHE_TTL, () -> getPersonalizedUncached(safeCategoryIds));
    }

    private List<ProductResponse> getPersonalizedUncached(List<Long> categoryIds) {
        List<Product> products = productRepository.findAll();

        if (categoryIds != null && !categoryIds.isEmpty()) {
            Set<Long> categorySet = new LinkedHashSet<>(categoryIds);
            products = products.stream()
                    .filter(product -> categorySet.contains(product.getCategory().getId()))
                    .toList();
        }

        products = products.stream()
                .filter(product -> ProductMapper.hasInStockVariant(product.getId(), productVariantRepository))
                .sorted(Comparator
                        .comparing(Product::getRating, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .toList();

        Map<Long, List<ProductVariant>> variantsMap = ProductMapper.variantsByProduct(products, productVariantRepository);
        return products.stream().map(product -> ProductMapper.toProductResponse(product, variantsMap, productRepository, objectMapper)).toList();
    }

    public ProductFiltersResponse getFilters(String categorySlug) {
        Map<String, String> cacheParams = new LinkedHashMap<>();
        cacheParams.put("category__slug", categorySlug);
        String cacheKey = generateCacheKey("products:filters", cacheParams);
        return getOrCache(cacheService, cacheKey, PRODUCT_FILTERS_CACHE_TTL, () -> getFiltersUncached(categorySlug));
    }

    private ProductFiltersResponse getFiltersUncached(String categorySlug) {
        List<Product> products = productRepository.findAll();

        String normalizedSlug = trimToNull(categorySlug);
        if (normalizedSlug != null) {
            Optional<Category> targetOptional = categoryRepository.findBySlugIgnoreCase(normalizedSlug);
            if (targetOptional.isEmpty()) {
                return new ProductFiltersResponse(new ProductFiltersResponse.PriceRange(null, null), List.of(), List.of());
            }
            Category target = targetOptional.get();
            Set<Long> categoryIds = new LinkedHashSet<>();
            categoryIds.add(target.getId());
            categoryRepository.findByParentId(target.getId()).forEach(child -> categoryIds.add(child.getId()));
            products = products.stream().filter(product -> categoryIds.contains(product.getCategory().getId())).toList();
        }

        Map<Long, List<ProductVariant>> variantsMap = ProductMapper.variantsByProduct(products, productVariantRepository);
        List<ProductVariant> variants = variantsMap.values().stream().flatMap(Collection::stream).toList();

        BigDecimal minPrice = variants.stream().map(ProductVariant::getPrice).min(BigDecimal::compareTo).orElse(null);
        BigDecimal maxPrice = variants.stream().map(ProductVariant::getPrice).max(BigDecimal::compareTo).orElse(null);

        List<com.hoang.backend.modules.products.dto.ProductColorResponse> colors = variants.stream()
                .map(ProductVariant::getColor)
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(ProductColor::getId, ProductMapper::toColorResponse, (left, right) -> left, LinkedHashMap::new),
                        map -> new ArrayList<>(map.values())
                ));

        List<String> storageOptions = variants.stream()
                .map(ProductVariant::getStorage)
                .map(ProductUtils::normalizeStorage)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();

        return new ProductFiltersResponse(new ProductFiltersResponse.PriceRange(minPrice, maxPrice), colors, storageOptions);
    }
}
