CREATE TABLE payment_methods
(
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT       NOT NULL,
    pg_provider      VARCHAR(50)  NOT NULL,
    payment_type     VARCHAR(20)  NOT NULL,
    billing_key      VARCHAR(255) NOT NULL,
    payment_detail   TEXT,
    is_default       BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL
);

CREATE TABLE payments
(
    id                BIGINT PRIMARY KEY,
    user_id           BIGINT NOT NULL,
    order_id          BIGINT NOT NULL,
    order_name        VARCHAR(256),
    payment_method_id BIGINT REFERENCES payment_methods (id),
    amount            INTEGER      NOT NULL,
    status            VARCHAR(20)  NOT NULL,
    pg_transaction_id VARCHAR(256),
    retry_count       INTEGER NOT NULL,
    paid_at           TIMESTAMP,
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX idx_payments_order_status
    ON payments (order_id)
    WHERE status IN ('PAYMENT_PENDING', 'PAYMENT_SUCCESS');



CREATE TABLE payment_histories
(
    id                BIGSERIAL PRIMARY KEY,
    payment_id        BIGINT REFERENCES payments (id) NOT NULL ,
    type              VARCHAR(20)  NOT NULL,
    status            VARCHAR(20)  NOT NULL,
    pg_row_data       TEXT,
    pg_transaction_id VARCHAR(255) NOT NULL,
--     payment_id        BIGINT REFERENCES payments (id),
--     credit_plan_id    BIGINT REFERENCES credit_plans (id),
    created_at        TIMESTAMP    NOT NULL
);



-- Test data
INSERT INTO payment_methods (user_id, pg_provider, payment_type, billing_key, is_default, is_active, created_at, updated_at)
VALUES (1, 'PORTONE', 'CARD', 'billing-key-019a6c29-6944-fd02-80fb-fe67791fdd2a', true, true, NOW(), NOW());
