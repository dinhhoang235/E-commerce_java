CREATE TABLE app_users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(150) NOT NULL,
    email VARCHAR(254) NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(150) NULL,
    last_name VARCHAR(150) NULL,
    date_joined DATETIME(6) NOT NULL,
    last_login DATETIME(6) NULL,
    token_version INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_users_username (username),
    UNIQUE KEY uk_app_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    first_name VARCHAR(150) NULL,
    last_name VARCHAR(150) NULL,
    avatar VARCHAR(500) NULL,
    phone VARCHAR(15) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_accounts_user_id (user_id),
    CONSTRAINT fk_accounts_user_id
        FOREIGN KEY (user_id) REFERENCES app_users (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE addresses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    first_name VARCHAR(150) NULL,
    last_name VARCHAR(150) NULL,
    phone VARCHAR(15) NULL,
    address_line1 VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL DEFAULT 'VN',
    created_at DATETIME(6) NOT NULL,
    is_default BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    KEY idx_addresses_user_id (user_id),
    CONSTRAINT fk_addresses_user_id
        FOREIGN KEY (user_id) REFERENCES app_users (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
