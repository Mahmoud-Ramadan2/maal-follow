
-- Adds payment_discount_config table and updates payment_reminder table structure

-- 1. Create Payment Discount Configuration Table
CREATE TABLE payment_discount_config (
    id BIGINT NOT NULL AUTO_INCREMENT,
    discount_type ENUM('EARLY_PAYMENT', 'FINAL_INSTALLMENT', 'BULK_PAYMENT', 'LOYALTY_DISCOUNT', 'MANUAL') NOT NULL,
    early_payment_days_threshold INT NOT NULL DEFAULT 5,
    early_payment_discount_percentage DECIMAL(5,2) DEFAULT 0.00,
    final_installment_discount_percentage DECIMAL(5,2) DEFAULT 0.00,
    minimum_discount_amount DECIMAL(10,2) DEFAULT 10.00,
    maximum_discount_amount DECIMAL(10,2) DEFAULT 1000.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    PRIMARY KEY (id),
    KEY idx_discount_type_active (discount_type, is_active),
    KEY fk_discount_config_created_by (created_by),
    CONSTRAINT fk_discount_config_created_by FOREIGN KEY (created_by) REFERENCES user (id)
);

-- 2. Drop and recreate payment_reminder table with updated structure
DROP TABLE IF EXISTS payment_reminder;

CREATE TABLE payment_reminder (
    id BIGINT NOT NULL AUTO_INCREMENT,
    installment_schedule_id BIGINT NOT NULL,
    reminder_date DATE NOT NULL,
    due_date DATE NOT NULL,
    days_before_due INT NOT NULL,
    status ENUM('PENDING', 'SENT', 'ACKNOWLEDGED', 'COMPLETED', 'CANCELLED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    reminderMethod ENUM('SMS','PHONE_CALL','WHATSAPP','VISIT') NOT NULL DEFAULT 'PHONE_CALL',
    reminder_message TEXT,
    sent_at TIMESTAMP NULL,
    acknowledged_at TIMESTAMP NULL,
    is_recurring BOOLEAN NOT NULL DEFAULT TRUE,
    attempt_count INT NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    PRIMARY KEY (id),
    KEY idx_reminder_schedule (installment_schedule_id),
    KEY idx_reminder_date_status (reminder_date, status),
    KEY idx_reminder_due_date (due_date),
    KEY idx_reminder_status (status),
    KEY fk_reminder_created_by (created_by),
    CONSTRAINT fk_reminder_schedule FOREIGN KEY (installment_schedule_id) REFERENCES installment_schedule (id) ON DELETE CASCADE,
    CONSTRAINT fk_reminder_created_by FOREIGN KEY (created_by) REFERENCES user (id)
);

-- 3. Add indexes to payment table for enhanced querying
ALTER TABLE payment
ADD INDEX idx_payment_agreed_month (agreed_payment_month),
ADD INDEX idx_payment_actual_date (actual_payment_date),
ADD INDEX idx_payment_early (is_early_payment),
ADD INDEX idx_payment_discount (discount_amount),
ADD INDEX idx_payment_net_amount (net_amount);
