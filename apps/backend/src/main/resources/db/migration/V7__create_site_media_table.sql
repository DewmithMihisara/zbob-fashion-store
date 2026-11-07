CREATE TABLE site_media (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    slot        VARCHAR(40)   NOT NULL,
    storage_key VARCHAR(255)  NOT NULL,
    url         VARCHAR(1000) NOT NULL,
    created_at  TIMESTAMP(6)  NOT NULL,
    updated_at  TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_site_media_slot UNIQUE (slot)
);
