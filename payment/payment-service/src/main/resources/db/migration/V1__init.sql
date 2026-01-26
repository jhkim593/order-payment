CREATE TABLE payment_method
(
    payment_method_id BIGSERIAL PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    pg_provider       VARCHAR(50)  NOT NULL,
    payment_type      VARCHAR(20)  NOT NULL,
    billing_key       VARCHAR(255) NOT NULL,
    payment_detail    TEXT,
    is_default        BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP    NOT NULL
);

CREATE TABLE payment
(
    payment_id         BIGSERIAL PRIMARY KEY,
    user_id            BIGINT NOT NULL,
    order_id           BIGINT NOT NULL UNIQUE,
    currency           VARCHAR(20),
    payment_method_id  BIGINT REFERENCES payment_method (payment_method_id),
    order_name         VARCHAR(256),
    amount             INTEGER      NOT NULL,
    status             VARCHAR(20)  NOT NULL,
    pg_transaction_id  VARCHAR(256),
    pg_cancellation_id VARCHAR(256),
    paid_at            TIMESTAMP,
    cancelled_at       TIMESTAMP,
    status_updated_at  TIMESTAMP    NOT NULL,
    created_at         TIMESTAMP    NOT NULL,
    updated_at         TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX idx_payment_order_status
    ON payment (order_id)
    WHERE status IN ('PAYMENT_PENDING', 'PAYMENT_SUCCESS');



CREATE TABLE payment_history
(
    payment_history_id BIGSERIAL PRIMARY KEY,
    payment_id         BIGINT REFERENCES payment (payment_id) NOT NULL,
    type               VARCHAR(20)  NOT NULL,
    status             VARCHAR(20)  NOT NULL,
    pg_row_data        TEXT,
    pg_transaction_id  VARCHAR(255) NOT NULL,
--     payment_id        BIGINT REFERENCES payment (payment_id),
--     credit_plan_id    BIGINT REFERENCES credit_plans (id),
    created_at         TIMESTAMP    NOT NULL
);



-- Test data
INSERT INTO payment_method (user_id, pg_provider, payment_type, billing_key, is_default, is_active, created_at, updated_at)
VALUES (1, 'PAYPAL', 'CARD', 'billing-key-019a6c29-6944-fd02-80fb-fe67791fdd2a', true, true, NOW(), NOW());

INSERT INTO payment_method (user_id, pg_provider, payment_type, billing_key, is_default, is_active, created_at, updated_at)
VALUES (1, 'PAYPAL', 'CARD', 'billing-key-019bb65b-49b0-ed2b-a913-57cf22335baa', false, true, NOW(), NOW());

INSERT INTO payment_method (user_id, pg_provider, payment_type, billing_key, is_default, is_active, created_at, updated_at)
VALUES (1, 'PAYPAL', 'CARD', 'billing-key-019bb9ad-1db2-0cbe-2f1d-caa2070dbf76', false, true, NOW(), NOW());