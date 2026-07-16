package com.hoang.backend.modules.orders.repository;

import com.hoang.backend.modules.orders.entity.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByUserIdOrderByDateDesc(Long userId);

    List<Order> findByUserIdAndStatusOrderByDateDesc(Long userId, String status);

    Optional<Order> findByIdAndUserId(String id, Long userId);

    List<Order> findAllByOrderByDateDesc();

    long countByStatus(String status);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, String status);

    long countByDateGreaterThanEqual(LocalDateTime fromDate);

    long countByUserIdAndDateGreaterThanEqual(Long userId, LocalDateTime fromDate);

    @Query("select coalesce(sum(o.total), 0) from Order o")
    BigDecimal sumTotal();

    @Query("select coalesce(sum(o.total), 0) from Order o where o.user.id = :userId")
    BigDecimal sumTotalByUserId(@Param("userId") Long userId);

    @Query("select coalesce(sum(o.total), 0) from Order o where o.date >= :fromDate")
    BigDecimal sumTotalFromDate(@Param("fromDate") LocalDateTime fromDate);

    @Query("select coalesce(sum(o.total), 0) from Order o where o.status not in ('cancelled', 'refunded')")
    BigDecimal sumTotalActive();

    @Query("select coalesce(sum(o.total), 0) from Order o where o.status not in ('cancelled', 'refunded') and o.user.id = :userId")
    BigDecimal sumTotalActiveByUserId(@Param("userId") Long userId);

    @Query("select coalesce(sum(o.total), 0) from Order o where o.status not in ('cancelled', 'refunded') and o.date >= :fromDate")
    BigDecimal sumTotalActiveFromDate(@Param("fromDate") LocalDateTime fromDate);

    @Query("select p.name, coalesce(sum(oi.quantity), 0), coalesce(sum(oi.price * oi.quantity), 0) " +
            "from OrderItem oi join oi.productVariant pv join pv.product p " +
            "group by p.name order by sum(oi.price * oi.quantity) desc")
    List<Object[]> findTopProducts();
}
