CREATE TABLE shipping_methods (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    name           VARCHAR(80)  NOT NULL,
    description    VARCHAR(200) NULL,
    price_cents    INT          NOT NULL,
    estimated_days INT          NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP(6) NOT NULL,
    updated_at     TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id)
);
