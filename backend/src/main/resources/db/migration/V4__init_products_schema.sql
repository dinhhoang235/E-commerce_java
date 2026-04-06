CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description TEXT NULL,
    image VARCHAR(500) NULL,
    is_active BIT(1) NOT NULL DEFAULT b'1',
    sort_order INT NOT NULL DEFAULT 0,
    parent_id BIGINT NULL,
    product_count INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_slug (slug),
    KEY idx_categories_parent_id (parent_id),
    CONSTRAINT fk_categories_parent_id
        FOREIGN KEY (parent_id) REFERENCES categories (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    category_id BIGINT NOT NULL,
    image VARCHAR(500) NULL,
    rating DOUBLE NOT NULL DEFAULT 0.0,
    reviews INT NOT NULL DEFAULT 0,
    badge VARCHAR(50) NULL,
    description TEXT NULL,
    full_description LONGTEXT NULL,
    features LONGTEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_products_category_id (category_id),
    CONSTRAINT fk_products_category_id
        FOREIGN KEY (category_id) REFERENCES categories (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE product_colors (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    hex_code VARCHAR(7) NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE product_variants (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    color_id BIGINT NOT NULL,
    storage VARCHAR(50) NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    sold INT NOT NULL DEFAULT 0,
    is_in_stock BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_variants_combination (product_id, color_id, storage),
    KEY idx_product_variants_product_id (product_id),
    KEY idx_product_variants_color_id (color_id),
    CONSTRAINT fk_product_variants_product_id
        FOREIGN KEY (product_id) REFERENCES products (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_product_variants_color_id
        FOREIGN KEY (color_id) REFERENCES product_colors (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
