package com.hoang.backend.modules.payments.service;

import static java.util.Map.*;

import static com.hoang.backend.common.util.TextUtils.*;

import com.hoang.backend.common.constants.OrderStatus;
import com.hoang.backend.common.constants.PaymentStatus;
import com.hoang.backend.common.event.EventPublisher;
import com.hoang.backend.common.event.PaymentSucceededEvent;
import com.hoang.backend.common.shipping.ShippingCostCalculator;
import com.hoang.backend.modules.orders.dto.OrderCreateItemRequest;
import com.hoang.backend.modules.orders.dto.OrderCreateRequest;
import com.hoang.backend.modules.orders.dto.ShippingAddressPayload;
import com.hoang.backend.modules.orders.entity.Order;
import com.hoang.backend.modules.orders.entity.OrderItem;
import com.hoang.backend.modules.orders.repository.OrderItemRepository;
import com.hoang.backend.modules.orders.repository.OrderRepository;
import com.hoang.backend.modules.orders.service.OrderCommandService;
import com.hoang.backend.modules.payments.dto.CartPaymentItemRequest;
import com.hoang.backend.modules.payments.dto.CreateCheckoutFromCartRequest;
import com.hoang.backend.modules.payments.dto.PaymentSessionResponse;
import com.hoang.backend.modules.payments.dto.ShippingAddressRequest;
import com.hoang.backend.modules.payments.entity.PaymentTransaction;
import com.hoang.backend.modules.payments.repository.PaymentTransactionRepository;
import com.hoang.backend.modules.products.entity.ProductVariant;
import com.hoang.backend.modules.products.repository.ProductVariantRepository;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentCommandService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AppUserRepository appUserRepository;
    private final OrderCommandService orderCommandService;
    private final StripeSessionService stripeSessionService;
    private final ShippingCostCalculator shippingCostCalculator;
    private final EventPublisher eventPublisher;

    public synchronized PaymentSessionResponse createCheckoutSession(String authenticatedUsername, String orderId) {
        AppUser user = requireUser(authenticatedUsername);
        Order order = requireOrder(orderId, user.getId());

        if (Boolean.TRUE.equals(order.getIsPaid())) {
            throw new IllegalArgumentException("Order is already paid");
        }

        Optional<PaymentTransaction> existingPending = paymentTransactionRepository
                .findFirstByOrderIdAndStatusOrderByCreatedAtDesc(order.getId(), PaymentStatus.PENDING);

        if (existingPending.isPresent() && notBlank(order.getCheckoutUrl())) {
            PaymentTransaction tx = existingPending.get();
            if (stripeSessionService.isSessionOpen(tx.getStripeCheckoutId())) {
                return new PaymentSessionResponse(order.getCheckoutUrl(), tx.getStripeCheckoutId(), order.getId());
            }
        }

        var session = stripeSessionService.createCheckoutSession(order);
        PaymentTransaction transaction = existingPending.orElseGet(PaymentTransaction::new);
        transaction.setOrder(order);
        transaction.setStripeCheckoutId(session.getId());
        transaction.setAmount(orderTotalWithShipping(order));
        transaction.setStatus(PaymentStatus.PENDING);
        paymentTransactionRepository.save(transaction);

        order.setCheckoutUrl(session.getUrl());
        orderRepository.save(order);

        return new PaymentSessionResponse(session.getUrl(), session.getId(), order.getId());
    }

    public PaymentSessionResponse createCheckoutSessionFromCart(String authenticatedUsername, CreateCheckoutFromCartRequest request) {
        AppUser user = requireUser(authenticatedUsername);
        if (request == null || request.cart_items() == null || request.cart_items().isEmpty()) {
            throw new IllegalArgumentException("Cart items are required");
        }

        cleanupExpiredPendingOrders(user.getId(), 1);

        Order existingPendingOrder = findRecentPendingOrder(user.getId(), 2).orElse(null);
        if (existingPendingOrder != null) {
            Optional<PaymentTransaction> existingPendingTransaction = paymentTransactionRepository
                    .findFirstByOrderIdAndStatusOrderByCreatedAtDesc(existingPendingOrder.getId(), PaymentStatus.PENDING);
            if (existingPendingTransaction.isPresent()
                    && notBlank(existingPendingOrder.getCheckoutUrl())
                    && stripeSessionService.isSessionOpen(existingPendingTransaction.get().getStripeCheckoutId())) {
                return new PaymentSessionResponse(
                        existingPendingOrder.getCheckoutUrl(),
                        existingPendingTransaction.get().getStripeCheckoutId(),
                        existingPendingOrder.getId()
                );
            }
            return createCheckoutSession(authenticatedUsername, existingPendingOrder.getId());
        }

        List<OrderCreateItemRequest> orderItems = resolveOrderItemsFromCartPayload(request.cart_items());
        OrderCreateRequest createRequest = new OrderCreateRequest(
                null,
                toShippingAddressPayload(request.shipping_address()),
                request.shipping_method(),
                orderItems
        );

        String orderId = orderCommandService.createOrderFromCart(authenticatedUsername, createRequest).id();
        return createCheckoutSession(authenticatedUsername, orderId);
    }

    public PaymentSessionResponse continuePayment(String authenticatedUsername, String orderId) {
        AppUser user = requireUser(authenticatedUsername);
        Order order = requireOrder(orderId, user.getId());

        if (Boolean.TRUE.equals(order.getIsPaid())) {
            throw new IllegalArgumentException("Order is already paid");
        }
        if (!OrderStatus.PENDING.equals(order.getStatus())) {
            throw new IllegalArgumentException("Order is not in pending status");
        }

        return createCheckoutSession(authenticatedUsername, orderId);
    }

    public Map<String, Object> verifyPaymentAndCreateOrder(String authenticatedUsername, String sessionId) {
        AppUser user = requireUser(authenticatedUsername);
        if (blank(sessionId)) {
            throw new IllegalArgumentException("Session ID is required");
        }

        var session = stripeSessionService.retrieveSession(sessionId);
        if (!"paid".equalsIgnoreCase(session.getPaymentStatus())) {
            throw new IllegalArgumentException("Payment was not successful");
        }

        PaymentTransaction existingSuccess = paymentTransactionRepository
                .findByStripeCheckoutId(sessionId)
                .filter(tx -> PaymentStatus.SUCCESS.equals(tx.getStatus()))
                .orElse(null);
        if (existingSuccess != null) {
            return of("success", true, "order_id", existingSuccess.getOrder().getId(), "message", "Order already processed for this payment");
        }

        String metadataUserId = session.getMetadata().get("user_id");
        String orderId = session.getMetadata().get("order_id");
        if (blank(orderId)) {
            throw new IllegalArgumentException("Order ID not found in payment session");
        }
        if (blank(metadataUserId) || !metadataUserId.equals(String.valueOf(user.getId()))) {
            throw new IllegalArgumentException("Payment session does not belong to current user");
        }

        Order order = requireOrder(orderId, user.getId());
        BigDecimal expectedAmount = orderTotalWithShipping(order);
        BigDecimal chargedAmount = session.getAmountTotal() != null
                ? BigDecimal.valueOf(session.getAmountTotal()).movePointLeft(2)
                : null;
        if (chargedAmount != null && expectedAmount.compareTo(chargedAmount) != 0) {
            throw new IllegalArgumentException("Payment amount mismatch. Expected: " + expectedAmount + ", Charged: " + chargedAmount);
        }
        order.setStatus(OrderStatus.PROCESSING);
        order.setIsPaid(true);
        order.setCheckoutUrl(null);
        orderRepository.save(order);

        PaymentTransaction transaction = paymentTransactionRepository.findByStripeCheckoutId(sessionId)
                .orElseGet(PaymentTransaction::new);
        transaction.setOrder(order);
        transaction.setStripeCheckoutId(sessionId);
        transaction.setStripePaymentIntent(session.getPaymentIntent());
        transaction.setAmount(orderTotalWithShipping(order));
        transaction.setStatus(PaymentStatus.SUCCESS);
        paymentTransactionRepository.save(transaction);

        eventPublisher.publish(new PaymentSucceededEvent(order.getId(), transaction.getAmount(), "stripe"));

        return of("success", true, "order_id", orderId, "message", "Order updated successfully after payment");
    }

    public Map<String, Object> processFullRefund(String authenticatedUsername, String orderId, String reason) {
        AppUser user = requireUser(authenticatedUsername);
        if (!Boolean.TRUE.equals(user.getIsStaff())) {
            throw new IllegalArgumentException("You do not have permission to process refunds.");
        }
        Order order = requireOrder(orderId, user.getId());

        if (!Boolean.TRUE.equals(order.getIsPaid())) {
            throw new IllegalArgumentException("Order has not been paid yet");
        }
        if (OrderStatus.CANCELLED.equals(order.getStatus())) {
            throw new IllegalArgumentException("Order is already cancelled");
        }
        if (OrderStatus.REFUNDED.equals(order.getStatus())) {
            throw new IllegalArgumentException("Order has already been refunded");
        }

        PaymentTransaction successful = paymentTransactionRepository
                .findFirstByOrderIdAndStatusOrderByCreatedAtDesc(orderId, PaymentStatus.SUCCESS)
                .orElseThrow(() -> new IllegalArgumentException("No successful payment found for this order"));

        if (blank(successful.getStripePaymentIntent())) {
            throw new IllegalArgumentException("Cannot process refund: Payment intent not found");
        }

        String refundReason = blank(reason) ? "Customer requested refund" : reason;
        var refund = stripeSessionService.createRefund(successful.getStripePaymentIntent(), successful.getAmount(), orderId, user.getId(), refundReason);

        successful.setStatus(PaymentStatus.REFUNDED);
        paymentTransactionRepository.save(successful);

        order.setStatus(OrderStatus.REFUNDED);
        order.setIsPaid(false);
        order.setCheckoutUrl(null);
        orderRepository.save(order);

        restoreOrderStock(order.getId());

        return of(
                "success", true, "message", "Full refund processed successfully",
                "order_id", orderId, "refund_id", refund.getId(),
                "refunded_amount", asPlain(successful.getAmount()),
                "order_status", order.getStatus(), "transaction_id", successful.getId()
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> validateCartVariants(List<CartPaymentItemRequest> cartItems) {
        List<CartPaymentItemRequest> safeItems = cartItems == null ? List.of() : cartItems;
        if (safeItems.isEmpty()) {
            return of("valid", true, "message", "No cart items to validate");
        }

        List<Map<String, Object>> validItems = new ArrayList<>();
        List<Map<String, Object>> invalidItems = new ArrayList<>();

        for (CartPaymentItemRequest item : safeItems) {
            ProductVariant variant = resolveVariant(item);
            if (variant == null) {
                invalidItems.add(of("variant_id", item == null ? null : item.product_id(), "error", "Product variant not found"));
                continue;
            }

            Map<String, Object> value = new LinkedHashMap<>();
            value.put("variant_id", variant.getId());
            value.put("product_id", variant.getProduct().getId());
            value.put("product_name", variant.getProduct().getName());
            value.put("color", variant.getColor() == null ? null : variant.getColor().getName());
            value.put("storage", variant.getStorage());
            value.put("price", variant.getPrice());
            value.put("stock", variant.getStock());
            value.put("quantity", item == null || item.quantity() == null ? 1 : item.quantity());
            validItems.add(value);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("valid", invalidItems.isEmpty());
        result.put("total_items", safeItems.size());
        result.put("valid_items", validItems);
        result.put("invalid_items", invalidItems);
        result.put("message", "Validated " + safeItems.size() + " cart items. " + validItems.size() + " valid, " + invalidItems.size() + " invalid.");
        return result;
    }

    private List<OrderCreateItemRequest> resolveOrderItemsFromCartPayload(List<CartPaymentItemRequest> cartItems) {
        List<OrderCreateItemRequest> result = new ArrayList<>();
        for (CartPaymentItemRequest item : cartItems) {
            ProductVariant variant = resolveVariant(item);
            if (variant == null) throw new IllegalArgumentException("Some products in your cart are no longer available");
            result.add(new OrderCreateItemRequest(variant.getId(), item.quantity() == null ? 1 : Math.max(item.quantity(), 1)));
        }
        return result;
    }

    private ProductVariant resolveVariant(CartPaymentItemRequest item) {
        if (item == null || item.product_id() == null) return null;
        Long id = item.product_id();
        Optional<ProductVariant> byVariantId = productVariantRepository.findById(id);
        if (byVariantId.isPresent()) return byVariantId.get();

        String color = parseOption(item.description(), "Color");
        String storage = parseOption(item.description(), "Storage");
        List<ProductVariant> variants = productVariantRepository.findByProductId(id);
        if (variants.isEmpty()) return null;

        for (ProductVariant variant : variants) {
            boolean colorMatch = blank(color) || (variant.getColor() != null && color.equalsIgnoreCase(safe(variant.getColor().getName())));
            boolean storageMatch = blank(storage) || storage.equalsIgnoreCase(safe(variant.getStorage()));
            if (colorMatch && storageMatch) return variant;
        }
        return variants.get(0);
    }

    private String parseOption(String description, String key) {
        if (blank(description)) return "";
        String prefix = key + ":";
        int start = description.toLowerCase(Locale.ROOT).indexOf(prefix.toLowerCase(Locale.ROOT));
        if (start < 0) return "";
        String substring = description.substring(start + prefix.length()).trim();
        int comma = substring.indexOf(',');
        return comma >= 0 ? substring.substring(0, comma).trim() : substring.trim();
    }

    private ShippingAddressPayload toShippingAddressPayload(ShippingAddressRequest request) {
        if (request == null) return null;
        return new ShippingAddressPayload(request.firstName(), request.lastName(), request.phone(), request.address(), request.city(), request.state(), request.zipCode(), request.country());
    }

    private BigDecimal orderTotalWithShipping(Order order) {
        BigDecimal total = order.getTotal() == null ? BigDecimal.ZERO : order.getTotal();
        return total.add(shippingCostCalculator.getCost(order.getShippingMethod()));
    }

    private void restoreOrderStock(String orderId) {
        for (OrderItem item : orderItemRepository.findByOrderIdOrderByIdAsc(orderId)) {
            ProductVariant variant = item.getProductVariant();
            int quantity = item.getQuantity() == null ? 1 : item.getQuantity();
            variant.setStock((variant.getStock() == null ? 0 : variant.getStock()) + quantity);
            variant.setSold(Math.max((variant.getSold() == null ? 0 : variant.getSold()) - quantity, 0));
            variant.setIsInStock((variant.getStock() == null ? 0 : variant.getStock()) > 0);
            productVariantRepository.save(variant);
        }
    }

    private Optional<Order> findRecentPendingOrder(Long userId, int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        return orderRepository.findByUserIdAndStatusOrderByDateDesc(userId, OrderStatus.PENDING).stream()
                .filter(order -> !Boolean.TRUE.equals(order.getIsPaid()))
                .filter(order -> order.getDate() != null && order.getDate().isAfter(cutoff)).findFirst();
    }

    private void cleanupExpiredPendingOrders(Long userId, int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        orderRepository.findByUserIdAndStatusOrderByDateDesc(userId, OrderStatus.PENDING).stream()
                .filter(order -> !Boolean.TRUE.equals(order.getIsPaid()))
                .filter(order -> order.getDate() != null && order.getDate().isBefore(cutoff))
                .forEach(order -> {
                    restoreOrderStock(order.getId());
                    order.setStatus(OrderStatus.CANCELLED);
                    order.setCheckoutUrl(null);
                    orderRepository.save(order);
                    paymentTransactionRepository.findFirstByOrderIdAndStatusOrderByCreatedAtDesc(order.getId(), PaymentStatus.PENDING)
                            .ifPresent(tx -> { tx.setStatus(PaymentStatus.EXPIRED); paymentTransactionRepository.save(tx); });
                });
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

}
