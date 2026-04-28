-- Create refresh_token table for secure token management

CREATE TABLE refresh_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    jti VARCHAR(64) NOT NULL,
    family_id VARCHAR(64) NOT NULL,
    parent_jti VARCHAR(64),
    device_id VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    issued_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME,
    replaced_by_jti VARCHAR(64),
    reuse_detected BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_refresh_token_jti UNIQUE (jti),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_family_id ON refresh_token (family_id);
CREATE INDEX idx_refresh_token_user_id ON refresh_token (user_id);
CREATE INDEX idx_refresh_token_expires_at ON refresh_token (expires_at);

