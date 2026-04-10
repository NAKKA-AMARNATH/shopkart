CREATE TABLE categories (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id   UUID         REFERENCES categories(id) ON DELETE SET NULL,
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    image_url   VARCHAR(500),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE products (
    id          UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID           NOT NULL REFERENCES categories(id),
    seller_id   UUID           NOT NULL REFERENCES users(id),
    name        VARCHAR(255)   NOT NULL,
    slug        VARCHAR(300)   NOT NULL UNIQUE,
    description TEXT,
    base_price  NUMERIC(10, 2) NOT NULL,
    brand       VARCHAR(100),
    sku         VARCHAR(100)   UNIQUE,
    is_active   BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE product_variants (
    id             UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id     UUID           NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    color          VARCHAR(50),
    size           VARCHAR(50),
    price          NUMERIC(10, 2) NOT NULL,
    stock_quantity INT            NOT NULL DEFAULT 0,
    image_url      VARCHAR(500),
    sku            VARCHAR(100)   UNIQUE,
    created_at     TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE product_images (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID         NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url  VARCHAR(500) NOT NULL,
    is_primary BOOLEAN      NOT NULL DEFAULT FALSE,
    sort_order INT          NOT NULL DEFAULT 0,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_category  ON products(category_id);
CREATE INDEX idx_products_seller    ON products(seller_id);
CREATE INDEX idx_products_slug      ON products(slug);
CREATE INDEX idx_variants_product   ON product_variants(product_id);
CREATE INDEX idx_images_product     ON product_images(product_id);