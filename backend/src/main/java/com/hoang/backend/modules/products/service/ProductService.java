package com.hoang.backend.modules.products.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoang.backend.common.InMemoryCacheService;
import com.hoang.backend.modules.products.dto.CategoryResponse;
import com.hoang.backend.modules.products.dto.ProductColorResponse;
import com.hoang.backend.modules.products.dto.ProductFiltersResponse;
import com.hoang.backend.modules.products.dto.ProductResponse;
import com.hoang.backend.modules.products.dto.ProductShortResponse;
import com.hoang.backend.modules.products.dto.ProductVariantResponse;
import com.hoang.backend.modules.products.entity.Category;
import com.hoang.backend.modules.products.entity.Product;
import com.hoang.backend.modules.products.entity.ProductColor;
import com.hoang.backend.modules.products.entity.ProductVariant;
import com.hoang.backend.modules.products.repository.CategoryRepository;
import com.hoang.backend.modules.products.repository.ProductColorRepository;
import com.hoang.backend.modules.products.repository.ProductRepository;
import com.hoang.backend.modules.products.repository.ProductVariantRepository;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final long CATEGORY_CACHE_TTL = 1800;
    private static final long COLOR_CACHE_TTL = 3600;
    private static final long PRODUCT_LIST_CACHE_TTL = 600;
    private static final long PRODUCT_DETAIL_CACHE_TTL = 900;
    private static final long PRODUCT_VARIANTS_CACHE_TTL = 900;
    private static final long PRODUCT_RECOMMENDATIONS_CACHE_TTL = 1200;
    private static final long PRODUCT_TOP_SELLERS_CACHE_TTL = 1800;
    private static final long PRODUCT_NEW_ARRIVALS_CACHE_TTL = 900;
    private static final long PRODUCT_PERSONALIZED_CACHE_TTL = 1200;
    private static final long PRODUCT_FILTERS_CACHE_TTL = 1800;

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductColorRepository productColorRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AppUserRepository appUserRepository;
    private final ObjectMapper objectMapper;
    private final InMemoryCacheService cacheService;

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return getOrCache("categories:all", CATEGORY_CACHE_TTL, () ->
            categoryRepository.findAllByOrderBySortOrderAscNameAsc().stream()
                .map(this::toCategoryResponse)
                .toList()
        );
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {
        return getOrCache("category:" + id, CATEGORY_CACHE_TTL, () -> {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found."));
            return toCategoryResponse(category);
        });
    }

    public CategoryResponse createCategory(String authenticatedUsername, Map<String, Object> payload, MultipartFile imageFile) throws IOException {
        requireAdmin(authenticatedUsername);

        Category category = new Category();
        applyCategoryPayload(category, payload, imageFile);
        CategoryResponse response = toCategoryResponse(categoryRepository.save(category));
        invalidateCategoryCache();
        return response;
    }

    public CategoryResponse updateCategory(String authenticatedUsername, Long id, Map<String, Object> payload, MultipartFile imageFile) throws IOException {
        requireAdmin(authenticatedUsername);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

        applyCategoryPayload(category, payload, imageFile);
        CategoryResponse response = toCategoryResponse(categoryRepository.save(category));
        invalidateCategoryCache();
        return response;
    }

    public void deleteCategory(String authenticatedUsername, Long id) {
        requireAdmin(authenticatedUsername);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));
        categoryRepository.delete(category);
        invalidateCategoryCache();
    }

    @Transactional(readOnly = true)
    public List<ProductColorResponse> listColors() {
        return getOrCache("product_colors:all", COLOR_CACHE_TTL, () ->
            productColorRepository.findAllByOrderByNameAsc().stream()
                .map(this::toColorResponse)
                .toList()
        );
    }

    public ProductColorResponse createColor(String authenticatedUsername, Map<String, Object> payload) {
        requireAdmin(authenticatedUsername);

        ProductColor color = new ProductColor();
        color.setName(requireNonBlank(payload.get("name"), "Color name is required."));
        color.setHexCode(cleanNullableString(payload.get("hex_code")));
        ProductColorResponse response = toColorResponse(productColorRepository.save(color));
        cacheService.delete("product_colors:all");
        invalidateProductCache(null);
        return response;
    }

    public ProductColorResponse updateColor(String authenticatedUsername, Long id, Map<String, Object> payload) {
        requireAdmin(authenticatedUsername);

        ProductColor color = productColorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Color not found."));

        if (payload.containsKey("name")) {
            color.setName(requireNonBlank(payload.get("name"), "Color name is required."));
        }
        if (payload.containsKey("hex_code")) {
            color.setHexCode(cleanNullableString(payload.get("hex_code")));
        }
        ProductColorResponse response = toColorResponse(productColorRepository.save(color));
        cacheService.delete("product_colors:all");
        invalidateProductCache(null);
        return response;
    }

    public void deleteColor(String authenticatedUsername, Long id) {
        requireAdmin(authenticatedUsername);

        ProductColor color = productColorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Color not found."));
        productColorRepository.delete(color);
        cacheService.delete("product_colors:all");
        invalidateProductCache(null);
    }

    @Transactional(readOnly = true)
    public List<ProductVariantResponse> listVariants(Optional<Long> productId) {
        if (productId.isPresent()) {
            Long value = productId.get();
            return getOrCache("product:" + value + ":variants:list", PRODUCT_VARIANTS_CACHE_TTL, () ->
                    productVariantRepository.findByProductIdOrderByCreatedAtDesc(value).stream()
                            .map(this::toVariantResponse)
                            .toList()
            );
        }

        List<ProductVariant> variants = productId
                .map(productVariantRepository::findByProductIdOrderByCreatedAtDesc)
                .orElseGet(productVariantRepository::findAllByOrderByCreatedAtDesc);

        return variants.stream().map(this::toVariantResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductVariantResponse getVariant(Long id) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found."));
        return toVariantResponse(variant);
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
        return toVariantResponse(saved);
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
        return toVariantResponse(saved);
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
        return toVariantResponse(saved);
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
        return toVariantResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listProducts(Map<String, String> queryParams) {
        String cacheKey = generateCacheKey("products:list", queryParams);
        return getOrCache(cacheKey, PRODUCT_LIST_CACHE_TTL, () -> listProductsUncached(queryParams));
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

        Map<Long, List<ProductVariant>> variantsMap = variantsByProduct(products);

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

        Map<Long, List<ProductVariant>> filteredVariantsMap = variantsByProduct(products);
        return products.stream().map(product -> toProductResponse(product, filteredVariantsMap)).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        return getOrCache("product:" + id, PRODUCT_DETAIL_CACHE_TTL, () -> {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found."));
            Map<Long, List<ProductVariant>> variantsMap = variantsByProduct(List.of(product));
            return toProductResponse(product, variantsMap);
        });
    }

    public ProductResponse createProduct(String authenticatedUsername, Map<String, Object> payload, MultipartFile imageFile) throws IOException {
        requireAdmin(authenticatedUsername);

        Product product = new Product();
        applyProductPayload(product, payload, imageFile);

        Product saved = productRepository.save(product);
        invalidateProductCache(null);
        return getProduct(saved.getId());
    }

    public ProductResponse updateProduct(String authenticatedUsername, Long id, Map<String, Object> payload, MultipartFile imageFile) throws IOException {
        requireAdmin(authenticatedUsername);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        applyProductPayload(product, payload, imageFile);

        productRepository.save(product);
        invalidateProductCache(id);
        return getProduct(id);
    }

    public void deleteProduct(String authenticatedUsername, Long id) {
        requireAdmin(authenticatedUsername);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));
        productRepository.delete(product);
        invalidateProductCache(id);
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
                    .map(this::toVariantResponse)
                    .toList();
        });
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getRecommendations(Long productId) {
        return getOrCache("product:" + productId + ":recommendations", PRODUCT_RECOMMENDATIONS_CACHE_TTL, () -> {
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

            Category currentCategory = product.getCategory();
            Long targetCategoryId = currentCategory.getParent() != null ? currentCategory.getParent().getId() : currentCategory.getId();

            Set<Long> categoryIds = new LinkedHashSet<>();
            categoryIds.add(targetCategoryId);
            categoryRepository.findByParentId(targetCategoryId).forEach(category -> categoryIds.add(category.getId()));

            List<Product> products = productRepository.findDistinctByCategoryIdIn(categoryIds.stream().toList()).stream()
                .filter(candidate -> !Objects.equals(candidate.getId(), productId))
                .filter(candidate -> hasInStockVariant(candidate.getId()))
                .sorted(Comparator
                    .comparing(Product::getRating, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(8)
                .toList();

            Map<Long, List<ProductVariant>> variantsMap = variantsByProduct(products);
            return products.stream().map(item -> toProductResponse(item, variantsMap)).toList();
        });
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getTopSellers() {
        return getOrCache("products:top_sellers", PRODUCT_TOP_SELLERS_CACHE_TTL, () -> {
            List<Product> products = productRepository.findAll();
            Map<Long, List<ProductVariant>> variantsMap = variantsByProduct(products);

            List<Product> result = products.stream()
                .sorted(Comparator
                    .comparingInt((Product product) -> totalSold(variantsMap.getOrDefault(product.getId(), List.of())))
                    .reversed()
                    .thenComparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .filter(product -> totalSold(variantsMap.getOrDefault(product.getId(), List.of())) > 0)
                .limit(10)
                .toList();

            if (result.isEmpty()) {
            result = products.stream()
                .sorted(Comparator.comparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .toList();
            }

            Map<Long, List<ProductVariant>> resultVariants = variantsByProduct(result);
            return result.stream().map(product -> toProductResponse(product, resultVariants)).toList();
        });
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getNewArrivals() {
        return getOrCache("products:new_arrivals", PRODUCT_NEW_ARRIVALS_CACHE_TTL, () -> {
            List<Product> products = productRepository.findAllByOrderByCreatedAtDesc().stream()
                    .filter(product -> hasInStockVariant(product.getId()))
                    .limit(10)
                    .toList();

            Map<Long, List<ProductVariant>> variantsMap = variantsByProduct(products);
            return products.stream().map(product -> toProductResponse(product, variantsMap)).toList();
        });
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getPersonalized(List<Long> categoryIds) {
        List<Long> safeCategoryIds = categoryIds == null ? List.of() : categoryIds.stream().filter(Objects::nonNull).sorted().toList();
        String cacheKey = generateCacheKey("products:personalized", Map.of("categories", safeCategoryIds.stream().map(String::valueOf).collect(Collectors.joining(","))));
        return getOrCache(cacheKey, PRODUCT_PERSONALIZED_CACHE_TTL, () -> getPersonalizedUncached(safeCategoryIds));
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
                .filter(product -> hasInStockVariant(product.getId()))
                .sorted(Comparator
                        .comparing(Product::getRating, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .toList();

        Map<Long, List<ProductVariant>> variantsMap = variantsByProduct(products);
        return products.stream().map(product -> toProductResponse(product, variantsMap)).toList();
    }

    @Transactional(readOnly = true)
    public ProductFiltersResponse getFilters(String categorySlug) {
        Map<String, String> cacheParams = new LinkedHashMap<>();
        cacheParams.put("category__slug", categorySlug);
        String cacheKey = generateCacheKey("products:filters", cacheParams);
        return getOrCache(cacheKey, PRODUCT_FILTERS_CACHE_TTL, () -> getFiltersUncached(categorySlug));
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

        Map<Long, List<ProductVariant>> variantsMap = variantsByProduct(products);
        List<ProductVariant> variants = variantsMap.values().stream().flatMap(Collection::stream).toList();

        BigDecimal minPrice = variants.stream().map(ProductVariant::getPrice).min(BigDecimal::compareTo).orElse(null);
        BigDecimal maxPrice = variants.stream().map(ProductVariant::getPrice).max(BigDecimal::compareTo).orElse(null);

        List<ProductColorResponse> colors = variants.stream()
                .map(ProductVariant::getColor)
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(ProductColor::getId, this::toColorResponse, (left, right) -> left, LinkedHashMap::new),
                        map -> new ArrayList<>(map.values())
                ));

        List<String> storageOptions = variants.stream()
                .map(ProductVariant::getStorage)
                .map(this::normalizeStorage)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();

        return new ProductFiltersResponse(new ProductFiltersResponse.PriceRange(minPrice, maxPrice), colors, storageOptions);
    }

    private ProductResponse toProductResponse(Product product, Map<Long, List<ProductVariant>> variantsMap) {
        List<ProductVariant> variants = variantsMap.getOrDefault(product.getId(), List.of());

        BigDecimal minPrice = variants.stream().map(ProductVariant::getPrice).min(BigDecimal::compareTo).orElse(null);
        BigDecimal maxPrice = variants.stream().map(ProductVariant::getPrice).max(BigDecimal::compareTo).orElse(null);
        int totalStock = variants.stream().map(ProductVariant::getStock).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();

        List<ProductColorResponse> availableColors = variants.stream()
                .map(ProductVariant::getColor)
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(ProductColor::getId, this::toColorResponse, (left, right) -> left, LinkedHashMap::new),
                        map -> new ArrayList<>(map.values())
                ));

        List<String> availableStorages = variants.stream()
                .map(ProductVariant::getStorage)
                .map(this::normalizeStorage)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();

        List<ProductVariantResponse> variantResponses = variants.stream()
                .sorted(Comparator.comparing(ProductVariant::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toVariantResponse)
                .toList();

        return new ProductResponse(
                product.getId(),
                safeString(product.getName()),
                toCategoryResponse(product.getCategory()),
                product.getCategory() == null ? null : product.getCategory().getId(),
                safeString(product.getImage()),
                product.getRating() == null ? 0.0 : product.getRating(),
                product.getReviews() == null ? 0 : product.getReviews(),
                safeString(product.getBadge()),
                safeString(product.getDescription()),
                safeString(product.getFullDescription()),
                parseFeatureList(product.getFeatures()),
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

    private CategoryResponse toCategoryResponse(Category category) {
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
                Math.toIntExact(productRepository.countByCategoryId(category.getId())),
                formatTime(category.getCreatedAt()),
                formatTime(category.getUpdatedAt())
        );
    }

    private ProductColorResponse toColorResponse(ProductColor color) {
        return new ProductColorResponse(color.getId(), safeString(color.getName()), safeString(color.getHexCode()));
    }

    private ProductVariantResponse toVariantResponse(ProductVariant variant) {
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

    private void applyCategoryPayload(Category category, Map<String, Object> payload, MultipartFile imageFile) throws IOException {
        if (payload.containsKey("name") || category.getId() == null) {
            category.setName(requireNonBlank(payload.get("name"), "Category name is required."));
        }
        if (payload.containsKey("slug") || category.getId() == null) {
            category.setSlug(requireNonBlank(payload.get("slug"), "Category slug is required."));
        }

        if (payload.containsKey("description")) {
            category.setDescription(safeString(payload.get("description")));
        }
        if (payload.containsKey("is_active")) {
            category.setIsActive(Boolean.TRUE.equals(parseBoolean(payload.get("is_active"))));
        }
        if (payload.containsKey("sort_order")) {
            Integer sortOrder = parseInt(payload.get("sort_order"));
            category.setSortOrder(sortOrder == null ? 0 : sortOrder);
        }

        if (payload.containsKey("parent_id")) {
            Long parentId = parseLong(payload.get("parent_id"));
            if (parentId == null) {
                category.setParent(null);
            } else {
                Category parent = categoryRepository.findById(parentId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid parent category ID."));
                if (category.getId() != null && Objects.equals(category.getId(), parentId)) {
                    throw new IllegalArgumentException("Category cannot be its own parent.");
                }
                category.setParent(parent);
            }
        }

        if (payload.containsKey("image") && cleanNullableString(payload.get("image")) != null) {
            category.setImage(cleanNullableString(payload.get("image")));
        }
        if (payload.containsKey("image") && cleanNullableString(payload.get("image")) == null && imageFile == null) {
            category.setImage(null);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            category.setImage(storeImage("categories", safeString(category.getSlug()), imageFile));
        }
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
            product.setFeatures(serializeFeatures(payload.get("features")));
        }

        if (payload.containsKey("image") && cleanNullableString(payload.get("image")) != null) {
            product.setImage(cleanNullableString(payload.get("image")));
        }
        if (payload.containsKey("image") && cleanNullableString(payload.get("image")) == null && imageFile == null) {
            product.setImage(null);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String categorySlug = product.getCategory() == null ? "uncategorized" : safeString(product.getCategory().getSlug());
            product.setImage(storeImage("products", categorySlug, imageFile));
        }
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

    private Map<Long, List<ProductVariant>> variantsByProduct(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Map.of();
        }

        List<Long> productIds = products.stream().map(Product::getId).toList();
        return productVariantRepository.findByProductIdIn(productIds).stream()
                .collect(Collectors.groupingBy(variant -> variant.getProduct().getId()));
    }

    private boolean hasInStockVariant(Long productId) {
        return productVariantRepository.findByProductId(productId).stream().anyMatch(variant -> Boolean.TRUE.equals(variant.getIsInStock()));
    }

    private int totalSold(List<ProductVariant> variants) {
        return variants.stream().map(ProductVariant::getSold).filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
    }

    @SuppressWarnings("unchecked")
    private <T> T getOrCache(String cacheKey, long ttlSeconds, Supplier<T> supplier) {
        Object cachedValue = cacheService.get(cacheKey);
        if (cachedValue != null) {
            return (T) cachedValue;
        }

        T computed = supplier.get();
        cacheService.set(cacheKey, computed, ttlSeconds);
        return computed;
    }

    private String generateCacheKey(String prefix, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return prefix;
        }

        String query = params.entrySet().stream()
                .filter(entry -> trimToNull(entry.getValue()) != null)
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        if (query.isBlank()) {
            return prefix;
        }

        return prefix + ":" + shortHash(query);
    }

    private String shortHash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] encoded = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < encoded.length; i++) {
                builder.append(String.format("%02x", encoded[i]));
            }
            return builder.substring(0, 8);
        } catch (NoSuchAlgorithmException exception) {
            return Integer.toHexString(raw.hashCode());
        }
    }

    private void invalidateCategoryCache() {
        cacheService.clearPattern("categories:*");
        cacheService.clearPattern("category:*");
        cacheService.clearPattern("products:list:*");
        cacheService.clearPattern("products:filters:*");
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

    private void requireAdmin(String usernameOrEmail) {
        AppUser user = appUserRepository.findByUsernameIgnoreCase(usernameOrEmail)
                .or(() -> appUserRepository.findByEmailIgnoreCase(usernameOrEmail))
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (!Boolean.TRUE.equals(user.getIsStaff())) {
            throw new IllegalArgumentException("Invalid credentials or insufficient permissions");
        }
    }

    private String storeImage(String folder, String subFolder, MultipartFile imageFile) throws IOException {
        String originalName = imageFile.getOriginalFilename() == null ? "image" : imageFile.getOriginalFilename();
        String extension = extensionOf(originalName);
        String baseName = baseNameOf(originalName);

        String safeFolder = sanitizePathPart(folder);
        String safeSubFolder = sanitizePathPart(subFolder);
        String filename = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + "_" + sanitizePathPart(baseName);

        Path uploadDirectory = Path.of("uploads", "media", safeFolder, safeSubFolder);
        Files.createDirectories(uploadDirectory);

        Path target = uploadDirectory.resolve(filename + extension);
        Files.copy(imageFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return "/media/" + safeFolder + "/" + safeSubFolder + "/" + target.getFileName();
    }

    private String extensionOf(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    private String baseNameOf(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex <= 0) {
            return filename;
        }
        return filename.substring(0, dotIndex);
    }

    private String sanitizePathPart(String input) {
        String value = safeString(input).replaceAll("[^a-zA-Z0-9._-]", "_");
        if (value.isBlank()) {
            return "item";
        }
        return value;
    }

    private String serializeFeatures(Object value) {
        try {
            List<String> features = parseFeatures(value);
            return objectMapper.writeValueAsString(features);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid features format.");
        }
    }

    private List<String> parseFeatureList(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(raw, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<String> parseFeatures(Object value) {
        if (value == null) {
            return List.of();
        }

        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).map(String::trim).filter(item -> !item.isBlank()).toList();
        }

        if (value instanceof String text) {
            String trimmed = text.trim();
            if (trimmed.isBlank()) {
                return List.of();
            }
            if (trimmed.startsWith("[")) {
                try {
                    return objectMapper.readValue(trimmed, new TypeReference<>() {
                    });
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Invalid features JSON format.");
                }
            }
            return List.of(trimmed);
        }

        throw new IllegalArgumentException("Invalid features format.");
    }

    private String requireNonBlank(Object value, String message) {
        String normalized = trimToNull(safeString(value));
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private String safeString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String cleanNullableString(Object value) {
        String normalized = trimToNull(safeString(value));
        if (normalized == null || "null".equalsIgnoreCase(normalized)) {
            return null;
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String normalizeStorage(String value) {
        String normalized = trimToNull(value);
        if (normalized == null || "null".equalsIgnoreCase(normalized)) {
            return null;
        }
        return normalized;
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        String text = safeString(value).trim();
        if (text.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Integer parseInt(Object value) {
        if (value == null) {
            return null;
        }
        String text = safeString(value).trim();
        if (text.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Double parseDoubleObject(Object value) {
        if (value == null) {
            return null;
        }
        String text = safeString(value).trim();
        if (text.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private BigDecimal parseDecimal(Object value) {
        if (value == null) {
            return null;
        }
        String text = safeString(value).trim();
        if (text.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Boolean parseBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean boolValue) {
            return boolValue;
        }

        String text = safeString(value).trim().toLowerCase(Locale.ROOT);
        if (text.isBlank()) {
            return null;
        }
        return "true".equals(text) || "1".equals(text) || "yes".equals(text) || "on".equals(text);
    }

    private Boolean parseBooleanNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseBoolean(value);
    }

    private String formatTime(LocalDateTime value) {
        return value == null ? null : ISO_FORMATTER.format(value);
    }
}
