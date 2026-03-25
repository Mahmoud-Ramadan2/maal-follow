-- V14: Add idempotency and status columns for payment and ledger modules,
-- and modify payment_method enum to include 'REINVEST' option.

-- Add idempotency_key and status columns to payment table
ALTER TABLE payment
    ADD COLUMN idempotency_key VARCHAR(100) UNIQUE,
    ADD COLUMN status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED') DEFAULT 'COMPLETED';


-- Create index for idempotency key lookups
CREATE UNIQUE INDEX idx_payment_idempotency ON payment (idempotency_key);

-- Create index for status lookups
CREATE INDEX idx_payment_status ON payment (status);

-- Add idempotency_key column to daily_ledger table
ALTER TABLE daily_ledger
    ADD COLUMN idempotency_key VARCHAR(100) UNIQUE;

-- Create index for idempotency key lookups on ledger
CREATE UNIQUE INDEX idx_ledger_idempotency ON daily_ledger (idempotency_key);

-- Create index for source lookups
CREATE INDEX idx_ledger_source ON daily_ledger (source);

-- Create index for type lookups
CREATE INDEX idx_ledger_type ON daily_ledger (type);

-- Modify payment_method column in partner_monthly_profit and payment tables to include 'REINVEST' option
ALTER TABLE partner_monthly_profit
    MODIFY COLUMN payment_method ENUM('CASH','VODAFONE_CASH','BANK_TRANSFER','REINVEST','OTHER') NOT NULL;

ALTER TABLE payment
    MODIFY COLUMN payment_method ENUM('CASH','VODAFONE_CASH','BANK_TRANSFER','REINVEST','OTHER') NOT NULL;
