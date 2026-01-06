CREATE TABLE outbox_event (
    outbox_event_id BIGINT PRIMARY KEY,
    type            VARCHAR(50) NOT NULL,
    aggregate_id    BIGINT NOT NULL,
    payload         TEXT NOT NULL,
    published       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL
);

CREATE INDEX outbox_event_published_created_at_idx ON outbox_event(published, created_at);