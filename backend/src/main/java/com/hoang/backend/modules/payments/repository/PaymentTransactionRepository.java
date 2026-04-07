package com.hoang.backend.modules.payments.repository;

import com.hoang.backend.modules.payments.entity.PaymentTransaction;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByOrderIdOrderByCreatedAtDesc(String orderId);

    Optional<PaymentTransaction> findFirstByOrderIdOrderByCreatedAtDesc(String orderId);

    Optional<PaymentTransaction> findFirstByOrderIdAndStatusOrderByCreatedAtDesc(String orderId, String status);

    Optional<PaymentTransaction> findByStripeCheckoutId(String stripeCheckoutId);

    Optional<PaymentTransaction> findFirstByStripePaymentIntentAndStatusOrderByCreatedAtDesc(String stripePaymentIntent, String status);

    long countByStatus(String status);

    @Query("select coalesce(sum(p.amount), 0) from PaymentTransaction p")
    BigDecimal sumAmount();

    @Query("select coalesce(sum(p.amount), 0) from PaymentTransaction p where p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") String status);

    List<PaymentTransaction> findAllByOrderByCreatedAtDesc();
}
