CREATE TABLE orders (
    id VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL,
    shipping_address_id BIGINT NULL,
    shipping_method VARCHAR(20) NOT NULL DEFAULT 'standard',
    total DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    date DATETIME(6) NOT NULL,
    is_paid BIT(1) NOT NULL DEFAULT b'0',
    checkout_url VARCHAR(500) NULL,
    PRIMARY KEY (id),
    KEY idx_orders_user_id (user_id),
    KEY idx_orders_status (status),
    KEY idx_orders_date (date),
    KEY idx_orders_shipping_address_id (shipping_address_id),
    CONSTRAINT fk_orders_user_id
        FOREIGN KEY (user_id) REFERENCES app_users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_orders_shipping_address_id
        FOREIGN KEY (shipping_address_id) REFERENCES addresses (id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id VARCHAR(20) NOT NULL,
    product_variant_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_order_items_order_id (order_id),
    KEY idx_order_items_product_variant_id (product_variant_id),
    CONSTRAINT fk_order_items_order_id
        FOREIGN KEY (order_id) REFERENCES orders (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product_variant_id
        FOREIGN KEY (product_variant_id) REFERENCES product_variants (id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;