CREATE TABLE promotions (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    code               VARCHAR(40)  NOT NULL,
    description        VARCHAR(200) NULL,
    discount_type      VARCHAR(10)  NOT NULL,
    discount_value     INT          NOT NULL,
    minimum_cents      INT          NOT NULL DEFAULT 0,
    starts_at          TIMESTAMP(6) NULL,
    ends_at            TIMESTAMP(6) NULL,
    active             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP(6) NOT NULL,
    updated_at         TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_promotions_code UNIQUE (code)
);
