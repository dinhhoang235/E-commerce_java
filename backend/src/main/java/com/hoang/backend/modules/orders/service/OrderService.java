package com.hoang.backend.modules.orders.service;

import com.hoang.backend.modules.cart.entity.Cart;
import com.hoang.backend.modules.cart.repository.CartItemRepository;
import com.hoang.backend.modules.cart.repository.CartRepository;
import com.hoang.backend.modules.orders.dto.AdminOrderListResponse;
import com.hoang.backend.modules.orders.dto.OrderCreateItemRequest;
import com.hoang.backend.modules.orders.dto.OrderCreateRequest;
import com.hoang.backend.modules.orders.dto.OrderHistoryResponse;
import com.hoang.backend.modules.orders.dto.OrderItemResponse;
import com.hoang.backend.modules.orders.dto.OrderResponse;
import com.hoang.backend.modules.orders.dto.OrderStatsResponse;
import com.hoang.backend.modules.orders.dto.OrderUserAccountResponse;
import com.hoang.backend.modules.orders.dto.OrderUserResponse;
import com.hoang.backend.modules.orders.dto.ShippingAddressPayload;
import com.hoang.backend.modules.orders.dto.ShippingInfoResponse;
import com.hoang.backend.modules.orders.dto.StatusCountResponse;
import com.hoang.backend.modules.orders.entity.Order;
import com.hoang.backend.modules.orders.entity.OrderItem;
import com.hoang.backend.modules.orders.repository.OrderItemRepository;
import com.hoang.backend.modules.orders.repository.OrderRepository;
import com.hoang.backend.modules.products.entity.Product;
import com.hoang.backend.modules.products.entity.ProductVariant;
import com.hoang.backend.modules.products.repository.ProductVariantRepository;
import com.hoang.backend.modules.users.entity.Account;
import com.hoang.backend.modules.users.entity.Address;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AccountRepository;
import com.hoang.backend.modules.users.repository.AddressRepository;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final Set<String> VALID_STATUSES = Set.of("pending", "processing", "shipped", "completed", "cancelled", "refunded");
    private static final Set<String> USER_CANCELABLE_STATUSES = Set.of("pending", "processing");
    private static final Set<String> VALID_SHIPPING_METHODS = Set.of("standard", "express", "overnight");
    private static final Map<String, BigDecimal> SHIPPING_COSTS = Map.of(
            "standard", BigDecimal.ZERO,
            "express", new BigDecimal("5.00"),
            "overnight", new BigDecimal("10.00")
    );

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AppUserRepository appUserRepository;
    private final AddressRepository addressRepository;
    private final AccountRepository accountRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional(readOnly = true)
    public List<OrderResponse> listMyOrders(String authenticatedUsername) {
        AppUser user = requireUser(authenticatedUsername);
        return orderRepository.findByUserIdOrderByDateDesc(user.getId())
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(String authenticatedUsername, String orderId) {
        AppUser user = requireUser(authenticatedUsername);
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));
        return toOrderResponse(order);
    }

    public OrderResponse createOrder(String authenticatedUsername, OrderCreateRequest request) {
        AppUser user = requireUser(authenticatedUsername);
        ResolvedOrderItems resolved = resolveRequestedItems(user, request == null ? null : request.items(), false);
        Address shippingAddress = resolveShippingAddress(user, request);
        String shippingMethod = normalizeShippingMethod(request == null ? null : request.shipping_method());

        Order order = createOrderInternal(user, resolved.items(), shippingAddress, shippingMethod);

        if (resolved.fromCart()) {
            clearCart(user.getId());
        }

        return toOrderResponse(order);
    }

    public OrderResponse createOrderFromCart(String authenticatedUsername, OrderCreateRequest request) {
        AppUser user = requireUser(authenticatedUsername);
        ResolvedOrderItems resolved = resolveRequestedItems(user, request == null ? null : request.items(), true);
        Address shippingAddress = resolveShippingAddress(user, request);
        String shippingMethod = normalizeShippingMethod(request == null ? null : request.shipping_method());

        Order order = createOrderInternal(user, resolved.items(), shippingAddress, shippingMethod);

        if (resolved.fromCart()) {
            clearCart(user.getId());
        }

        return toOrderResponse(order);
    }

    public OrderResponse updateOrderStatusAsUser(String authenticatedUsername, String orderId, String newStatus) {
        AppUser user = requireUser(authenticatedUsername);
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));

        String normalizedStatus = normalizeStatus(newStatus);
        if (!"cancelled".equals(normalizedStatus) || !"pending".equals(order.getStatus())) {
            throw new IllegalArgumentException("You can only cancel pending orders.");
        }

        applyStatusTransition(order, normalizedStatus);
        return toOrderResponse(orderRepository.save(order));
    }

    public OrderResponse cancelOrder(String authenticatedUsername, String orderId) {
        AppUser user = requireUser(authenticatedUsername);
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));

        if (!USER_CANCELABLE_STATUSES.contains(order.getStatus())) {
            throw new IllegalArgumentException("Cannot cancel order with status: " + order.getStatus());
        }

        applyStatusTransition(order, "cancelled");
        return toOrderResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderHistoryResponse history(String authenticatedUsername, int page, int pageSize) {
        AppUser user = requireUser(authenticatedUsername);
        List<Order> allOrders = orderRepository.findByUserIdOrderByDateDesc(user.getId());

        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int start = Math.min((safePage - 1) * safePageSize, allOrders.size());
        int end = Math.min(start + safePageSize, allOrders.size());

        List<OrderResponse> content = allOrders.subList(start, end).stream().map(this::toOrderResponse).toList();

        return new OrderHistoryResponse(
                content,
                allOrders.size(),
                safePage,
                safePageSize,
                end < allOrders.size()
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> checkPaymentStatus(String authenticatedUsername, String orderId) {
        AppUser user = requireUser(authenticatedUsername);
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));

        String paymentStatus = paymentStatus(order);
        return Map.of(
                "expired", false,
                "status", paymentStatus,
                "is_paid", Boolean.TRUE.equals(order.getIsPaid()),
                "payment_deadline", null,
                "message", "Payment status retrieved successfully"
        );
    }

    @Transactional(readOnly = true)
    public OrderStatsResponse myStats(String authenticatedUsername) {
        AppUser user = requireUser(authenticatedUsername);
        return new OrderStatsResponse(
                orderRepository.findByUserIdOrderByDateDesc(user.getId()).size(),
                orderRepository.countByUserIdAndStatus(user.getId(), "pending"),
                orderRepository.countByUserIdAndStatus(user.getId(), "processing"),
                orderRepository.countByUserIdAndStatus(user.getId(), "shipped"),
                orderRepository.countByUserIdAndStatus(user.getId(), "completed"),
                orderRepository.sumTotalByUserId(user.getId()),
                null,
                null,
                null,
                null
        );
    }

    @Transactional(readOnly = true)
    public AdminOrderListResponse listAdminOrders(String authenticatedUsername, String status, String customer, Integer limit) {
        requireAdmin(authenticatedUsername);
        List<Order> orders = orderRepository.findAllByOrderByDateDesc();

        if (status != null && !status.isBlank()) {
            String normalized = status.trim().toLowerCase(Locale.ROOT);
            orders = orders.stream().filter(order -> normalized.equals(order.getStatus())).toList();
        }

        if (customer != null && !customer.isBlank()) {
            String normalizedCustomer = customer.trim().toLowerCase(Locale.ROOT);
            orders = orders.stream()
                    .filter(order -> customerMatches(order, normalizedCustomer))
                    .toList();
        }

        if (limit != null && limit > 0 && limit < orders.size()) {
            orders = orders.subList(0, limit);
        }

        List<OrderResponse> results = orders.stream().map(this::toOrderResponse).toList();
        return new AdminOrderListResponse(results.size(), results);
    }

    @Transactional(readOnly = true)
    public OrderResponse adminGetOrder(String authenticatedUsername, String orderId) {
        requireAdmin(authenticatedUsername);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));
        return toOrderResponse(order);
    }

    public OrderResponse adminUpdateOrderStatus(String authenticatedUsername, String orderId, String newStatus) {
        requireAdmin(authenticatedUsername);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));

        applyStatusTransition(order, normalizeStatus(newStatus));
        return toOrderResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderStatsResponse adminStats(String authenticatedUsername) {
        requireAdmin(authenticatedUsername);

        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus("pending");
        long processingOrders = orderRepository.countByStatus("processing");
        long shippedOrders = orderRepository.countByStatus("shipped");
        long completedOrders = orderRepository.countByStatus("completed");

        BigDecimal totalRevenue = orderRepository.sumTotal();
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long recentOrders = orderRepository.countByDateGreaterThanEqual(thirtyDaysAgo);
        BigDecimal recentRevenue = orderRepository.sumTotalFromDate(thirtyDaysAgo);
        BigDecimal averageOrderValue = totalOrders == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);

        List<StatusCountResponse> statusBreakdown = List.of(
                new StatusCountResponse("pending", pendingOrders),
                new StatusCountResponse("processing", processingOrders),
                new StatusCountResponse("shipped", shippedOrders),
                new StatusCountResponse("completed", completedOrders),
                new StatusCountResponse("cancelled", orderRepository.countByStatus("cancelled")),
                new StatusCountResponse("refunded", orderRepository.countByStatus("refunded"))
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

    private Order createOrderInternal(AppUser user, List<ResolvedItem> items, Address shippingAddress, String shippingMethod) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("No items in cart to create order");
        }

        BigDecimal total = items.stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setId(generateOrderId());
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setShippingMethod(shippingMethod);
        order.setTotal(total);
        order.setStatus("pending");
        order.setIsPaid(false);
        order = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (ResolvedItem item : items) {
            ProductVariant variant = requireVariant(item.productVariant().getId());
            ensureStock(variant, item.quantity());

            variant.setStock(variant.getStock() - item.quantity());
            variant.setSold((variant.getSold() == null ? 0 : variant.getSold()) + item.quantity());
            variant.setIsInStock(variant.getStock() > 0);
            productVariantRepository.save(variant);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductVariant(variant);
            orderItem.setQuantity(item.quantity());
            orderItem.setPrice(item.price());
            orderItems.add(orderItemRepository.save(orderItem));
        }

        order.setItems(orderItems);
        return order;
    }

    private ResolvedOrderItems resolveRequestedItems(AppUser user, List<OrderCreateItemRequest> requestedItems, boolean forceUseCartIfEmpty) {
        List<OrderCreateItemRequest> items = requestedItems == null ? List.of() : requestedItems;

        if (items.isEmpty() || forceUseCartIfEmpty) {
            List<OrderCreateItemRequest> fromCart = loadItemsFromCart(user.getId());
            if (fromCart.isEmpty()) {
                throw new IllegalArgumentException("No items in cart");
            }
            return new ResolvedOrderItems(resolveItems(fromCart), true);
        }

        return new ResolvedOrderItems(resolveItems(items), false);
    }

    private List<OrderCreateItemRequest> loadItemsFromCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            return List.of();
        }

        return cartItemRepository.findByCartIdOrderByCreatedAtDesc(cart.getId()).stream()
                .map(item -> new OrderCreateItemRequest(item.getProductVariant().getId(), item.getQuantity()))
                .toList();
    }

    private List<ResolvedItem> resolveItems(List<OrderCreateItemRequest> items) {
        List<ResolvedItem> resolved = new ArrayList<>();
        for (OrderCreateItemRequest item : items) {
            if (item == null || item.product_variant_id() == null) {
                throw new IllegalArgumentException("product_variant_id is required");
            }

            int quantity = item.quantity() == null ? 1 : item.quantity();
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }

            ProductVariant variant = requireVariant(item.product_variant_id());
            ensureStock(variant, quantity);
            resolved.add(new ResolvedItem(variant, quantity, variant.getPrice()));
        }
        return resolved;
    }

    private Address resolveShippingAddress(AppUser user, OrderCreateRequest request) {
        if (request == null) {
            return null;
        }

        if (request.shipping_address_id() != null) {
            return addressRepository.findById(request.shipping_address_id())
                    .filter(address -> Objects.equals(address.getUser().getId(), user.getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Shipping address not found."));
        }

        ShippingAddressPayload payload = request.shipping_address();
        if (payload == null) {
            return null;
        }

        String addressLine1 = safe(payload.address_line1());
        String city = safe(payload.city());
        String state = safe(payload.state());
        String zipCode = safe(payload.zip_code());
        String country = safe(payload.country()).isBlank() ? "VN" : safe(payload.country());

        if (addressLine1.isBlank() || city.isBlank() || state.isBlank() || zipCode.isBlank()) {
            throw new IllegalArgumentException("Shipping address is incomplete.");
        }

        Address address = addressRepository
                .findFirstByUserIdAndAddressLine1IgnoreCaseAndCityIgnoreCaseAndStateIgnoreCaseAndZipCodeIgnoreCaseAndCountryIgnoreCase(
                        user.getId(), addressLine1, city, state, zipCode, country)
                .orElseGet(Address::new);

        address.setUser(user);
        address.setFirstName(safe(payload.first_name()));
        address.setLastName(safe(payload.last_name()));
        address.setPhone(safe(payload.phone()));
        address.setAddressLine1(addressLine1);
        address.setCity(city);
        address.setState(state);
        address.setZipCode(zipCode);
        address.setCountry(country);
        if (address.getId() == null) {
            address.setDefault(false);
        }

        return addressRepository.save(address);
    }

    private void clearCart(Long userId) {
        cartRepository.findByUserId(userId)
                .ifPresent(cart -> cartItemRepository.deleteByCartId(cart.getId()));
    }

    private void applyStatusTransition(Order order, String targetStatus) {
        String previousStatus = safe(order.getStatus());
        if (previousStatus.equals(targetStatus)) {
            return;
        }

        if ("cancelled".equals(targetStatus) && !USER_CANCELABLE_STATUSES.contains(previousStatus) && !"shipped".equals(previousStatus)
                && !"completed".equals(previousStatus) && !"refunded".equals(previousStatus)) {
            throw new IllegalArgumentException("Cannot cancel order with status: " + previousStatus);
        }

        List<OrderItem> items = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId());

        if (!"cancelled".equals(previousStatus) && "cancelled".equals(targetStatus)) {
            for (OrderItem item : items) {
                ProductVariant variant = requireVariant(item.getProductVariant().getId());
                variant.setStock((variant.getStock() == null ? 0 : variant.getStock()) + item.getQuantity());
                variant.setSold(Math.max((variant.getSold() == null ? 0 : variant.getSold()) - item.getQuantity(), 0));
                variant.setIsInStock(variant.getStock() > 0);
                productVariantRepository.save(variant);
            }
        }

        if ("cancelled".equals(previousStatus) && !"cancelled".equals(targetStatus)) {
            for (OrderItem item : items) {
                ProductVariant variant = requireVariant(item.getProductVariant().getId());
                ensureStock(variant, item.getQuantity());
                variant.setStock(variant.getStock() - item.getQuantity());
                variant.setSold((variant.getSold() == null ? 0 : variant.getSold()) + item.getQuantity());
                variant.setIsInStock(variant.getStock() > 0);
                productVariantRepository.save(variant);
            }
        }

        order.setStatus(targetStatus);
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId());
        List<OrderItemResponse> items = orderItems.stream().map(this::toOrderItemResponse).toList();
        List<String> products = orderItems.stream().map(this::productLabel).toList();

        BigDecimal shippingCost = shippingCost(order.getShippingMethod());
        BigDecimal subtotal = order.getTotal() == null ? BigDecimal.ZERO : order.getTotal();
        BigDecimal totalWithShipping = subtotal.add(shippingCost);

        AppUser user = order.getUser();
        return new OrderResponse(
                order.getId(),
                customerName(user),
                safe(user.getEmail()),
                products,
                subtotal,
                subtotal,
                shippingCost,
                totalWithShipping,
                safe(order.getStatus()),
                order.getDate() == null ? null : DATE_FORMATTER.format(order.getDate()),
                new ShippingInfoResponse(shippingAddressFormatted(order.getShippingAddress()), shippingMethodLabel(order.getShippingMethod()), shippingCost),
                items,
                Boolean.TRUE.equals(order.getIsPaid()),
                paymentStatus(order),
                hasPendingPayment(order),
                canContinuePayment(order),
                toOrderUserResponse(user)
        );
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        ProductVariant variant = item.getProductVariant();
        Product product = variant.getProduct();

        return new OrderItemResponse(
                item.getId(),
                variant.getId(),
                safe(product.getName()),
                variant.getColor() == null ? "" : safe(variant.getColor().getName()),
                safe(variant.getStorage()),
                item.getQuantity() == null ? 1 : item.getQuantity(),
                item.getPrice() == null ? BigDecimal.ZERO : item.getPrice(),
                variant.getPrice() == null ? BigDecimal.ZERO : variant.getPrice(),
                safe(product.getImage())
        );
    }

    private String productLabel(OrderItem item) {
        ProductVariant variant = item.getProductVariant();
        Product product = variant.getProduct();
        String color = variant.getColor() == null ? "No Color" : safe(variant.getColor().getName());
        String storage = safe(variant.getStorage()).isBlank() ? "No Storage" : safe(variant.getStorage());
        String suffix = item.getQuantity() != null && item.getQuantity() > 1 ? " x" + item.getQuantity() : "";
        return safe(product.getName()) + " (" + color + ", " + storage + ")" + suffix;
    }

    private String customerName(AppUser user) {
        Account account = accountRepository.findByUserId(user.getId()).orElse(null);
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

        if (!safe(user.getUsername()).isBlank()) {
            return user.getUsername();
        }

        if (!safe(user.getEmail()).isBlank()) {
            return user.getEmail();
        }

        return "User " + user.getId();
    }

    private OrderUserResponse toOrderUserResponse(AppUser user) {
        Account account = accountRepository.findByUserId(user.getId()).orElse(null);
        OrderUserAccountResponse accountResponse = account == null
                ? null
                : new OrderUserAccountResponse(safe(account.getFirstName()), safe(account.getLastName()));

        return new OrderUserResponse(
                user.getId(),
                safe(user.getUsername()),
                safe(user.getFirstName()),
                safe(user.getLastName()),
                safe(user.getEmail()),
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
        if (!safe(address.getPhone()).isBlank()) {
            components.add(address.getPhone());
        }
        if (!safe(address.getAddressLine1()).isBlank()) {
            components.add(address.getAddressLine1());
        }

        String cityStateZip = String.join(", ", List.of(safe(address.getCity()), safe(address.getState()), safe(address.getZipCode())).stream()
                .filter(value -> !value.isBlank())
                .toList());
        if (!cityStateZip.isBlank()) {
            components.add(cityStateZip);
        }
        if (!safe(address.getCountry()).isBlank()) {
            components.add(address.getCountryLabel());
        }

        return String.join(" | ", components);
    }

    private String shippingMethodLabel(String shippingMethod) {
        return switch (normalizeShippingMethod(shippingMethod)) {
            case "express" -> "Express Shipping";
            case "overnight" -> "Overnight Shipping";
            default -> "Standard Shipping";
        };
    }

    private BigDecimal shippingCost(String shippingMethod) {
        return SHIPPING_COSTS.getOrDefault(normalizeShippingMethod(shippingMethod), BigDecimal.ZERO);
    }

    private String paymentStatus(Order order) {
        if ("refunded".equals(order.getStatus())) {
            return "refunded";
        }
        if (Boolean.TRUE.equals(order.getIsPaid())) {
            return "success";
        }
        if ("cancelled".equals(order.getStatus())) {
            return "canceled";
        }
        return "no_payment";
    }

    private boolean hasPendingPayment(Order order) {
        return "pending".equals(order.getStatus())
                && !Boolean.TRUE.equals(order.getIsPaid())
                && order.getCheckoutUrl() != null
                && !order.getCheckoutUrl().isBlank();
    }

    private boolean canContinuePayment(Order order) {
        return "pending".equals(order.getStatus()) && !Boolean.TRUE.equals(order.getIsPaid());
    }

    private boolean customerMatches(Order order, String customerKeyword) {
        AppUser user = order.getUser();
        List<String> candidates = new ArrayList<>();
        candidates.add(safe(user.getUsername()));
        candidates.add(safe(user.getFirstName()));
        candidates.add(safe(user.getLastName()));

        Account account = accountRepository.findByUserId(user.getId()).orElse(null);
        if (account != null) {
            candidates.add(safe(account.getFirstName()));
            candidates.add(safe(account.getLastName()));
        }

        return candidates.stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.contains(customerKeyword));
    }

    private ProductVariant requireVariant(Long variantId) {
        return productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Product variant with ID " + variantId + " not found"));
    }

    private void ensureStock(ProductVariant variant, int quantity) {
        int available = variant.getStock() == null ? 0 : variant.getStock();
        if (quantity > available) {
            throw new IllegalArgumentException(
                    "Insufficient stock for variant " + variant.getId() + ". Available: " + available + ", Requested: " + quantity
            );
        }
    }

    private AppUser requireUser(String authenticatedUsername) {
        return appUserRepository.findByUsernameIgnoreCase(authenticatedUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private AppUser requireAdmin(String authenticatedUsername) {
        AppUser user = requireUser(authenticatedUsername);
        if (!Boolean.TRUE.equals(user.getIsStaff())) {
            throw new IllegalArgumentException("You do not have permission to access this resource.");
        }
        return user;
    }

    private String normalizeStatus(String status) {
        String normalized = safe(status).toLowerCase(Locale.ROOT);
        if (!VALID_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Invalid status. Valid choices: " + VALID_STATUSES);
        }
        return normalized;
    }

    private String normalizeShippingMethod(String shippingMethod) {
        String normalized = safe(shippingMethod).toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return "standard";
        }
        if (!VALID_SHIPPING_METHODS.contains(normalized)) {
            throw new IllegalArgumentException("Invalid shipping_method. Valid choices: " + VALID_SHIPPING_METHODS);
        }
        return normalized;
    }

    private String generateOrderId() {
        String orderId = randomOrderId();
        while (orderRepository.existsById(orderId)) {
            orderId = randomOrderId();
        }
        return orderId;
    }

    private String randomOrderId() {
        return "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private String joinName(String firstName, String lastName) {
        String first = safe(firstName);
        String last = safe(lastName);
        if (first.isBlank() && last.isBlank()) {
            return "";
        }
        if (first.isBlank()) {
            return last;
        }
        if (last.isBlank()) {
            return first;
        }
        return first + " " + last;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private record ResolvedItem(ProductVariant productVariant, int quantity, BigDecimal price) {
    }

    private record ResolvedOrderItems(List<ResolvedItem> items, boolean fromCart) {
    }
}
