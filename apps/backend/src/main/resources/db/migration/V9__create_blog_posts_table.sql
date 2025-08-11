CREATE TABLE blog_posts (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    title             VARCHAR(200)  NOT NULL,
    slug              VARCHAR(220)  NOT NULL,
    excerpt           VARCHAR(500)  NOT NULL,
    body              TEXT          NOT NULL,
    topic             VARCHAR(40)   NOT NULL,
    read_minutes      INT           NOT NULL DEFAULT 5,
    cover_storage_key VARCHAR(255)  NULL,
    cover_url         VARCHAR(1000) NULL,
    published_at      TIMESTAMP(6)  NULL,
    created_at        TIMESTAMP(6)  NOT NULL,
    updated_at        TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_blog_posts_slug UNIQUE (slug)
);

CREATE INDEX idx_blog_posts_published ON blog_posts (published_at);
