package com.hoang.backend.modules.products.repository;

import com.hoang.backend.modules.products.entity.ProductColor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductColorRepository extends JpaRepository<ProductColor, Long> {

	java.util.List<ProductColor> findAllByOrderByNameAsc();
}
