-- Migration: Add created_by column to customer table
-- This adds user tracking to the customer entity

ALTER TABLE customer
    ADD COLUMN created_by BIGINT NULL,
    ADD CONSTRAINT fk_customer_created_by
        FOREIGN KEY (created_by) REFERENCES user (id);

-- Create index for created_by lookup
CREATE INDEX idx_customer_created_by ON customer (created_by);

