CREATE TABLE product_variants (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    product_id          BIGINT       NOT NULL,
    sku                 VARCHAR(64)  NOT NULL,
    size                VARCHAR(20)  NOT NULL,
    color               VARCHAR(40)  NOT NULL,
    stock_quantity      INT          NOT NULL DEFAULT 0,
    low_stock_threshold INT          NOT NULL DEFAULT 5,
    created_at          TIMESTAMP(6) NOT NULL,
    updated_at          TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_variants_sku UNIQUE (sku),
    CONSTRAINT uq_variants_option UNIQUE (product_id, size, color),
    CONSTRAINT fk_variants_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);
