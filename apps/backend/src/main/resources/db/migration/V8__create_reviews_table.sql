CREATE TABLE reviews (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    author_name  VARCHAR(120)  NOT NULL,
    author_title VARCHAR(120)  NULL,
    headline     VARCHAR(200)  NOT NULL,
    body         VARCHAR(2000) NOT NULL,
    rating       INT           NOT NULL,
    approved     BOOLEAN       NOT NULL DEFAULT FALSE,
    product_id   BIGINT        NULL,
    user_id      BIGINT        NULL,
    created_at   TIMESTAMP(6)  NOT NULL,
    updated_at   TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE SET NULL,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_reviews_approved ON reviews (approved, created_at);
