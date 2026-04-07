package com.hoang.backend.modules.payments.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoang.backend.modules.orders.dto.OrderCreateItemRequest;
import com.hoang.backend.modules.orders.dto.OrderCreateRequest;
import com.hoang.backend.modules.orders.dto.ShippingAddressPayload;
import com.hoang.backend.modules.orders.entity.Order;
import com.hoang.backend.modules.orders.entity.OrderItem;
import com.hoang.backend.modules.orders.repository.OrderItemRepository;
import com.hoang.backend.modules.orders.repository.OrderRepository;
import com.hoang.backend.modules.orders.service.OrderService;
import com.hoang.backend.modules.payments.dto.CartPaymentItemRequest;
import com.hoang.backend.modules.payments.dto.CreateCheckoutFromCartRequest;
import com.hoang.backend.modules.payments.dto.PaymentSessionResponse;
import com.hoang.backend.modules.payments.dto.ShippingAddressRequest;
import com.hoang.backend.modules.payments.entity.PaymentTransaction;
import com.hoang.backend.modules.payments.repository.PaymentTransactionRepository;
import com.hoang.backend.modules.products.entity.ProductVariant;
import com.hoang.backend.modules.products.repository.ProductVariantRepository;
import com.hoang.backend.modules.users.entity.Account;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AccountRepository;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AppUserRepository appUserRepository;
    private final AccountRepository accountRepository;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret:}")
    private String stripeWebhookSecret;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public PaymentSessionResponse createCheckoutSession(String authenticatedUsername, String orderId) {
        AppUser user = requireUser(authenticatedUsername);
        Order order = requireOrder(orderId, user.getId());

        if (Boolean.TRUE.equals(order.getIsPaid())) {
            throw new IllegalArgumentException("Order is already paid");
        }

        Optional<PaymentTransaction> existingPending = paymentTransactionRepository
                .findFirstByOrderIdAndStatusOrderByCreatedAtDesc(order.getId(), "pending");

        if (existingPending.isPresent() && notBlank(order.getCheckoutUrl())) {
            PaymentTransaction tx = existingPending.get();
            if (isSessionOpen(tx.getStripeCheckoutId())) {
                return new PaymentSessionResponse(order.getCheckoutUrl(), tx.getStripeCheckoutId(), order.getId());
            }
        }

        Session session = createStripeSession(order);
        PaymentTransaction transaction = existingPending.orElseGet(PaymentTransaction::new);
        transaction.setOrder(order);
        transaction.setStripeCheckoutId(session.getId());
        transaction.setAmount(orderTotalWithShipping(order));
        transaction.setStatus("pending");
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
                    .findFirstByOrderIdAndStatusOrderByCreatedAtDesc(existingPendingOrder.getId(), "pending");
            if (existingPendingTransaction.isPresent()
                    && notBlank(existingPendingOrder.getCheckoutUrl())
                    && isSessionOpen(existingPendingTransaction.get().getStripeCheckoutId())) {
                return new PaymentSessionResponse(
                        existingPendingOrder.getCheckoutUrl(),
                        existingPendingTransaction.get().getStripeCheckoutId(),
                        existingPendingOrder.getId()
                );
            }
        }

        List<OrderCreateItemRequest> orderItems = resolveOrderItemsFromCartPayload(request.cart_items());
        OrderCreateRequest createRequest = new OrderCreateRequest(
                null,
                toShippingAddressPayload(request.shipping_address()),
                request.shipping_method(),
                orderItems
        );

        String orderId = orderService.createOrderFromCart(authenticatedUsername, createRequest).id();
        return createCheckoutSession(authenticatedUsername, orderId);
    }

    public PaymentSessionResponse continuePayment(String authenticatedUsername, String orderId) {
        AppUser user = requireUser(authenticatedUsername);
        Order order = requireOrder(orderId, user.getId());

        if (Boolean.TRUE.equals(order.getIsPaid())) {
            throw new IllegalArgumentException("Order is already paid");
        }
        if (!"pending".equals(order.getStatus())) {
            throw new IllegalArgumentException("Order is not in pending status");
        }

        return createCheckoutSession(authenticatedUsername, orderId);
    }

    public Map<String, Object> verifyPaymentAndCreateOrder(String authenticatedUsername, String sessionId) {
        AppUser user = requireUser(authenticatedUsername);
        if (blank(sessionId)) {
            throw new IllegalArgumentException("Session ID is required");
        }

        Session session = retrieveSession(sessionId);
        if (!"paid".equalsIgnoreCase(session.getPaymentStatus())) {
            throw new IllegalArgumentException("Payment was not successful");
        }

        PaymentTransaction existingSuccess = paymentTransactionRepository
                .findByStripeCheckoutId(sessionId)
                .filter(tx -> "success".equals(tx.getStatus()))
                .orElse(null);
        if (existingSuccess != null) {
            return Map.of(
                    "success", true,
                    "order_id", existingSuccess.getOrder().getId(),
                    "message", "Order already processed for this payment"
            );
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
        order.setStatus("processing");
        order.setIsPaid(true);
        order.setCheckoutUrl(null);
        orderRepository.save(order);

        PaymentTransaction transaction = paymentTransactionRepository.findByStripeCheckoutId(sessionId)
                .orElseGet(PaymentTransaction::new);
        transaction.setOrder(order);
        transaction.setStripeCheckoutId(sessionId);
        transaction.setStripePaymentIntent(session.getPaymentIntent());
        transaction.setAmount(orderTotalWithShipping(order));
        transaction.setStatus("success");
        paymentTransactionRepository.save(transaction);

        return Map.of(
                "success", true,
                "order_id", orderId,
                "message", "Order updated successfully after payment"
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> paymentStatus(String authenticatedUsername, String orderId) {
        AppUser user = requireUser(authenticatedUsername);
        Order order = requireOrder(orderId, user.getId());

        PaymentTransaction transaction = paymentTransactionRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId).orElse(null);
        if (transaction == null) {
            return Map.of(
                    "status", "no_payment",
                    "order_status", order.getStatus(),
                    "is_paid", Boolean.TRUE.equals(order.getIsPaid())
            );
        }

        return Map.of(
                "status", transaction.getStatus(),
                "order_status", order.getStatus(),
                "is_paid", Boolean.TRUE.equals(order.getIsPaid()),
                "amount", asPlain(transaction.getAmount()),
                "created_at", formatTime(transaction.getCreatedAt())
        );
    }

    public Map<String, Object> processFullRefund(String authenticatedUsername, String orderId, String reason) {
        AppUser user = requireUser(authenticatedUsername);
        Order order = requireOrder(orderId, user.getId());

        if (!Boolean.TRUE.equals(order.getIsPaid())) {
            throw new IllegalArgumentException("Order has not been paid yet");
        }
        if ("cancelled".equals(order.getStatus())) {
            throw new IllegalArgumentException("Order is already cancelled");
        }
        if ("refunded".equals(order.getStatus())) {
            throw new IllegalArgumentException("Order has already been refunded");
        }

        PaymentTransaction successful = paymentTransactionRepository
                .findFirstByOrderIdAndStatusOrderByCreatedAtDesc(orderId, "success")
                .orElseThrow(() -> new IllegalArgumentException("No successful payment found for this order"));

        if (blank(successful.getStripePaymentIntent())) {
            throw new IllegalArgumentException("Cannot process refund: Payment intent not found");
        }

        String refundReason = blank(reason) ? "Customer requested refund" : reason;
        Refund refund = createStripeRefund(successful.getStripePaymentIntent(), successful.getAmount(), orderId, user.getId(), refundReason);

        successful.setStatus("refunded");
        paymentTransactionRepository.save(successful);

        order.setStatus("refunded");
        order.setIsPaid(false);
        order.setCheckoutUrl(null);
        orderRepository.save(order);

        restoreOrderStock(order.getId());

        return Map.of(
                "success", true,
                "message", "Full refund processed successfully",
                "order_id", orderId,
                "refund_id", refund.getId(),
                "refunded_amount", asPlain(successful.getAmount()),
                "order_status", order.getStatus(),
                "transaction_id", successful.getId()
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> refundStatus(String authenticatedUsername, String orderId) {
        AppUser user = requireUser(authenticatedUsername);
        Order order = requireOrder(orderId, user.getId());

        List<PaymentTransaction> transactions = paymentTransactionRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
        PaymentTransaction refunded = transactions.stream().filter(tx -> "refunded".equals(tx.getStatus())).findFirst().orElse(null);
        PaymentTransaction success = transactions.stream().filter(tx -> "success".equals(tx.getStatus())).findFirst().orElse(null);

        if (success == null) {
            return Map.of(
                    "refund_status", "no_payment",
                    "message", "No successful payment found for this order"
            );
        }

        if (refunded != null) {
            return Map.of(
                    "refund_status", "refunded",
                    "order_status", order.getStatus(),
                    "refunded_amount", asPlain(refunded.getAmount().abs()),
                    "refund_date", formatTime(refunded.getCreatedAt()),
                    "original_amount", asPlain(success.getAmount()),
                    "refund_transaction_id", refunded.getId()
            );
        }

        boolean eligibleForRefund = Boolean.TRUE.equals(order.getIsPaid())
                && !"cancelled".equals(order.getStatus())
                && "success".equals(success.getStatus());

        return Map.of(
                "refund_status", "not_refunded",
                "order_status", order.getStatus(),
                "is_paid", Boolean.TRUE.equals(order.getIsPaid()),
                "eligible_for_refund", eligibleForRefund,
                "original_amount", asPlain(success.getAmount()),
                "payment_date", formatTime(success.getCreatedAt())
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> validateCartVariants(List<CartPaymentItemRequest> cartItems) {
        List<CartPaymentItemRequest> safeItems = cartItems == null ? List.of() : cartItems;
        if (safeItems.isEmpty()) {
            return Map.of(
                    "valid", true,
                    "message", "No cart items to validate"
            );
        }

        List<Map<String, Object>> validItems = new ArrayList<>();
        List<Map<String, Object>> invalidItems = new ArrayList<>();

        for (CartPaymentItemRequest item : safeItems) {
            ProductVariant variant = resolveVariant(item);
            if (variant == null) {
                invalidItems.add(Map.of(
                        "variant_id", item == null ? null : item.product_id(),
                        "error", "Product variant not found"
                ));
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

    public void handleWebhook(String payload, String signature) {
        if (blank(payload)) {
            return;
        }

        JsonNode node;
        try {
            node = objectMapper.readTree(payload);
        } catch (Exception exception) {
            return;
        }

        String eventType = node.path("type").asText("");
        if (blank(eventType)) {
            return;
        }

        if ("checkout.session.completed".equals(eventType)) {
            String sessionId = node.path("data").path("object").path("id").asText(null);
            if (notBlank(sessionId)) {
                paymentTransactionRepository.findByStripeCheckoutId(sessionId).ifPresent(transaction -> {
                    Order order = transaction.getOrder();
                    order.setStatus("processing");
                    order.setIsPaid(true);
                    order.setCheckoutUrl(null);
                    orderRepository.save(order);

                    transaction.setStatus("success");
                    paymentTransactionRepository.save(transaction);
                });
            }
        }
    }

    @Transactional(readOnly = true)
    public String resolvePaymentStatus(Order order) {
        PaymentTransaction refunded = paymentTransactionRepository
                .findFirstByOrderIdAndStatusOrderByCreatedAtDesc(order.getId(), "refunded")
                .orElse(null);
        if (refunded != null) {
            return "refunded";
        }

        return paymentTransactionRepository.findFirstByOrderIdOrderByCreatedAtDesc(order.getId())
                .map(PaymentTransaction::getStatus)
                .orElse("no_payment");
    }

    @Transactional(readOnly = true)
    public boolean hasPendingPayment(Order order) {
        return paymentTransactionRepository
                .findFirstByOrderIdAndStatusOrderByCreatedAtDesc(order.getId(), "pending")
                .isPresent();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listAdminTransactions() {
        List<PaymentTransaction> transactions = paymentTransactionRepository.findAllByOrderByCreatedAtDesc();
        return transactions.stream().map(this::toAdminPaymentPayload).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> adminPaymentStats() {
        long total = paymentTransactionRepository.count();
        long success = paymentTransactionRepository.countByStatus("success");
        long pending = paymentTransactionRepository.countByStatus("pending");
        long failed = paymentTransactionRepository.countByStatus("failed");
        long refunded = paymentTransactionRepository.countByStatus("refunded");

        return Map.of(
                "total_transactions", total,
                "total_amount", paymentTransactionRepository.sumAmount(),
                "successful_transactions", success,
                "pending_transactions", pending,
                "failed_transactions", failed,
                "refunded_transactions", refunded
        );
    }

    private Session createStripeSession(Order order) {
        configureStripe();

        List<OrderItem> items = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId());
        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/payment/cancel")
                .putMetadata("order_id", order.getId())
                .putMetadata("user_id", String.valueOf(order.getUser().getId()))
                .setCustomerEmail(order.getUser().getEmail());

        for (OrderItem item : items) {
            String name = safe(item.getProductVariant().getProduct().getName());
            String description = safe(item.getProductVariant().getProduct().getDescription());
            long amountInCents = item.getPrice().multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();

            SessionCreateParams.LineItem.PriceData.ProductData.Builder productBuilder = SessionCreateParams.LineItem.PriceData.ProductData
                    .builder()
                    .setName(name.isBlank() ? "Product" : name);
            if (notBlank(description)) {
                productBuilder.setDescription(description.length() > 500 ? description.substring(0, 500) : description);
            }

            SessionCreateParams.LineItem line = SessionCreateParams.LineItem.builder()
                    .setQuantity((long) (item.getQuantity() == null ? 1 : item.getQuantity()))
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("usd")
                            .setUnitAmount(amountInCents)
                            .setProductData(productBuilder.build())
                            .build())
                    .build();
            builder.addLineItem(line);
        }

        BigDecimal shippingCost = shippingCost(order.getShippingMethod());
        if (shippingCost.compareTo(BigDecimal.ZERO) > 0) {
            long shippingCents = shippingCost.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();
            builder.addLineItem(SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("usd")
                            .setUnitAmount(shippingCents)
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(shippingMethodLabel(order.getShippingMethod()))
                                    .setDescription(shippingMethodLabel(order.getShippingMethod()) + " delivery")
                                    .build())
                            .build())
                    .build());
        }

        try {
            return Session.create(builder.build());
        } catch (StripeException exception) {
            throw new IllegalArgumentException("Payment processing error: " + exception.getMessage());
        }
    }

    private Refund createStripeRefund(String paymentIntent, BigDecimal amount, String orderId, Long userId, String reason) {
        configureStripe();
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();

        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntent)
                .setAmount(amountInCents)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .putMetadata("order_id", orderId)
                .putMetadata("user_id", String.valueOf(userId))
                .putMetadata("reason", reason)
                .putMetadata("refund_type", "full_refund")
                .build();

        try {
            return Refund.create(params);
        } catch (StripeException exception) {
            throw new IllegalArgumentException("Refund processing failed: " + exception.getMessage());
        }
    }

    private Session retrieveSession(String sessionId) {
        configureStripe();
        try {
            return Session.retrieve(sessionId);
        } catch (StripeException exception) {
            throw new IllegalArgumentException("Invalid payment session");
        }
    }

    private boolean isSessionOpen(String sessionId) {
        if (blank(sessionId)) {
            return false;
        }
        try {
            Session session = retrieveSession(sessionId);
            return "open".equalsIgnoreCase(session.getStatus());
        } catch (Exception exception) {
            return false;
        }
    }

    private void configureStripe() {
        if (blank(stripeSecretKey)) {
            throw new IllegalArgumentException("Stripe secret key is not configured");
        }
        Stripe.apiKey = stripeSecretKey;
    }

    private List<OrderCreateItemRequest> resolveOrderItemsFromCartPayload(List<CartPaymentItemRequest> cartItems) {
        List<OrderCreateItemRequest> result = new ArrayList<>();
        for (CartPaymentItemRequest item : cartItems) {
            ProductVariant variant = resolveVariant(item);
            if (variant == null) {
                throw new IllegalArgumentException("Some products in your cart are no longer available");
            }

            int quantity = item.quantity() == null ? 1 : Math.max(item.quantity(), 1);
            result.add(new OrderCreateItemRequest(variant.getId(), quantity));
        }
        return result;
    }

    private ProductVariant resolveVariant(CartPaymentItemRequest item) {
        if (item == null || item.product_id() == null) {
            return null;
        }

        Long id = item.product_id();
        Optional<ProductVariant> byVariantId = productVariantRepository.findById(id);
        if (byVariantId.isPresent()) {
            return byVariantId.get();
        }

        String color = parseOption(item.description(), "Color");
        String storage = parseOption(item.description(), "Storage");
        List<ProductVariant> variants = productVariantRepository.findByProductId(id);
        if (variants.isEmpty()) {
            return null;
        }

        for (ProductVariant variant : variants) {
            boolean colorMatch = blank(color)
                    || (variant.getColor() != null && color.equalsIgnoreCase(safe(variant.getColor().getName())));
            boolean storageMatch = blank(storage)
                    || storage.equalsIgnoreCase(safe(variant.getStorage()));
            if (colorMatch && storageMatch) {
                return variant;
            }
        }

        return variants.get(0);
    }

    private String parseOption(String description, String key) {
        if (blank(description)) {
            return "";
        }
        String prefix = key + ":";
        int start = description.toLowerCase(Locale.ROOT).indexOf(prefix.toLowerCase(Locale.ROOT));
        if (start < 0) {
            return "";
        }
        String substring = description.substring(start + prefix.length()).trim();
        int comma = substring.indexOf(',');
        return comma >= 0 ? substring.substring(0, comma).trim() : substring.trim();
    }

    private ShippingAddressPayload toShippingAddressPayload(ShippingAddressRequest request) {
        if (request == null) {
            return null;
        }
        return new ShippingAddressPayload(
                request.firstName(),
                request.lastName(),
                request.phone(),
                request.address(),
                request.city(),
                request.state(),
                request.zipCode(),
                request.country()
        );
    }

    private BigDecimal orderTotalWithShipping(Order order) {
        BigDecimal total = order.getTotal() == null ? BigDecimal.ZERO : order.getTotal();
        return total.add(shippingCost(order.getShippingMethod()));
    }

    private BigDecimal shippingCost(String shippingMethod) {
        String normalized = safe(shippingMethod).toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "express" -> new BigDecimal("5.00");
            case "overnight" -> new BigDecimal("10.00");
            default -> BigDecimal.ZERO;
        };
    }

    private String shippingMethodLabel(String shippingMethod) {
        return switch (safe(shippingMethod).toLowerCase(Locale.ROOT)) {
            case "express" -> "Express Shipping";
            case "overnight" -> "Overnight Shipping";
            default -> "Standard Shipping";
        };
    }

    private void restoreOrderStock(String orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderIdOrderByIdAsc(orderId);
        for (OrderItem item : items) {
            ProductVariant variant = item.getProductVariant();
            int stock = variant.getStock() == null ? 0 : variant.getStock();
            int sold = variant.getSold() == null ? 0 : variant.getSold();
            int quantity = item.getQuantity() == null ? 1 : item.getQuantity();

            variant.setStock(stock + quantity);
            variant.setSold(Math.max(sold - quantity, 0));
            variant.setIsInStock((variant.getStock() == null ? 0 : variant.getStock()) > 0);
            productVariantRepository.save(variant);
        }
    }

    private Optional<Order> findRecentPendingOrder(Long userId, int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        return orderRepository.findByUserIdAndStatusOrderByDateDesc(userId, "pending").stream()
                .filter(order -> !Boolean.TRUE.equals(order.getIsPaid()))
                .filter(order -> order.getDate() != null && order.getDate().isAfter(cutoff))
                .findFirst();
    }

    private void cleanupExpiredPendingOrders(Long userId, int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        List<Order> expired = orderRepository.findByUserIdAndStatusOrderByDateDesc(userId, "pending").stream()
                .filter(order -> !Boolean.TRUE.equals(order.getIsPaid()))
                .filter(order -> order.getDate() != null && order.getDate().isBefore(cutoff))
                .toList();

        for (Order order : expired) {
            order.setStatus("cancelled");
            order.setCheckoutUrl(null);
            orderRepository.save(order);
            paymentTransactionRepository.findFirstByOrderIdAndStatusOrderByCreatedAtDesc(order.getId(), "pending")
                    .ifPresent(tx -> {
                        tx.setStatus("failed");
                        paymentTransactionRepository.save(tx);
                    });
        }
    }

    private Map<String, Object> toAdminPaymentPayload(PaymentTransaction transaction) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", transaction.getId());
        payload.put("order_id", transaction.getOrder().getId());
        payload.put("stripe_checkout_id", transaction.getStripeCheckoutId());
        payload.put("stripe_payment_intent", transaction.getStripePaymentIntent());
        payload.put("amount", asPlain(transaction.getAmount()));
        payload.put("status", transaction.getStatus());
        payload.put("created_at", formatTime(transaction.getCreatedAt()));

        AppUser user = transaction.getOrder().getUser();
        Account account = accountRepository.findByUserId(user.getId()).orElse(null);

        Map<String, Object> orderPayload = new LinkedHashMap<>();
        orderPayload.put("id", transaction.getOrder().getId());

        Map<String, Object> userPayload = new LinkedHashMap<>();
        userPayload.put("first_name", account == null ? safe(user.getFirstName()) : safe(account.getFirstName()));
        userPayload.put("last_name", account == null ? safe(user.getLastName()) : safe(account.getLastName()));
        userPayload.put("email", safe(user.getEmail()));
        orderPayload.put("user", userPayload);
        payload.put("order", orderPayload);

        return payload;
    }

    private Order requireOrder(String orderId, Long userId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    private AppUser requireUser(String authenticatedUsername) {
        return appUserRepository.findByUsernameIgnoreCase(authenticatedUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? null : ISO_FORMATTER.format(time);
    }

    private String asPlain(BigDecimal value) {
        BigDecimal safe = value == null ? BigDecimal.ZERO : value;
        return safe.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean blank(String value) {
        return value == null || value.trim().isBlank();
    }

    private boolean notBlank(String value) {
        return !blank(value);
    }
}
