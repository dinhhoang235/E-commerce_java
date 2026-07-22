package com.hoang.backend.modules.orders.service;

import static com.hoang.backend.common.util.TextUtils.*;

import com.hoang.backend.common.constants.OrderStatus;
import com.hoang.backend.common.constants.PaymentStatus;
import com.hoang.backend.common.constants.ShippingMethod;
import com.hoang.backend.common.dto.PaginatedResponse;
import com.hoang.backend.common.exceptions.OrderNotFoundException;
import com.hoang.backend.common.exceptions.UnauthorizedException;
import com.hoang.backend.common.exceptions.UserNotFoundException;
import com.hoang.backend.common.shipping.ShippingCostCalculator;
import com.hoang.backend.modules.orders.dto.OrderHistoryResponse;
import com.hoang.backend.modules.orders.dto.OrderItemResponse;
import com.hoang.backend.modules.orders.dto.OrderResponse;
import com.hoang.backend.modules.orders.dto.OrderStatsResponse;
import com.hoang.backend.modules.orders.dto.OrderUserAccountResponse;
import com.hoang.backend.modules.orders.dto.OrderUserResponse;
import com.hoang.backend.modules.orders.dto.ShippingInfoResponse;
import com.hoang.backend.modules.orders.dto.StatusCountResponse;
import com.hoang.backend.modules.orders.entity.Order;
import com.hoang.backend.modules.orders.entity.OrderItem;
import com.hoang.backend.modules.orders.repository.OrderItemRepository;
import com.hoang.backend.modules.orders.repository.OrderRepository;
import com.hoang.backend.modules.payments.entity.PaymentTransaction;
import com.hoang.backend.modules.payments.repository.PaymentTransactionRepository;
import com.hoang.backend.modules.products.entity.Product;
import com.hoang.backend.modules.products.entity.ProductVariant;
import com.hoang.backend.modules.users.entity.Account;
import com.hoang.backend.modules.users.entity.Address;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AccountRepository;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final Set<String> VALID_SHIPPING_METHODS = Set.of(ShippingMethod.STANDARD, ShippingMethod.EXPRESS, ShippingMethod.OVERNIGHT);

    private final ShippingCostCalculator shippingCostCalculator;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final AppUserRepository appUserRepository;
    private final AccountRepository accountRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    public PaginatedResponse<OrderResponse> listMyOrders(String authenticatedUsername, int page, int pageSize) {
        AppUser user = requireUser(authenticatedUsername);
        List<Order> allOrders = orderRepository.findByUserIdOrderByDateDesc(user.getId());

        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int start = Math.min((safePage - 1) * safePageSize, allOrders.size());
        int end = Math.min(start + safePageSize, allOrders.size());

        List<Order> pageOrders = allOrders.subList(start, end);
        Map<Long, Account> accountMap = buildAccountMap(pageOrders);
        Map<String, List<PaymentTransaction>> paymentMap = buildPaymentMap(pageOrders);

        List<OrderResponse> content = pageOrders.stream()
                .map(order -> toOrderResponse(order, accountMap, paymentMap)).toList();
        return new PaginatedResponse<>(content, safePage, safePageSize, allOrders.size());
    }

    public OrderResponse getMyOrder(String authenticatedUsername, String orderId) {
        AppUser user = requireUser(authenticatedUsername);
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return toOrderResponse(order);
    }

    public OrderHistoryResponse history(String authenticatedUsername, int page, int pageSize) {
        AppUser user = requireUser(authenticatedUsername);
        List<Order> allOrders = orderRepository.findByUserIdOrderByDateDesc(user.getId());

        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int start = Math.min((safePage - 1) * safePageSize, allOrders.size());
        int end = Math.min(start + safePageSize, allOrders.size());

        List<Order> pageOrders = allOrders.subList(start, end);
        Map<Long, Account> accountMap = buildAccountMap(pageOrders);
        Map<String, List<PaymentTransaction>> paymentMap = buildPaymentMap(pageOrders);

        List<OrderResponse> content = pageOrders.stream()
                .map(order -> toOrderResponse(order, accountMap, paymentMap)).toList();

        return new OrderHistoryResponse(
                content,
                allOrders.size(),
                safePage,
                safePageSize,
                end < allOrders.size()
        );
    }

    public Map<String, Object> checkPaymentStatus(String authenticatedUsername, String orderId) {
        AppUser user = requireUser(authenticatedUsername);
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        String paymentStatus = paymentStatus(order);
        return Map.of(
                "expired", false,
                "status", paymentStatus,
                "is_paid", Boolean.TRUE.equals(order.getIsPaid()),
                "payment_deadline", null,
                "message", "Payment status retrieved successfully"
        );
    }

    public OrderStatsResponse myStats(String authenticatedUsername) {
        AppUser user = requireUser(authenticatedUsername);
        return new OrderStatsResponse(
                orderRepository.findByUserIdOrderByDateDesc(user.getId()).size(),
                orderRepository.countByUserIdAndStatus(user.getId(), OrderStatus.PENDING),
                orderRepository.countByUserIdAndStatus(user.getId(), OrderStatus.PROCESSING),
                orderRepository.countByUserIdAndStatus(user.getId(), OrderStatus.SHIPPED),
                orderRepository.countByUserIdAndStatus(user.getId(), OrderStatus.COMPLETED),
                orderRepository.sumTotalActiveByUserId(user.getId()),
                null,
                null,
                null,
                null
        );
    }

    public PaginatedResponse<OrderResponse> listAdminOrders(String authenticatedUsername, String status, String customer, int page, int pageSize) {
        requireAdmin(authenticatedUsername);
        List<Order> allOrders = orderRepository.findAllByOrderByDateDesc();

        if (status != null && !status.isBlank()) {
            String normalized = status.trim().toLowerCase(Locale.ROOT);
            allOrders = allOrders.stream().filter(order -> normalized.equals(order.getStatus())).toList();
        }

        if (customer != null && !customer.isBlank()) {
            String normalizedCustomer = customer.trim().toLowerCase(Locale.ROOT);
            Map<Long, Account> accountMap = buildAccountMap(allOrders);
            allOrders = allOrders.stream()
                    .filter(order -> customerMatches(order, normalizedCustomer, accountMap))
                    .toList();
        }

        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int start = Math.min((safePage - 1) * safePageSize, allOrders.size());
        int end = Math.min(start + safePageSize, allOrders.size());

        List<Order> pageOrders = allOrders.subList(start, end);
        Map<Long, Account> accountMap = buildAccountMap(pageOrders);
        Map<String, List<PaymentTransaction>> paymentMap = buildPaymentMap(pageOrders);

        List<OrderResponse> results = pageOrders.stream()
                .map(order -> toOrderResponse(order, accountMap, paymentMap)).toList();
        return new PaginatedResponse<>(results, safePage, safePageSize, allOrders.size());
    }

    public OrderResponse adminGetOrder(String authenticatedUsername, String orderId) {
        requireAdmin(authenticatedUsername);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return toOrderResponse(order);
    }

    public OrderStatsResponse adminStats(String authenticatedUsername) {
        requireAdmin(authenticatedUsername);

        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        long processingOrders = orderRepository.countByStatus(OrderStatus.PROCESSING);
        long shippedOrders = orderRepository.countByStatus(OrderStatus.SHIPPED);
        long completedOrders = orderRepository.countByStatus(OrderStatus.COMPLETED);

        BigDecimal totalRevenue = orderRepository.sumTotalActive();
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long recentOrders = orderRepository.countByDateGreaterThanEqual(thirtyDaysAgo);
        BigDecimal recentRevenue = orderRepository.sumTotalActiveFromDate(thirtyDaysAgo);
        BigDecimal averageOrderValue = totalOrders == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);

        List<StatusCountResponse> statusBreakdown = List.of(
                new StatusCountResponse(OrderStatus.PENDING, pendingOrders),
                new StatusCountResponse(OrderStatus.PROCESSING, processingOrders),
                new StatusCountResponse(OrderStatus.SHIPPED, shippedOrders),
                new StatusCountResponse(OrderStatus.COMPLETED, completedOrders),
                new StatusCountResponse(OrderStatus.CANCELLED, orderRepository.countByStatus(OrderStatus.CANCELLED)),
                new StatusCountResponse(OrderStatus.REFUNDED, orderRepository.countByStatus(OrderStatus.REFUNDED))
        );

        return new OrderStatsResponse(
                totalOrders,
                pendingOrders,
                processingOrders,
                shippedOrders,
                completedOrders,
                totalRevenue,
                recentOrders,
                recentRevenue,
                statusBreakdown,
                averageOrderValue
        );
    }

    OrderResponse toOrderResponse(Order order) {
        return toOrderResponse(order, null, null);
    }

    private OrderResponse toOrderResponse(Order order, Map<Long, Account> accountMap, Map<String, List<PaymentTransaction>> paymentMap) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId());
        List<OrderItemResponse> items = orderItems.stream().map(this::toOrderItemResponse).toList();
        List<String> products = orderItems.stream().map(this::productLabel).toList();

        BigDecimal shippingCost = shippingCost(order.getShippingMethod());
        BigDecimal subtotal = order.getTotal() == null ? BigDecimal.ZERO : order.getTotal();
        BigDecimal totalWithShipping = subtotal.add(shippingCost);

        AppUser user = order.getUser();
        return new OrderResponse(
                order.getId(),
                customerName(user, accountMap),
                safeTrim(user.getEmail()),
                products,
                subtotal,
                subtotal,
                shippingCost,
                totalWithShipping,
                safeTrim(order.getStatus()),
                order.getDate() == null ? null : DATE_FORMATTER.format(order.getDate()),
                new ShippingInfoResponse(shippingAddressFormatted(order.getShippingAddress()), shippingMethodLabel(order.getShippingMethod()), shippingCost),
                items,
                Boolean.TRUE.equals(order.getIsPaid()),
                paymentStatus(order, paymentMap),
                hasPendingPayment(order, paymentMap),
                canContinuePayment(order),
                toOrderUserResponse(user, accountMap)
        );
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        ProductVariant variant = item.getProductVariant();
        Product product = variant.getProduct();

        return new OrderItemResponse(
                item.getId(),
                variant.getId(),
                safeTrim(product.getName()),
                variant.getColor() == null ? "" : safeTrim(variant.getColor().getName()),
                safeTrim(variant.getStorage()),
                item.getQuantity() == null ? 1 : item.getQuantity(),
                item.getPrice() == null ? BigDecimal.ZERO : item.getPrice(),
                variant.getPrice() == null ? BigDecimal.ZERO : variant.getPrice(),
                safeTrim(product.getImage())
        );
    }

    private String productLabel(OrderItem item) {
        ProductVariant variant = item.getProductVariant();
        Product product = variant.getProduct();
        String color = variant.getColor() == null ? "No Color" : safeTrim(variant.getColor().getName());
        String storage = safeTrim(variant.getStorage()).isBlank() ? "No Storage" : safeTrim(variant.getStorage());
        String suffix = item.getQuantity() != null && item.getQuantity() > 1 ? " x" + item.getQuantity() : "";
        return safeTrim(product.getName()) + " (" + color + ", " + storage + ")" + suffix;
    }

    private String customerName(AppUser user, Map<Long, Account> accountMap) {
        Account account = accountMap != null ? accountMap.get(user.getId()) : null;
        if (account == null) {
            account = accountRepository.findByUserId(user.getId()).orElse(null);
        }
        if (account != null) {
            String fromAccount = joinName(account.getFirstName(), account.getLastName());
            if (!fromAccount.isBlank()) {
                return fromAccount;
            }
        }

        String fromUser = joinName(user.getFirstName(), user.getLastName());
        if (!fromUser.isBlank()) {
            return fromUser;
        }

        if (!safeTrim(user.getUsername()).isBlank()) {
            return user.getUsername();
        }

        if (!safeTrim(user.getEmail()).isBlank()) {
            return user.getEmail();
        }

        return "User " + user.getId();
    }

    private OrderUserResponse toOrderUserResponse(AppUser user, Map<Long, Account> accountMap) {
        Account account = accountMap != null ? accountMap.get(user.getId()) : null;
        if (account == null) {
            account = accountRepository.findByUserId(user.getId()).orElse(null);
        }
        OrderUserAccountResponse accountResponse = account == null
                ? null
                : new OrderUserAccountResponse(safeTrim(account.getFirstName()), safeTrim(account.getLastName()));

        return new OrderUserResponse(
                user.getId(),
                safeTrim(user.getUsername()),
                safeTrim(user.getFirstName()),
                safeTrim(user.getLastName()),
                safeTrim(user.getEmail()),
                accountResponse
        );
    }

    private String shippingAddressFormatted(Address address) {
        if (address == null) {
            return "No shipping address";
        }

        List<String> components = new ArrayList<>();
        String name = joinName(address.getFirstName(), address.getLastName());
        if (!name.isBlank()) {
            components.add(name);
        }
        if (!safeTrim(address.getPhone()).isBlank()) {
            components.add(address.getPhone());
        }
        if (!safeTrim(address.getAddressLine1()).isBlank()) {
            components.add(address.getAddressLine1());
        }

        String cityStateZip = String.join(", ", List.of(safeTrim(address.getCity()), safeTrim(address.getState()), safeTrim(address.getZipCode())).stream()
                .filter(value -> !value.isBlank())
                .toList());
        if (!cityStateZip.isBlank()) {
            components.add(cityStateZip);
        }
        if (!safeTrim(address.getCountry()).isBlank()) {
            components.add(address.getCountryLabel());
        }

        return String.join(" | ", components);
    }

    private String shippingMethodLabel(String shippingMethod) {
        return shippingCostCalculator.getLabel(normalizeShippingMethod(shippingMethod));
    }

    private BigDecimal shippingCost(String shippingMethod) {
        return shippingCostCalculator.getCost(normalizeShippingMethod(shippingMethod));
    }

    private String paymentStatus(Order order) {
        return paymentStatus(order, null);
    }

    private String paymentStatus(Order order, Map<String, List<PaymentTransaction>> paymentMap) {
        List<PaymentTransaction> txs = paymentMap != null ? paymentMap.get(order.getId()) : null;
        if (txs == null) {
            if (paymentTransactionRepository.findFirstByOrderIdAndStatusOrderByCreatedAtDesc(order.getId(), PaymentStatus.REFUNDED).isPresent()) {
                return PaymentStatus.REFUNDED;
            }
            return paymentTransactionRepository.findFirstByOrderIdOrderByCreatedAtDesc(order.getId())
                    .map(tx -> tx.getStatus() == null || tx.getStatus().isBlank() ? PaymentStatus.NO_PAYMENT : tx.getStatus())
                    .orElse(PaymentStatus.NO_PAYMENT);
        }
        boolean refunded = txs.stream().anyMatch(tx -> PaymentStatus.REFUNDED.equals(tx.getStatus()));
        if (refunded) return PaymentStatus.REFUNDED;
        return txs.stream()
                .findFirst()
                .map(tx -> tx.getStatus() == null || tx.getStatus().isBlank() ? PaymentStatus.NO_PAYMENT : tx.getStatus())
                .orElse(PaymentStatus.NO_PAYMENT);
    }

    private boolean hasPendingPayment(Order order, Map<String, List<PaymentTransaction>> paymentMap) {
        List<PaymentTransaction> txs = paymentMap != null ? paymentMap.get(order.getId()) : null;
        if (txs == null) {
            return paymentTransactionRepository.findFirstByOrderIdAndStatusOrderByCreatedAtDesc(order.getId(), PaymentStatus.PENDING).isPresent();
        }
        return txs.stream().anyMatch(tx -> PaymentStatus.PENDING.equals(tx.getStatus()));
    }

    private boolean canContinuePayment(Order order) {
        return OrderStatus.PENDING.equals(order.getStatus()) && !Boolean.TRUE.equals(order.getIsPaid());
    }

    private boolean customerMatches(Order order, String customerKeyword) {
        return customerMatches(order, customerKeyword, Map.of());
    }

    private boolean customerMatches(Order order, String customerKeyword, Map<Long, Account> accountMap) {
        AppUser user = order.getUser();
        List<String> candidates = new ArrayList<>();
        candidates.add(safeTrim(user.getUsername()));
        candidates.add(safeTrim(user.getFirstName()));
        candidates.add(safeTrim(user.getLastName()));

        Account account = accountMap != null ? accountMap.get(user.getId()) : null;
        if (account == null) {
            account = accountRepository.findByUserId(user.getId()).orElse(null);
        }
        if (account != null) {
            candidates.add(safeTrim(account.getFirstName()));
            candidates.add(safeTrim(account.getLastName()));
        }

        return candidates.stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.contains(customerKeyword));
    }

    private Map<Long, Account> buildAccountMap(List<Order> orders) {
        List<Long> userIds = orders.stream().map(o -> o.getUser().getId()).distinct().toList();
        return accountRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(a -> a.getUser().getId(), a -> a, (a1, a2) -> a1));
    }

    private Map<String, List<PaymentTransaction>> buildPaymentMap(List<Order> orders) {
        List<String> orderIds = orders.stream().map(Order::getId).toList();
        return paymentTransactionRepository.findByOrderIdIn(orderIds).stream()
                .collect(Collectors.groupingBy(tx -> tx.getOrder().getId()));
    }

    private AppUser requireUser(String authenticatedUsername) {
        return appUserRepository.findByUsernameIgnoreCase(authenticatedUsername)
                .orElseThrow(() -> new UserNotFoundException(authenticatedUsername));
    }

    private AppUser requireAdmin(String authenticatedUsername) {
        AppUser user = requireUser(authenticatedUsername);
        if (!Boolean.TRUE.equals(user.getIsStaff())) {
            throw new UnauthorizedException();
        }
        return user;
    }

    private String normalizeShippingMethod(String shippingMethod) {
        String normalized = safeTrim(shippingMethod).toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return ShippingMethod.STANDARD;
        }
        if (!VALID_SHIPPING_METHODS.contains(normalized)) {
            throw new IllegalArgumentException("Invalid shipping_method. Valid choices: " + VALID_SHIPPING_METHODS);
        }
        return normalized;
    }

}
