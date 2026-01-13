
-- Add indexes for better query performance
CREATE INDEX idx_customer_name ON customer(name);
CREATE INDEX idx_contract_status ON installment_contract(status);
CREATE INDEX idx_payment_date ON payment(payment_date);
CREATE INDEX idx_ledger_date ON daily_ledger(date);
CREATE INDEX idx_schedule_due_date ON installment_schedule(due_date);
CREATE INDEX idx_schedule_status ON installment_schedule(status);

-- Add check constraints for data integrity
ALTER TABLE partner ADD CONSTRAINT chk_share_percentage CHECK (share_percentage BETWEEN 0 AND 100);
ALTER TABLE product_purchase ADD CONSTRAINT chk_buy_price_positive CHECK (buy_price > 0);
ALTER TABLE installment_contract ADD CONSTRAINT chk_final_price_positive CHECK (final_price > 0);
ALTER TABLE installment_contract ADD CONSTRAINT chk_down_payment_positive CHECK (down_payment >= 0);
