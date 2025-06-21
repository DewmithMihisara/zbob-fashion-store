CREATE TABLE product_images (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    product_id  BIGINT        NOT NULL,
    storage_key VARCHAR(255)  NOT NULL,
    url         VARCHAR(1000) NOT NULL,
    alt_text    VARCHAR(255)  NULL,
    position    INT           NOT NULL DEFAULT 0,
    created_at  TIMESTAMP(6)  NOT NULL,
    updated_at  TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);
