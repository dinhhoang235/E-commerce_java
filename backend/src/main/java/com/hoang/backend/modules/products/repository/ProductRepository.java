package com.hoang.backend.modules.products.repository;

import com.hoang.backend.modules.products.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"category", "images"})
    List<Product> findAllByActiveTrue();

    @EntityGraph(attributePaths = {"category", "images"})
    List<Product> findAllByActiveTrueOrderByCreatedAtDesc();

    List<Product> findDistinctByCategoryIdIn(List<Long> categoryIds);

    @EntityGraph(attributePaths = {"category", "images"})
    List<Product> findDistinctByActiveTrueAndCategoryIdIn(List<Long> categoryIds);

    List<Product> findDistinctByCategoryIdInOrderByRatingDescCreatedAtDesc(List<Long> categoryIds);

    @EntityGraph(attributePaths = {"category", "images"})
    List<Product> findDistinctByActiveTrueAndCategoryIdInOrderByRatingDescCreatedAtDesc(List<Long> categoryIds);

    List<Product> findByNameContainingIgnoreCase(String search);

    List<Product> findByActiveTrueAndNameContainingIgnoreCase(String search);

    long countByCategoryId(Long categoryId);
}
