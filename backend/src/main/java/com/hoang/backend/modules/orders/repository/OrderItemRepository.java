package com.hoang.backend.modules.orders.repository;

import com.hoang.backend.modules.orders.entity.OrderItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderIdOrderByIdAsc(String orderId);
}
