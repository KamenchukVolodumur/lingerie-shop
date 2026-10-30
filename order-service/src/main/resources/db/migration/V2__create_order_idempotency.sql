CREATE TABLE IF NOT EXISTS order_idempotency (
    id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    order_id BIGINT,
    payment_url VARCHAR(2000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_order_idempotency_user_key UNIQUE (user_id, idempotency_key)
);
