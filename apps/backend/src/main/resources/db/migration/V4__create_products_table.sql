CREATE TABLE products (
    id                     BIGINT        NOT NULL AUTO_INCREMENT,
    name                   VARCHAR(160)  NOT NULL,
    slug                   VARCHAR(180)  NOT NULL,
    description            VARCHAR(4000) NOT NULL,
    material               VARCHAR(500)  NULL,
    care                   VARCHAR(500)  NULL,
    warranty               VARCHAR(500)  NULL,
    badge                  VARCHAR(20)   NULL,
    price_cents            INT           NOT NULL,
    compare_at_price_cents INT           NULL,
    published              BOOLEAN       NOT NULL DEFAULT FALSE,
    category_id            BIGINT        NOT NULL,
    brand_id               BIGINT        NULL,
    created_at             TIMESTAMP(6)  NOT NULL,
    updated_at             TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_products_slug UNIQUE (slug),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_products_brand FOREIGN KEY (brand_id) REFERENCES brands (id)
);

CREATE INDEX idx_products_published_badge ON products (published, badge);
