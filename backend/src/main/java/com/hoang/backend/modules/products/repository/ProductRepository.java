package com.hoang.backend.modules.products.repository;

import com.hoang.backend.modules.products.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByOrderByCreatedAtDesc();

    List<Product> findAllByActiveTrue();

    List<Product> findAllByActiveTrueOrderByCreatedAtDesc();

    List<Product> findDistinctByCategoryIdIn(List<Long> categoryIds);

    List<Product> findDistinctByActiveTrueAndCategoryIdIn(List<Long> categoryIds);

    List<Product> findDistinctByCategoryIdInOrderByRatingDescCreatedAtDesc(List<Long> categoryIds);

    List<Product> findDistinctByActiveTrueAndCategoryIdInOrderByRatingDescCreatedAtDesc(List<Long> categoryIds);

    List<Product> findByNameContainingIgnoreCase(String search);

    List<Product> findByActiveTrueAndNameContainingIgnoreCase(String search);

    long countByCategoryId(Long categoryId);
}
