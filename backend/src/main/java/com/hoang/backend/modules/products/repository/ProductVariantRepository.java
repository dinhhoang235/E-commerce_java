package com.hoang.backend.modules.products.repository;

import com.hoang.backend.modules.products.entity.ProductVariant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findAllByOrderByCreatedAtDesc();

    List<ProductVariant> findByProductId(Long productId);

    List<ProductVariant> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<ProductVariant> findByProductIdIn(List<Long> productIds);

    boolean existsByProductIdAndColorIdAndStorage(Long productId, Long colorId, String storage);

    Optional<ProductVariant> findByProductIdAndColorIdAndStorage(Long productId, Long colorId, String storage);
}
