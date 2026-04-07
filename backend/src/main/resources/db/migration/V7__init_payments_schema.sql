CREATE TABLE payment_transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id VARCHAR(20) NOT NULL,
    stripe_checkout_id VARCHAR(255) NOT NULL,
    stripe_payment_intent VARCHAR(255) NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_transactions_checkout (stripe_checkout_id),
    KEY idx_payment_transactions_order_id (order_id),
    KEY idx_payment_transactions_status (status),
    KEY idx_payment_transactions_created_at (created_at),
    CONSTRAINT fk_payment_transactions_order_id
        FOREIGN KEY (order_id) REFERENCES orders (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;