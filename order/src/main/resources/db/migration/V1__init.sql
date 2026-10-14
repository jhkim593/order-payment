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

