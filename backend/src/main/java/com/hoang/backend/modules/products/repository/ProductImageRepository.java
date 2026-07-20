package com.hoang.backend.modules.products.repository;

import com.hoang.backend.modules.products.entity.ProductImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdAndActiveTrueOrderBySortOrderAsc(Long productId);

    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);

    long countByProductId(Long productId);
}
