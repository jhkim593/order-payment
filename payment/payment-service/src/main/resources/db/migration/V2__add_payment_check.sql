ALTER TABLE payment
    RENAME COLUMN attempt_count TO check_count;

ALTER TABLE payment
    ADD COLUMN checked_at TIMESTAMP NOT NULL;

CREATE INDEX idx_payment_status_checked_at ON payment (status, checked_at);
