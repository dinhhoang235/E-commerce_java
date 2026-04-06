package com.hoang.backend.modules.products.repository;

import com.hoang.backend.modules.products.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByOrderBySortOrderAscNameAsc();

    Optional<Category> findBySlugIgnoreCase(String slug);

    List<Category> findByParentId(Long parentId);
}
