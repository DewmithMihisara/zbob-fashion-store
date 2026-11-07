CREATE TABLE payments (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    order_id     BIGINT       NOT NULL,
    provider     VARCHAR(40)  NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    amount_cents INT          NOT NULL,
    reference    VARCHAR(120) NULL,
    created_at   TIMESTAMP(6) NOT NULL,
    updated_at   TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);
