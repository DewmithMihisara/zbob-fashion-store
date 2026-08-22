CREATE TABLE return_requests (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    order_id    BIGINT        NOT NULL,
    user_id     BIGINT        NOT NULL,
    reason      VARCHAR(1000) NOT NULL,
    status      VARCHAR(20)   NOT NULL,
    staff_note  VARCHAR(1000) NULL,
    decided_at  TIMESTAMP(6)  NULL,
    created_at  TIMESTAMP(6)  NOT NULL,
    updated_at  TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_returns_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_returns_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_returns_status ON return_requests (status, created_at);
