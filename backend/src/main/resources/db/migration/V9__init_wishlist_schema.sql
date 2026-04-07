CREATE TABLE wishlists (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wishlists_user_id (user_id),
    CONSTRAINT fk_wishlists_user_id
        FOREIGN KEY (user_id) REFERENCES app_users (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE wishlist_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    wishlist_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    added_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wishlist_items_wishlist_product (wishlist_id, product_id),
    KEY idx_wishlist_items_wishlist_id (wishlist_id),
    KEY idx_wishlist_items_product_id (product_id),
    CONSTRAINT fk_wishlist_items_wishlist_id
        FOREIGN KEY (wishlist_id) REFERENCES wishlists (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_items_product_id
        FOREIGN KEY (product_id) REFERENCES products (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
