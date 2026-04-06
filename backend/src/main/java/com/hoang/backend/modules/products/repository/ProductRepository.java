package com.hoang.backend.modules.products.repository;

import com.hoang.backend.modules.products.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByOrderByCreatedAtDesc();

    List<Product> findDistinctByCategoryIdIn(List<Long> categoryIds);

    List<Product> findDistinctByCategoryIdInOrderByRatingDescCreatedAtDesc(List<Long> categoryIds);

    List<Product> findByNameContainingIgnoreCase(String search);

    long countByCategoryId(Long categoryId);
}
