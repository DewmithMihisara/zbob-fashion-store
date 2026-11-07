CREATE TABLE contact_messages (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    name       VARCHAR(120)  NOT NULL,
    email      VARCHAR(255)  NOT NULL,
    subject    VARCHAR(200)  NULL,
    message    VARCHAR(4000) NOT NULL,
    handled    BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6)  NOT NULL,
    updated_at TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE newsletter_subscribers (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    email      VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_newsletter_email UNIQUE (email)
);
