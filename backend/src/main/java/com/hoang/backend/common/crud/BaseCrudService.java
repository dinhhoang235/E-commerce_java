package com.hoang.backend.common.crud;

import com.hoang.backend.common.InMemoryCacheService;
import java.util.Map;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hoang.backend.common.exceptions.UserNotFoundException;

public abstract class BaseCrudService<E, R> {

    protected final InMemoryCacheService cacheService;

    protected BaseCrudService(InMemoryCacheService cacheService) {
        this.cacheService = cacheService;
    }

    protected abstract JpaRepository<E, Long> getRepository();
    protected abstract R toResponse(E entity);
    protected abstract void invalidateCache();
    protected abstract E createNewEntity();
    protected abstract void applyPayload(E entity, Map<String, Object> payload);
    protected abstract String getEntityName();
    protected abstract void checkPermissions(String authenticatedUsername);

    public R create(String authenticatedUsername, Map<String, Object> payload) {
        checkPermissions(authenticatedUsername);
        E entity = createNewEntity();
        applyPayload(entity, payload);
        E saved = getRepository().save(entity);
        invalidateCache();
        return toResponse(saved);
    }

    public R update(String authenticatedUsername, Long id, Map<String, Object> payload) {
        checkPermissions(authenticatedUsername);
        E entity = getRepository().findById(id)
                .orElseThrow(() -> new IllegalArgumentException(getEntityName() + " not found."));
        applyPayload(entity, payload);
        E saved = getRepository().save(entity);
        invalidateCache();
        return toResponse(saved);
    }

    public void delete(String authenticatedUsername, Long id) {
        checkPermissions(authenticatedUsername);
        E entity = getRepository().findById(id)
                .orElseThrow(() -> new IllegalArgumentException(getEntityName() + " not found."));
        getRepository().delete(entity);
        invalidateCache();
    }

    protected String safeString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    protected String cleanNullableString(Object value) {
        String text = safeString(value).trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text)) {
            return null;
        }
        return text;
    }
}
