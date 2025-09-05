CREATE TABLE customer_addresses (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    recipient   VARCHAR(120) NOT NULL,
    line1       VARCHAR(200) NOT NULL,
    line2       VARCHAR(200) NULL,
    city        VARCHAR(100) NOT NULL,
    region      VARCHAR(100) NULL,
    postal_code VARCHAR(20)  NOT NULL,
    country     VARCHAR(2)   NOT NULL,
    phone       VARCHAR(30)  NULL,
    created_at  TIMESTAMP(6) NOT NULL,
    updated_at  TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
