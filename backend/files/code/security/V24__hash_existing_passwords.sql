-- Flyway Migration: V24__hash_existing_passwords_and_add_findByEmail.sql
-- Copy to: src/main/resources/db/migration/V24__hash_existing_passwords.sql
--
-- IMPORTANT: This migration adds a findByEmail support.
-- Existing passwords must be manually re-hashed using BCrypt
-- after this migration via a one-time script or admin endpoint.
--
-- BCrypt hash of "admin123" = $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

-- Ensure email has an index for login lookups
CREATE INDEX IF NOT EXISTS idx_user_email ON user(email);

-- Update role enum to include MANAGER and USER
ALTER TABLE user MODIFY COLUMN role ENUM('ADMIN', 'COLLECTOR', 'MANAGER', 'USER') NOT NULL;

-- Insert a default admin user with BCrypt-hashed password if not exists
-- Password: admin123 (CHANGE IN PRODUCTION!)
INSERT IGNORE INTO user (name, email, password, role, phone)
VALUES ('Admin', 'admin@maalflow.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'ADMIN', '01000000000');

