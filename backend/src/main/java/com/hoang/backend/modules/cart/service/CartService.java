package com.hoang.backend.modules.cart.service;

import com.hoang.backend.modules.cart.dto.AddToCartRequest;
import com.hoang.backend.modules.cart.dto.CartCountResponse;
import com.hoang.backend.modules.cart.dto.CartItemActionResponse;
import com.hoang.backend.modules.cart.dto.CartItemResponse;
import com.hoang.backend.modules.cart.dto.CartResponse;
import com.hoang.backend.modules.cart.dto.CartSummaryResponse;
import com.hoang.backend.modules.cart.dto.RemoveCartItemRequest;
import com.hoang.backend.modules.cart.dto.UpdateCartItemRequest;
import com.hoang.backend.modules.cart.entity.Cart;
import com.hoang.backend.modules.cart.entity.CartItem;
import com.hoang.backend.modules.cart.repository.CartItemRepository;
import com.hoang.backend.modules.cart.repository.CartRepository;
import com.hoang.backend.modules.products.dto.ProductColorResponse;
import com.hoang.backend.modules.products.dto.ProductShortResponse;
import com.hoang.backend.modules.products.dto.ProductVariantResponse;
import com.hoang.backend.modules.products.entity.Product;
import com.hoang.backend.modules.products.entity.ProductVariant;
import com.hoang.backend.modules.products.repository.ProductVariantRepository;
import com.hoang.backend.modules.users.entity.AppUser;
import com.hoang.backend.modules.users.repository.AppUserRepository;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AppUserRepository appUserRepository;

    @Transactional(readOnly = true)
    public CartResponse getCart(String authenticatedUsername) {
        Cart cart = getOrCreateCart(authenticatedUsername);
        return toCartResponse(cart);
    }

    public CartItemActionResponse addItem(String authenticatedUsername, AddToCartRequest request) {
        if (request == null || request.product_variant_id() == null) {
            throw new IllegalArgumentException("product_variant_id is required");
        }

        int quantityToAdd = request.quantity() == null ? 1 : request.quantity();
        if (quantityToAdd <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = getOrCreateCart(authenticatedUsername);
        ProductVariant variant = requireVariant(request.product_variant_id());

        CartItem cartItem = cartItemRepository.findByCartIdAndProductVariantId(cart.getId(), variant.getId())
                .orElseGet(() -> {
                    CartItem created = new CartItem();
                    created.setCart(cart);
                    created.setProductVariant(variant);
                    created.setQuantity(0);
                    return created;
                });

        int newQuantity = cartItem.getQuantity() + quantityToAdd;
        ensureStock(variant, newQuantity);

        cartItem.setQuantity(newQuantity);
        CartItem saved = cartItemRepository.save(cartItem);
        touchCart(cart);

        return new CartItemActionResponse(
                "Item added to cart successfully",
                toCartItemResponse(saved)
        );
    }

    public CartItemActionResponse updateItem(String authenticatedUsername, UpdateCartItemRequest request) {
        if (request == null || request.item_id() == null) {
            throw new IllegalArgumentException("item_id is required");
        }
        if (request.quantity() == null) {
            throw new IllegalArgumentException("quantity is required");
        }
        if (request.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = getOrCreateCart(authenticatedUsername);
        CartItem item = cartItemRepository.findByIdAndCartId(request.item_id(), cart.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found."));

        ensureStock(item.getProductVariant(), request.quantity());

        item.setQuantity(request.quantity());
        CartItem saved = cartItemRepository.save(item);
        touchCart(cart);

        return new CartItemActionResponse(
                "Cart item updated successfully",
                toCartItemResponse(saved)
        );
    }

    public void removeItem(String authenticatedUsername, RemoveCartItemRequest request) {
        if (request == null || request.item_id() == null) {
            throw new IllegalArgumentException("item_id is required");
        }

        Cart cart = getOrCreateCart(authenticatedUsername);
        CartItem item = cartItemRepository.findByIdAndCartId(request.item_id(), cart.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found."));

        cartItemRepository.delete(item);
        touchCart(cart);
    }

    public void clearCart(String authenticatedUsername) {
        Cart cart = getOrCreateCart(authenticatedUsername);
        cartItemRepository.deleteByCartId(cart.getId());
        touchCart(cart);
    }

    @Transactional(readOnly = true)
    public CartCountResponse count(String authenticatedUsername) {
        CartResponse response = getCart(authenticatedUsername);
        return new CartCountResponse(response.total_items());
    }

    @Transactional(readOnly = true)
    public CartSummaryResponse summary(String authenticatedUsername) {
        CartResponse response = getCart(authenticatedUsername);
        return new CartSummaryResponse(response.total_items(), response.total_price());
    }

    private Cart getOrCreateCart(String authenticatedUsername) {
        AppUser user = appUserRepository.findByUsernameIgnoreCase(authenticatedUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart created = new Cart();
                    created.setUser(user);
                    return cartRepository.save(created);
                });
    }

    private ProductVariant requireVariant(Long variantId) {
        return productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Product variant does not exist"));
    }

    private void ensureStock(ProductVariant variant, int requestedQuantity) {
        int available = variant.getStock() == null ? 0 : variant.getStock();
        if (requestedQuantity > available) {
            throw new IllegalArgumentException(
                    "Insufficient stock for variant " + variant.getId() + ". Available: " + available + ", Requested: " + requestedQuantity
            );
        }
    }

    private void touchCart(Cart cart) {
        cartRepository.save(cart);
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> items = cartItemRepository.findByCartIdOrderByCreatedAtDesc(cart.getId())
                .stream()
                .map(this::toCartItemResponse)
                .toList();

        int totalItems = items.stream().mapToInt(CartItemResponse::quantity).sum();
        BigDecimal totalPrice = items.stream()
                .map(CartItemResponse::total_price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                items,
                totalItems,
                totalPrice,
                formatTime(cart.getCreatedAt()),
                formatTime(cart.getUpdatedAt())
        );
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        ProductVariant variant = item.getProductVariant();
        BigDecimal price = variant.getPrice() == null ? BigDecimal.ZERO : variant.getPrice();
        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemResponse(
                item.getId(),
                toVariantResponse(variant),
                variant.getId(),
                item.getQuantity(),
                totalPrice,
                formatTime(item.getCreatedAt())
        );
    }

    private ProductVariantResponse toVariantResponse(ProductVariant variant) {
        Product product = variant.getProduct();
        ProductShortResponse productShort = new ProductShortResponse(
                product.getId(),
                safeString(product.getName()),
                safeString(product.getImage()),
                safeString(product.getDescription())
        );

        ProductColorResponse color = variant.getColor() == null
                ? null
                : new ProductColorResponse(
                        variant.getColor().getId(),
                        safeString(variant.getColor().getName()),
                        safeString(variant.getColor().getHexCode())
                );

        int stock = variant.getStock() == null ? 0 : variant.getStock();
        int sold = variant.getSold() == null ? 0 : variant.getSold();

        return new ProductVariantResponse(
                variant.getId(),
                productShort,
                product.getId(),
                color,
                color == null ? null : color.id(),
                safeString(variant.getStorage()),
                variant.getPrice(),
                stock,
                sold,
                Boolean.TRUE.equals(variant.getIsInStock()),
                stock,
                formatTime(variant.getCreatedAt()),
                formatTime(variant.getUpdatedAt())
        );
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private String formatTime(java.time.LocalDateTime value) {
        return value == null ? null : value.format(ISO_FORMATTER);
    }
}