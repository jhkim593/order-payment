CREATE TABLE products
(
    id           BIGSERIAL PRIMARY KEY,
    product_type VARCHAR(50)  NOT NULL,
    name         VARCHAR(255) NOT NULL,
    description  TEXT         NOT NULL,
    price        INTEGER      NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL
);

CREATE TABLE credit_products
(
    id            BIGINT PRIMARY KEY REFERENCES products (id),
    credit_amount INTEGER NOT NULL
);

CREATE TABLE orders
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    total_amount INTEGER      NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL
);

CREATE TABLE order_products
(
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT REFERENCES orders (id)   NOT NULL,
    product_id BIGINT REFERENCES products (id) NOT NULL,
    quantity   INTEGER   NOT NULL,
    price      INTEGER   NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Test data
INSERT INTO products (product_type, name, description, price, created_at, updated_at)
VALUES ('CREDIT', '크레딧 100', '100 크레딧 상품', 10000, NOW(), NOW()),
       ('CREDIT', '크레딧 500', '500 크레딧 상품', 45000, NOW(), NOW()),
       ('CREDIT', '크레딧 1000', '1000 크레딧 상품', 80000, NOW(), NOW());

INSERT INTO credit_products (id, credit_amount, validity_days)
VALUES (1, 100, 30),
       (2, 500, 30),
       (3, 1000, 60);
