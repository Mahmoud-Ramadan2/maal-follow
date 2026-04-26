
--  This migration adds two new columns to the 'partner' table: 'national_id' and 'effective_investment'. The 'national_id' column is a VARCHAR(20) that cannot be null and has a default value of 'UNKNOWN'. The 'effective_investment' column is a DECIMAL(15,2) that defaults to 0.00.
ALTER TABLE partner
    ADD COLUMN national_id VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN' AFTER phone;

ALTER TABLE partner
    ADD COLUMN effective_investment DECIMAL(15,2) DEFAULT 0.00 AFTER total_investment;