CREATE TABLE stock_history
(
    stock_history_id BIGSERIAL PRIMARY KEY,
    product_id       BIGINT      NOT NULL REFERENCES product (product_id),
    quantity         INTEGER     NOT NULL,
    reason           VARCHAR(20) NOT NULL,
    order_id         BIGINT REFERENCES orders (order_id),
    cancellation_id  BIGINT REFERENCES order_cancellation (cancellation_id),
    created_at       TIMESTAMP   NOT NULL
);

-- 기존 재고를 입고 이력으로 넣어 이력 합계와 현재 재고를 맞춘다 (ADR 0007)
INSERT INTO stock_history (product_id, quantity, reason, created_at)
SELECT product_id, stock, 'RESTOCKED', NOW()
FROM product
WHERE stock > 0;
