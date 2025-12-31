CREATE TABLE activity_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    actor_id    BIGINT       NULL,
    actor_name  VARCHAR(255) NOT NULL,
    action      VARCHAR(20)  NOT NULL,
    summary     VARCHAR(300) NOT NULL,
    status_code INT          NOT NULL,
    created_at  TIMESTAMP(6) NOT NULL,
    updated_at  TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_activity_log_created ON activity_log (created_at);
