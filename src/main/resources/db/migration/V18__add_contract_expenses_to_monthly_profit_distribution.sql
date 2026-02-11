-- Migration to add contract expenses tracking to monthly profit distribution
-- This allows proper tracking of expenses in profit calculations

ALTER TABLE monthly_profit_distribution
ADD COLUMN contract_expenses_amount DECIMAL(12,2) DEFAULT 0.00 NOT NULL
COMMENT 'Total contract expenses deducted from profit for this month';

