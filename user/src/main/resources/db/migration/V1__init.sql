CREATE TABLE users
(
    user_id    BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);

CREATE TABLE user_credit
(
    user_credit_id BIGSERIAL PRIMARY KEY,
    user_id        BIGINT UNIQUE NOT NULL,
    credit         INTEGER       NOT NULL,
    created_at     TIMESTAMP     NOT NULL,
    updated_at     TIMESTAMP     NOT NULL
);

CREATE TABLE credit_history
(
    credit_history_id BIGSERIAL PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    order_id          BIGINT       NOT NULL,
    amount            INTEGER      NOT NULL,
    transaction_type  VARCHAR(20)  NOT NULL,
    balance_after     INTEGER      NOT NULL,
    created_at        TIMESTAMP    NOT NULL,
    CONSTRAINT uk_order_transaction UNIQUE (order_id, transaction_type)
);

-- Test data
INSERT INTO users (name, created_at, updated_at)
VALUES ('테스트 사용자1', NOW(), NOW()),
       ('테스트 사용자2', NOW(), NOW()),
       ('테스트 사용자3', NOW(), NOW());

INSERT INTO user_credit (user_id, credit, created_at, updated_at)
VALUES (1, 0, NOW(), NOW()),
       (2, 0, NOW(), NOW()),
       (3, 0, NOW(), NOW());