package com.hoang.backend.modules.cart.repository;

import com.hoang.backend.modules.cart.entity.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartIdOrderByCreatedAtDesc(Long cartId);

    Optional<CartItem> findByIdAndCartId(Long id, Long cartId);

    Optional<CartItem> findByCartIdAndProductVariantId(Long cartId, Long productVariantId);

    void deleteByCartId(Long cartId);
}