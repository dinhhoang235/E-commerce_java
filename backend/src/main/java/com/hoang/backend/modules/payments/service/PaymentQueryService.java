package com.hoang.backend.modules.payments.service;

import com.hoang.backend.common.constants.OrderStatus;
import com.hoang.backend.common.constants.PaymentStatus;
import com.hoang.backend.modules.orders.entity.Order;
import com.hoang.backend.modules.orders.repository.OrderRepository;
import com.hoang.backend.modules.payments.entity.PaymentTransaction;
import com.hoang.backend.modules.payments.repository.PaymentTransactionRepository;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;
    private final AppUserRepository appUserRepository;

    public Map<String, Object> paymentStatus(String authenticatedUsername, String orderId) {
        AppUser user = requireUser(authenticatedUsername);
        Order order = requireOrder(orderId, user.getId());

        PaymentTransaction transaction = paymentTransactionRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId).orElse(null);
        if (transaction == null) {
            return Map.of("status", "no_payment", "order_status", order.getStatus(), "is_paid", Boolean.TRUE.equals(order.getIsPaid()));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", transaction.getStatus());
        result.put("order_status", order.getStatus());
        result.put("is_paid", Boolean.TRUE.equals(order.getIsPaid()));
        result.put("amount", asPlain(transaction.getAmount()));
        result.put("created_at", formatTime(transaction.getCreatedAt()));
        return result;
    }

    @Transactional
    public Map<String, Object> refundStatus(String authenticatedUsername, String orderId) {
        AppUser user = requireUser(authenticatedUsername);
        Order order = requireOrder(orderId, user.getId());

        List<PaymentTransaction> transactions = paymentTransactionRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
        PaymentTransaction refunded = transactions.stream().filter(tx -> PaymentStatus.REFUNDED.equals(tx.getStatus())).findFirst().orElse(null);
        PaymentTransaction success = transactions.stream().filter(tx -> PaymentStatus.SUCCESS.equals(tx.getStatus())).findFirst().orElse(null);

        if (success == null) {
            return Map.of("refund_status", "no_payment", "message", "No successful payment found for this order");
        }

        if (refunded != null) {
            return Map.of(
                    "refund_status", PaymentStatus.REFUNDED, "order_status", order.getStatus(),
                    "refunded_amount", asPlain(refunded.getAmount().abs()),
                    "refund_date", formatTime(refunded.getCreatedAt()),
                    "original_amount", asPlain(success.getAmount()),
                    "refund_transaction_id", refunded.getId()
            );
        }

        boolean eligibleForRefund = Boolean.TRUE.equals(order.getIsPaid())
                && !OrderStatus.CANCELLED.equals(order.getStatus()) && PaymentStatus.SUCCESS.equals(success.getStatus());

        return Map.of(
                "refund_status", "not_refunded", "order_status", order.getStatus(),
                "is_paid", Boolean.TRUE.equals(order.getIsPaid()),
                "eligible_for_refund", eligibleForRefund,
                "original_amount", asPlain(success.getAmount()),
                "payment_date", formatTime(success.getCreatedAt())
        );
    }

    public String resolvePaymentStatus(Order order) {
        PaymentTransaction refunded = paymentTransactionRepository
                .findFirstByOrderIdAndStatusOrderByCreatedAtDesc(order.getId(), PaymentStatus.REFUNDED).orElse(null);
        if (refunded != null) return PaymentStatus.REFUNDED;
        return paymentTransactionRepository.findFirstByOrderIdOrderByCreatedAtDesc(order.getId())
                .map(PaymentTransaction::getStatus).orElse("no_payment");
    }

    public boolean hasPendingPayment(Order order) {
        return paymentTransactionRepository.findFirstByOrderIdAndStatusOrderByCreatedAtDesc(order.getId(), PaymentStatus.PENDING).isPresent();
    }

    public List<Map<String, Object>> listAdminTransactions() {
        return paymentTransactionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toAdminPaymentPayload).toList();
    }

    public Map<String, Object> adminPaymentStats() {
        return Map.of(
                "total_transactions", paymentTransactionRepository.count(),
                "total_amount", paymentTransactionRepository.sumAmountByStatus(PaymentStatus.SUCCESS),
                "successful_transactions", paymentTransactionRepository.countByStatus(PaymentStatus.SUCCESS),
                "pending_transactions", paymentTransactionRepository.countByStatus(PaymentStatus.PENDING),
                "failed_transactions", paymentTransactionRepository.countByStatus(PaymentStatus.FAILED),
                "refunded_transactions", paymentTransactionRepository.countByStatus(PaymentStatus.REFUNDED)
        );
    }

    private Map<String, Object> toAdminPaymentPayload(PaymentTransaction transaction) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", transaction.getId());
        
        // Include order and customer info
        if (transaction.getOrder() != null) {
            payload.put("order_id", transaction.getOrder().getId());
            
            // Include customer info from order's user
            if (transaction.getOrder().getUser() != null) {
                var user = transaction.getOrder().getUser();
                Map<String, Object> customer = new LinkedHashMap<>();
                customer.put("first_name", user.getFirstName());
                customer.put("last_name", user.getLastName());
                customer.put("email", user.getEmail());
                
                Map<String, Object> orderData = new LinkedHashMap<>();
                orderData.put("id", transaction.getOrder().getId());
                orderData.put("user", customer);
                payload.put("order", orderData);
            } else {
                payload.put("order", Map.of("id", transaction.getOrder().getId()));
            }
        } else {
            payload.put("order_id", null);
        }
        
        payload.put("stripe_checkout_id", transaction.getStripeCheckoutId());
        payload.put("stripe_payment_intent", transaction.getStripePaymentIntent());
        payload.put("amount", asPlain(transaction.getAmount()));
        payload.put("status", transaction.getStatus());
        payload.put("created_at", formatTime(transaction.getCreatedAt()));
        return payload;
    }

    private Order requireOrder(String orderId, Long userId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));
    }

    private AppUser requireUser(String authenticatedUsername) {
        return appUserRepository.findByUsernameIgnoreCase(authenticatedUsername)
                .or(() -> appUserRepository.findByEmailIgnoreCase(authenticatedUsername))
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private String formatTime(LocalDateTime time) { return time == null ? null : ISO_FORMATTER.format(time); }
    private String asPlain(BigDecimal value) { return value == null ? "0" : value.setScale(2, RoundingMode.HALF_UP).toPlainString(); }
}
