ALTER TABLE app_users
    ADD COLUMN is_staff BIT(1) NOT NULL DEFAULT b'0' AFTER token_version,
    ADD COLUMN is_superuser BIT(1) NOT NULL DEFAULT b'0' AFTER is_staff;

CREATE TABLE store_settings (
    id BIGINT NOT NULL,
    store_name VARCHAR(255) NOT NULL DEFAULT 'Apple Store',
    store_description TEXT NOT NULL,
    store_email VARCHAR(254) NOT NULL DEFAULT 'contact@applestore.com',
    store_phone VARCHAR(20) NOT NULL DEFAULT '+1 (555) 123-4567',
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    timezone VARCHAR(50) NOT NULL DEFAULT 'America/New_York',
    email_notifications BIT(1) NOT NULL DEFAULT b'1',
    order_notifications BIT(1) NOT NULL DEFAULT b'1',
    inventory_alerts BIT(1) NOT NULL DEFAULT b'1',
    maintenance_mode BIT(1) NOT NULL DEFAULT b'0',
    allow_guest_checkout BIT(1) NOT NULL DEFAULT b'1',
    require_email_verification BIT(1) NOT NULL DEFAULT b'0',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO store_settings (
    id,
    store_name,
    store_description,
    store_email,
    store_phone,
    currency,
    timezone,
    email_notifications,
    order_notifications,
    inventory_alerts,
    maintenance_mode,
    allow_guest_checkout,
    require_email_verification,
    created_at,
    updated_at
)
VALUES (
    1,
    'Apple Store',
    'Your trusted destination for the latest Apple products',
    'contact@applestore.com',
    '+1 (555) 123-4567',
    'USD',
    'America/New_York',
    b'1',
    b'1',
    b'1',
    b'0',
    b'1',
    b'0',
    NOW(6),
    NOW(6)
);
