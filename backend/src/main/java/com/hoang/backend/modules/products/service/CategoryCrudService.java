package com.hoang.backend.modules.products.service;

import static com.hoang.backend.modules.products.service.ProductUtils.*;

import com.hoang.backend.common.InMemoryCacheService;
import com.hoang.backend.common.crud.BaseCrudService;
import com.hoang.backend.common.storage.MinioService;
import com.hoang.backend.modules.products.dto.CategoryResponse;
import com.hoang.backend.modules.products.entity.Category;
import com.hoang.backend.modules.products.repository.CategoryRepository;
import com.hoang.backend.modules.products.repository.ProductRepository;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CategoryCrudService extends BaseCrudService<Category, CategoryResponse> {

    private static final long CATEGORY_CACHE_TTL = 1800;

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final AppUserRepository appUserRepository;
    private final MinioService minioService;

    public CategoryCrudService(AppUserRepository appUserRepository,
                               InMemoryCacheService cacheService,
                               CategoryRepository categoryRepository,
                               ProductRepository productRepository,
                               MinioService minioService) {
        super(cacheService);
        this.appUserRepository = appUserRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.minioService = minioService;
    }

    @Override
    protected JpaRepository<Category, Long> getRepository() {
        return categoryRepository;
    }

    @Override
    protected Category createNewEntity() {
        return new Category();
    }

    @Override
    protected String getEntityName() {
        return "Category";
    }

    @Override
    protected CategoryResponse toResponse(Category category) {
        return toCategoryResponse(category);
    }

    @Override
    protected void checkPermissions(String authenticatedUsername) {
        AppUser user = appUserRepository.findByUsernameIgnoreCase(authenticatedUsername)
                .or(() -> appUserRepository.findByEmailIgnoreCase(authenticatedUsername))
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!Boolean.TRUE.equals(user.getIsStaff())) {
            throw new IllegalArgumentException("Invalid credentials or insufficient permissions");
        }
    }

    @Override
    protected void invalidateCache() {
        cacheService.clearPattern("categories:*");
        cacheService.clearPattern("category:*");
        cacheService.clearPattern("products:list:*");
        cacheService.clearPattern("products:filters:*");
    }

    @Override
    protected void applyPayload(Category category, Map<String, Object> payload) {
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
        if (payload.containsKey("image") && cleanNullableString(payload.get("image")) == null) {
            category.setImage(null);
        }
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return getOrCache(cacheService, "categories:all", CATEGORY_CACHE_TTL, () ->
            categoryRepository.findAllByOrderBySortOrderAscNameAsc().stream()
                .map(this::toCategoryResponse)
                .toList()
        );
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {
        return getOrCache(cacheService, "category:" + id, CATEGORY_CACHE_TTL, () -> {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found."));
            return toCategoryResponse(category);
        });
    }

    public CategoryResponse createWithImage(String authenticatedUsername, Map<String, Object> payload,
                                             MultipartFile imageFile) throws Exception {
        checkPermissions(authenticatedUsername);
        Category category = createNewEntity();
        applyPayload(category, payload);
        if (imageFile != null && !imageFile.isEmpty()) {
            category.setImage(minioService.storeImage("categories", safeString(category.getSlug()), imageFile));
        }
        CategoryResponse response = toResponse(categoryRepository.save(category));
        invalidateCache();
        return response;
    }

    public CategoryResponse updateWithImage(String authenticatedUsername, Long id,
                                             Map<String, Object> payload,
                                             MultipartFile imageFile) throws Exception {
        checkPermissions(authenticatedUsername);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));
        applyPayload(category, payload);
        if (imageFile != null && !imageFile.isEmpty()) {
            category.setImage(minioService.storeImage("categories", safeString(category.getSlug()), imageFile));
        }
        CategoryResponse response = toResponse(categoryRepository.save(category));
        invalidateCache();
        return response;
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return ProductMapper.toCategoryResponse(category, productRepository);
    }
}
