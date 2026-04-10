CREATE TABLE carts (
    id         UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID      NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE cart_items (
    id                 UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id            UUID           NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_variant_id UUID           NOT NULL REFERENCES product_variants(id),
    quantity           INT            NOT NULL CHECK (quantity > 0),
    unit_price         NUMERIC(10, 2) NOT NULL,
    created_at         TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP      NOT NULL DEFAULT NOW(),
    UNIQUE (cart_id, product_variant_id)
);

CREATE TABLE orders (
    id           UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID           NOT NULL REFERENCES users(id),
    address_id   UUID           NOT NULL REFERENCES addresses(id),
    order_number VARCHAR(50)    NOT NULL UNIQUE,
    status       VARCHAR(30)    NOT NULL DEFAULT 'PENDING',
    subtotal     NUMERIC(10, 2) NOT NULL,
    tax          NUMERIC(10, 2) NOT NULL DEFAULT 0,
    shipping_fee NUMERIC(10, 2) NOT NULL DEFAULT 0,
    total        NUMERIC(10, 2) NOT NULL,
    created_at   TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE order_items (
    id                 UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id           UUID           NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_variant_id UUID           NOT NULL REFERENCES product_variants(id),
    quantity           INT            NOT NULL CHECK (quantity > 0),
    unit_price         NUMERIC(10, 2) NOT NULL,
    total_price        NUMERIC(10, 2) NOT NULL,
    created_at         TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_carts_user        ON carts(user_id);
CREATE INDEX idx_cart_items_cart   ON cart_items(cart_id);
CREATE INDEX idx_orders_user       ON orders(user_id);
CREATE INDEX idx_orders_status     ON orders(status);
CREATE INDEX idx_order_items_order ON order_items(order_id);