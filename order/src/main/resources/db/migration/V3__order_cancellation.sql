CREATE TABLE order_cancellation
(
    cancellation_id   BIGINT PRIMARY KEY,
    order_id          BIGINT      NOT NULL REFERENCES orders (order_id),
    amount            INTEGER     NOT NULL,
    reason            TEXT        NOT NULL,
    status            VARCHAR(20) NOT NULL,
    status_updated_at TIMESTAMP   NOT NULL,
    created_at        TIMESTAMP   NOT NULL,
    updated_at        TIMESTAMP   NOT NULL
);

-- 주문당 처리 중인 취소는 하나 (ADR 0001)
CREATE UNIQUE INDEX uk_order_cancellation_pending
    ON order_cancellation (order_id) WHERE status = 'PENDING';

CREATE TABLE order_product_cancellation
(
    order_product_cancellation_id BIGSERIAL PRIMARY KEY,
    cancellation_id               BIGINT  NOT NULL REFERENCES order_cancellation (cancellation_id),
    order_product_id              BIGINT  NOT NULL REFERENCES order_product (order_product_id),
    quantity                      INTEGER NOT NULL CHECK (quantity > 0),
    UNIQUE (cancellation_id, order_product_id)
);

ALTER TABLE order_product ADD CONSTRAINT ck_order_product_quantity CHECK (quantity >= 0);