
-- V25
--  Add created_at and updated_at columns to installment_schedule table
ALTER TABLE installment_schedule
    ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE installment_schedule
    ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;