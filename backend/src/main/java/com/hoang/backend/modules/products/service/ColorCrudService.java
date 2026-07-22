package com.hoang.backend.modules.products.service;

import static com.hoang.backend.modules.products.service.ProductUtils.*;

import com.hoang.backend.common.InMemoryCacheService;
import com.hoang.backend.common.crud.BaseCrudService;
import com.hoang.backend.common.exceptions.UnauthorizedException;
import com.hoang.backend.common.exceptions.UserNotFoundException;
import com.hoang.backend.modules.products.dto.ProductColorResponse;
import com.hoang.backend.modules.products.entity.ProductColor;
import com.hoang.backend.modules.products.repository.ProductColorRepository;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ColorCrudService extends BaseCrudService<ProductColor, ProductColorResponse> {

    private static final long COLOR_CACHE_TTL = 3600;

    private final ProductColorRepository productColorRepository;
    private final AppUserRepository appUserRepository;

    public ColorCrudService(AppUserRepository appUserRepository,
                            InMemoryCacheService cacheService,
                            ProductColorRepository productColorRepository) {
        super(cacheService);
        this.appUserRepository = appUserRepository;
        this.productColorRepository = productColorRepository;
    }

    @Override
    protected JpaRepository<ProductColor, Long> getRepository() {
        return productColorRepository;
    }

    @Override
    protected ProductColor createNewEntity() {
        return new ProductColor();
    }

    @Override
    protected String getEntityName() {
        return "Color";
    }

    @Override
    protected ProductColorResponse toResponse(ProductColor color) {
        return ProductMapper.toColorResponse(color);
    }

    @Override
    protected void checkPermissions(String authenticatedUsername) {
        AppUser user = appUserRepository.findByUsernameIgnoreCase(authenticatedUsername)
                .or(() -> appUserRepository.findByEmailIgnoreCase(authenticatedUsername))
                .orElseThrow(() -> new UserNotFoundException(authenticatedUsername));
        if (!Boolean.TRUE.equals(user.getIsStaff())) {
            throw new UnauthorizedException();
        }
    }

    @Override
    protected void invalidateCache() {
        cacheService.delete("product_colors:all");
        cacheService.clearPattern("product:*");
    }

    @Override
    protected void applyPayload(ProductColor color, Map<String, Object> payload) {
        if (payload.containsKey("name") || color.getId() == null) {
            String name = safeString(payload.get("name")).trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Color name is required.");
            }
            color.setName(name);
        }
        if (payload.containsKey("hex_code")) {
            color.setHexCode(cleanNullableString(payload.get("hex_code")));
        }
    }

    @Transactional(readOnly = true)
    public List<ProductColorResponse> listColors() {
        return getOrCache(cacheService, "product_colors:all", COLOR_CACHE_TTL, () ->
            productColorRepository.findAllByOrderByNameAsc().stream()
                .map(ProductMapper::toColorResponse)
                .toList()
        );
    }
}
