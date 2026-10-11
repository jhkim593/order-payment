ALTER TABLE payment DROP COLUMN pg_cancellation_id;
ALTER TABLE payment DROP COLUMN cancelled_at;

CREATE TABLE payment_cancellation
(
    cancellation_id    BIGINT PRIMARY KEY,
    payment_id         BIGINT       NOT NULL REFERENCES payment (payment_id),
    amount             INTEGER      NOT NULL,
    reason             TEXT         NOT NULL,
    status             VARCHAR(20)  NOT NULL,
    pg_cancellation_id VARCHAR(256),
    cancelled_at       TIMESTAMP,
    status_updated_at  TIMESTAMP    NOT NULL,
    check_count        INTEGER      NOT NULL DEFAULT 0,
    checked_at         TIMESTAMP    NOT NULL,
    created_at         TIMESTAMP    NOT NULL,
    updated_at         TIMESTAMP    NOT NULL
);

-- 결제당 진행 중인 취소는 하나 (ADR 0004)
CREATE UNIQUE INDEX uk_payment_cancellation_in_progress
    ON payment_cancellation (payment_id) WHERE status IN ('PENDING', 'UNKNOWN');

CREATE INDEX idx_payment_cancellation_status_checked_at ON payment_cancellation (status, checked_at);