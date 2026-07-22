package com.hoang.backend.modules.orders.service;

import com.hoang.backend.common.exceptions.InsufficientStockException;
import com.hoang.backend.common.exceptions.InvalidOrderStatusException;
import com.hoang.backend.common.exceptions.OrderNotFoundException;
import com.hoang.backend.common.exceptions.UnauthorizedException;
import com.hoang.backend.common.exceptions.UserNotFoundException;
import com.hoang.backend.modules.cart.repository.CartItemRepository;
import com.hoang.backend.modules.cart.repository.CartRepository;
import static com.hoang.backend.common.util.TextUtils.*;

import com.hoang.backend.common.constants.OrderStatus;
import com.hoang.backend.common.constants.ShippingMethod;
import com.hoang.backend.common.event.EventPublisher;
import com.hoang.backend.common.event.OrderCreatedEvent;
import com.hoang.backend.common.event.OrderStatusChangedEvent;
import com.hoang.backend.modules.orders.dto.OrderCreateItemRequest;
import com.hoang.backend.modules.orders.dto.OrderCreateRequest;
import com.hoang.backend.modules.orders.dto.OrderResponse;
import com.hoang.backend.modules.orders.dto.ShippingAddressPayload;
import com.hoang.backend.modules.orders.entity.Order;
import com.hoang.backend.modules.orders.entity.OrderItem;
import com.hoang.backend.modules.orders.repository.OrderItemRepository;
import com.hoang.backend.modules.orders.repository.OrderRepository;
import com.hoang.backend.modules.products.entity.ProductVariant;
import com.hoang.backend.modules.products.repository.ProductVariantRepository;
import com.hoang.backend.modules.users.entity.Address;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AddressRepository;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderCommandService {

    private static final Set<String> VALID_STATUSES = Set.of(OrderStatus.PENDING, OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.COMPLETED, OrderStatus.CANCELLED, OrderStatus.REFUNDED);
    private static final Set<String> USER_CANCELABLE_STATUSES = Set.of(OrderStatus.PENDING, OrderStatus.PROCESSING);
    private static final Set<String> VALID_SHIPPING_METHODS = Set.of(ShippingMethod.STANDARD, ShippingMethod.EXPRESS, ShippingMethod.OVERNIGHT);

    private final EventPublisher eventPublisher;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AppUserRepository appUserRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderQueryService orderQueryService;

    public OrderResponse createOrder(String authenticatedUsername, OrderCreateRequest request) {
        AppUser user = requireUser(authenticatedUsername);
        ResolvedOrderItems resolved = resolveRequestedItems(user, request == null ? null : request.items(), false);
        Address shippingAddress = resolveShippingAddress(user, request);
        String shippingMethod = normalizeShippingMethod(request == null ? null : request.shipping_method());

        Order order = createOrderInternal(user, resolved.items(), shippingAddress, shippingMethod);

        if (resolved.fromCart()) {
            clearCart(user.getId());
        }

        eventPublisher.publish(new OrderCreatedEvent(order.getId(), String.valueOf(user.getId()), order.getTotal()));

        return orderQueryService.toOrderResponse(order);
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

        eventPublisher.publish(new OrderCreatedEvent(order.getId(), String.valueOf(user.getId()), order.getTotal()));

        return orderQueryService.toOrderResponse(order);
    }

    public OrderResponse updateOrderStatusAsUser(String authenticatedUsername, String orderId, String newStatus) {
        AppUser user = requireUser(authenticatedUsername);
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        String normalizedStatus = normalizeStatus(newStatus);
        if (!OrderStatus.CANCELLED.equals(normalizedStatus)) {
            throw new IllegalArgumentException("You can only cancel an order.");
        }
        if (!USER_CANCELABLE_STATUSES.contains(order.getStatus())) {
            throw new InvalidOrderStatusException("You can only cancel pending or processing orders.");
        }

        applyStatusTransition(order, normalizedStatus);
        return orderQueryService.toOrderResponse(order);
    }

    public OrderResponse cancelOrder(String authenticatedUsername, String orderId) {
        AppUser user = requireUser(authenticatedUsername);
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!USER_CANCELABLE_STATUSES.contains(order.getStatus())) {
            throw new InvalidOrderStatusException("Cannot cancel order with status: " + order.getStatus());
        }

        applyStatusTransition(order, OrderStatus.CANCELLED);
        return orderQueryService.toOrderResponse(order);
    }

    public OrderResponse adminUpdateOrderStatus(String authenticatedUsername, String orderId, String newStatus) {
        requireAdmin(authenticatedUsername);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        applyStatusTransition(order, normalizeStatus(newStatus));
        return orderQueryService.toOrderResponse(order);
    }

    Order createOrderInternal(AppUser user, List<ResolvedItem> items, Address shippingAddress, String shippingMethod) {
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
        order.setStatus(OrderStatus.PENDING);
        order.setIsPaid(false);
        order = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (ResolvedItem item : items) {
            ProductVariant variant = requireVariantWithLock(item.productVariant().getId());
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

        if (!items.isEmpty()) {
            return new ResolvedOrderItems(resolveItems(items), false);
        }

        if (forceUseCartIfEmpty) {
            List<OrderCreateItemRequest> fromCart = loadItemsFromCart(user.getId());
            if (fromCart.isEmpty()) {
                throw new IllegalArgumentException("No items in cart");
            }
            return new ResolvedOrderItems(resolveItems(fromCart), true);
        }

        throw new IllegalArgumentException("No items in cart");
    }

    private List<OrderCreateItemRequest> loadItemsFromCart(Long userId) {
        com.hoang.backend.modules.cart.entity.Cart cart = cartRepository.findByUserId(userId).orElse(null);
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

        String addressLine1 = safeTrim(payload.address_line1());
        String city = safeTrim(payload.city());
        String state = safeTrim(payload.state());
        String zipCode = safeTrim(payload.zip_code());
        String country = safeTrim(payload.country()).isBlank() ? "VN" : safeTrim(payload.country());

        if (addressLine1.isBlank() || city.isBlank() || state.isBlank() || zipCode.isBlank()) {
            throw new IllegalArgumentException("Shipping address is incomplete.");
        }

        Address address = addressRepository
                .findFirstByUserIdAndAddressLine1IgnoreCaseAndCityIgnoreCaseAndStateIgnoreCaseAndZipCodeIgnoreCaseAndCountryIgnoreCase(
                        user.getId(), addressLine1, city, state, zipCode, country)
                .orElseGet(Address::new);

        address.setUser(user);
        address.setFirstName(safeTrim(payload.first_name()));
        address.setLastName(safeTrim(payload.last_name()));
        address.setPhone(safeTrim(payload.phone()));
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

    void applyStatusTransition(Order order, String targetStatus) {
        String previousStatus = safeTrim(order.getStatus());
        if (previousStatus.equals(targetStatus)) {
            return;
        }

        if (OrderStatus.CANCELLED.equals(targetStatus) && !USER_CANCELABLE_STATUSES.contains(previousStatus) && !OrderStatus.SHIPPED.equals(previousStatus)) {
            throw new InvalidOrderStatusException("Cannot cancel order with status: " + previousStatus);
        }

        List<OrderItem> items = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId());

        if (!OrderStatus.CANCELLED.equals(previousStatus) && OrderStatus.CANCELLED.equals(targetStatus)) {
            for (OrderItem item : items) {
                ProductVariant variant = requireVariant(item.getProductVariant().getId());
                variant.setStock((variant.getStock() == null ? 0 : variant.getStock()) + item.getQuantity());
                variant.setSold(Math.max((variant.getSold() == null ? 0 : variant.getSold()) - item.getQuantity(), 0));
                variant.setIsInStock(variant.getStock() > 0);
                productVariantRepository.save(variant);
            }
        }

        if (OrderStatus.CANCELLED.equals(previousStatus) && !OrderStatus.CANCELLED.equals(targetStatus)) {
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
        orderRepository.save(order);

        eventPublisher.publish(new OrderStatusChangedEvent(order.getId(), previousStatus, targetStatus));
    }

    private ProductVariant requireVariant(Long variantId) {
        return productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Product variant with ID " + variantId + " not found"));
    }

    private ProductVariant requireVariantWithLock(Long variantId) {
        return productVariantRepository.findByIdWithLock(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Product variant with ID " + variantId + " not found"));
    }

    private void ensureStock(ProductVariant variant, int quantity) {
        int available = variant.getStock() == null ? 0 : variant.getStock();
        if (quantity > available) {
            throw new InsufficientStockException(variant.getId(), available, quantity);
        }
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

    private String normalizeStatus(String status) {
        String normalized = safeTrim(status).toLowerCase(Locale.ROOT);
        if (!VALID_STATUSES.contains(normalized)) {
            throw new InvalidOrderStatusException("Invalid status. Valid choices: " + VALID_STATUSES);
        }
        return normalized;
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

    record ResolvedItem(ProductVariant productVariant, int quantity, BigDecimal price) {
    }

    record ResolvedOrderItems(List<ResolvedItem> items, boolean fromCart) {
    }
}
