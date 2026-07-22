package com.hoang.backend.modules.products.repository;

import com.hoang.backend.modules.products.entity.ProductVariant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findAllByOrderByCreatedAtDesc();

    List<ProductVariant> findAllByActiveTrueOrderByCreatedAtDesc();

    List<ProductVariant> findByProductId(Long productId);

    List<ProductVariant> findByProductIdAndActiveTrue(Long productId);

    List<ProductVariant> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<ProductVariant> findByProductIdAndActiveTrueOrderByCreatedAtDesc(Long productId);

    List<ProductVariant> findByProductIdIn(List<Long> productIds);

    List<ProductVariant> findByProductIdInAndActiveTrue(List<Long> productIds);

    boolean existsByProductIdAndColorIdAndStorage(Long productId, Long colorId, String storage);

    Optional<ProductVariant> findByProductIdAndColorIdAndStorage(Long productId, Long colorId, String storage);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductVariant v WHERE v.id = :id")
    Optional<ProductVariant> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT DISTINCT v.product.id FROM ProductVariant v WHERE v.product.id IN :productIds AND v.active = true AND v.isInStock = true")
    Set<Long> findProductIdsWithInStockVariant(@Param("productIds") List<Long> productIds);
}
