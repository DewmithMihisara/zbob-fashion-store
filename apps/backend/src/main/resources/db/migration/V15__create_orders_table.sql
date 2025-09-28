CREATE TABLE orders (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    order_number    VARCHAR(30)  NOT NULL,
    user_id         BIGINT       NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    subtotal_cents  INT          NOT NULL,
    discount_cents  INT          NOT NULL DEFAULT 0,
    shipping_cents  INT          NOT NULL DEFAULT 0,
    total_cents     INT          NOT NULL,
    promotion_code  VARCHAR(40)  NULL,
    shipping_method VARCHAR(80)  NOT NULL,
    recipient       VARCHAR(120) NOT NULL,
    line1           VARCHAR(200) NOT NULL,
    line2           VARCHAR(200) NULL,
    city            VARCHAR(100) NOT NULL,
    region          VARCHAR(100) NULL,
    postal_code     VARCHAR(20)  NOT NULL,
    country         VARCHAR(2)   NOT NULL,
    phone           VARCHAR(30)  NULL,
    created_at      TIMESTAMP(6) NOT NULL,
    updated_at      TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_orders_number UNIQUE (order_number),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_orders_status ON orders (status, created_at);

CREATE TABLE order_items (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    order_id         BIGINT       NOT NULL,
    variant_id       BIGINT       NULL,
    product_name     VARCHAR(160) NOT NULL,
    sku              VARCHAR(64)  NOT NULL,
    size             VARCHAR(20)  NOT NULL,
    color            VARCHAR(40)  NOT NULL,
    unit_price_cents INT          NOT NULL,
    quantity         INT          NOT NULL,
    created_at       TIMESTAMP(6) NOT NULL,
    updated_at       TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants (id) ON DELETE SET NULL
);
