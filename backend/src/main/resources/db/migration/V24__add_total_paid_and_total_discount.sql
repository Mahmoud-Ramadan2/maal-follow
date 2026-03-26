-- V24 :

--  This migration adds two new columns, 'total_paid' and 'total_discount', to the 'installment_contract' table.
--  The 'total_paid' column will store the total amount paid towards the contract, while the 'total_discount' column will store the total discount applied to the contract.
ALTER TABLE installment_contract
    ADD COLUMN total_paid DECIMAL(12,2) DEFAULT 0.00 COMMENT 'Total amount paid towards the contract' AFTER capital_returned;
ALTER TABLE installment_contract
    ADD COLUMN total_discount DECIMAL(12,2) DEFAULT 0.00 COMMENT 'Total discount applied to the contract' AFTER total_paid;