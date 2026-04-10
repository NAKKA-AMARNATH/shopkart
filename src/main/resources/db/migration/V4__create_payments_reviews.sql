CREATE TABLE payments (
    id               UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id         UUID           NOT NULL UNIQUE REFERENCES orders(id),
    payment_method   VARCHAR(50)    NOT NULL,
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    amount           NUMERIC(10, 2) NOT NULL,
    transaction_id   VARCHAR(255)   UNIQUE,
    gateway_response TEXT,
    paid_at          TIMESTAMP,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE reviews (
    id         UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID      NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    user_id    UUID      NOT NULL REFERENCES users(id),
    rating     INT       NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title      VARCHAR(255),
    body       TEXT,
    is_verified BOOLEAN  NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (product_id, user_id)
);

CREATE INDEX idx_payments_order      ON payments(order_id);
CREATE INDEX idx_payments_transaction ON payments(transaction_id);
CREATE INDEX idx_reviews_product     ON reviews(product_id);
CREATE INDEX idx_reviews_user        ON reviews(user_id);