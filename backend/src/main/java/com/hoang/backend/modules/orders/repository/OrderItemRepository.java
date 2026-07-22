package com.hoang.backend.modules.orders.repository;

import com.hoang.backend.modules.orders.entity.OrderItem;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @EntityGraph(attributePaths = {"productVariant", "productVariant.product", "productVariant.color"})
    List<OrderItem> findByOrderIdOrderByIdAsc(String orderId);

    @EntityGraph(attributePaths = {"productVariant", "productVariant.product", "productVariant.color"})
    List<OrderItem> findByOrderIdIn(List<String> orderIds);
}
