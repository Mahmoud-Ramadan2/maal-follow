ALTER TABLE partner_monthly_profit
    ADD COLUMN paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 AFTER calculated_profit,
    ADD COLUMN reinvested_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 AFTER paid_amount,
    MODIFY COLUMN status ENUM('CALCULATED', 'PAID', 'REINVESTED', 'PARTIALLY_SETTLED', 'DEFERRED') DEFAULT 'CALCULATED',
    MODIFY COLUMN payment_method ENUM('CASH','VODAFONE_CASH','INSTAPAY','BANK_TRANSFER','OTHER') NULL;

-- Backfill existing rows.
UPDATE partner_monthly_profit
SET paid_amount = calculated_profit
WHERE status = 'PAID' AND paid_amount = 0.00;

UPDATE partner_monthly_profit
SET reinvested_amount = calculated_profit
WHERE status = 'REINVESTED' AND reinvested_amount = 0.00;
