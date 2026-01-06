CREATE TABLE product
(
    product_id           BIGSERIAL PRIMARY KEY,
    product_type VARCHAR(50)  NOT NULL,
    name         VARCHAR(255) NOT NULL,
    description  TEXT         NOT NULL,
    price        INTEGER      NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL
);

CREATE TABLE credit_product
(
    product_id    BIGINT PRIMARY KEY REFERENCES product (product_id),
    credit_amount INTEGER NOT NULL
);

CREATE TABLE orders
(
    order_id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    total_amount      INTEGER      NOT NULL,
    status            VARCHAR(20)  NOT NULL,
    status_updated_at TIMESTAMP    NOT NULL,
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP    NOT NULL
);

CREATE TABLE order_product
(
    order_product_id BIGSERIAL PRIMARY KEY,
    order_id         BIGINT REFERENCES orders (order_id) NOT NULL,
    product_id       BIGINT REFERENCES product (product_id) NOT NULL,
    quantity         INTEGER   NOT NULL,
    price            INTEGER   NOT NULL,
    created_at       TIMESTAMP NOT NULL
);

-- Test data
INSERT INTO product (product_type, name, description, price, created_at, updated_at)
VALUES ('CREDIT', '크레딧 100', '100 크레딧 상품', 10000, NOW(), NOW()),
       ('CREDIT', '크레딧 500', '500 크레딧 상품', 45000, NOW(), NOW()),
       ('CREDIT', '크레딧 1000', '1000 크레딧 상품', 80000, NOW(), NOW());

INSERT INTO credit_product (product_id, credit_amount)
VALUES (1, 100),
       (2, 500),
       (3, 1000);
