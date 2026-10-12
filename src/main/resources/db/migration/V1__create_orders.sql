CREATE TABLE orders (
    id           BIGSERIAL PRIMARY KEY,
    user_id      VARCHAR(64)    NOT NULL,
    username     VARCHAR(100)   NOT NULL,
    product_id   BIGINT         NOT NULL,
    product_name VARCHAR(200)   NOT NULL,
    quantity     INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price   NUMERIC(12, 2) NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    status       VARCHAR(20)    NOT NULL,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_user_id ON orders (user_id, created_at DESC);
