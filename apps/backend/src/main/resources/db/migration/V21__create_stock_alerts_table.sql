CREATE TABLE stock_alerts (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    variant_id  BIGINT       NOT NULL,
    email       VARCHAR(255) NOT NULL,
    notified_at TIMESTAMP(6) NULL,
    created_at  TIMESTAMP(6) NOT NULL,
    updated_at  TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_stock_alerts_variant FOREIGN KEY (variant_id) REFERENCES product_variants (id) ON DELETE CASCADE
);

CREATE INDEX idx_stock_alerts_pending ON stock_alerts (variant_id, notified_at);
