CREATE TABLE categories (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(80)  NOT NULL,
    slug        VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    badge       VARCHAR(20)  NULL,
    position    INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP(6) NOT NULL,
    updated_at  TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_categories_slug UNIQUE (slug)
);
