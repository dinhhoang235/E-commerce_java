CREATE TABLE carts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_carts_user_id (user_id),
    CONSTRAINT fk_carts_user_id
        FOREIGN KEY (user_id) REFERENCES app_users (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cart_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    product_variant_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cart_items_cart_variant (cart_id, product_variant_id),
    KEY idx_cart_items_cart_id (cart_id),
    KEY idx_cart_items_product_variant_id (product_variant_id),
    CONSTRAINT fk_cart_items_cart_id
        FOREIGN KEY (cart_id) REFERENCES carts (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product_variant_id
        FOREIGN KEY (product_variant_id) REFERENCES product_variants (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;