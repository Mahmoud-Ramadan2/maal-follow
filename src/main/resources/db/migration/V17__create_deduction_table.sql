-- V17: Create deduction table for tracking management fees, zakat, and other deductions from profit
-- This table is linked to InstallmentScheduleService for profit processing

CREATE TABLE deduction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    installment_schedule_id BIGINT NULL,
    deduction_type ENUM('MANAGEMENT_FEE', 'ZAKAT', 'TAX', 'COMMISSION', 'OTHER') NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    deduction_date DATE NOT NULL,
    month VARCHAR(7) NOT NULL,
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_deduction_contract FOREIGN KEY (contract_id)
        REFERENCES installment_contract(id) ON DELETE CASCADE,
    CONSTRAINT fk_deduction_schedule FOREIGN KEY (installment_schedule_id)
        REFERENCES installment_schedule(id) ON DELETE SET NULL,

    INDEX idx_deduction_contract (contract_id),
    INDEX idx_deduction_schedule (installment_schedule_id),
    INDEX idx_deduction_type (deduction_type),
    INDEX idx_deduction_month (month),
    INDEX idx_deduction_date (deduction_date)
);

-- Add comment for documentation
ALTER TABLE deduction COMMENT = 'Tracks deductions from profit including management fees, zakat, taxes, etc.';


-- Update daily_ledger reference_type to include 'DEDUCTION' and 'INSTALLMENT_SCHEDULE'
ALTER TABLE daily_ledger
MODIFY COLUMN reference_type ENUM('PAYMENT','PURCHASE','INVESTMENT','WITHDRAWAL','PROFIT_DISTRIBUTION', 'CONTRACT_EXPENSE', 'DEDUCTION', 'INSTALLMENT_SCHEDULE', 'MANUAL') NOT NULL;


ALTER TABLE contract_expense
    MODIFY COLUMN expense_type ENUM('SHIPPING', 'INSURANCE', 'MAINTENANCE', 'TAX', 'INSTALLMENT', 'OTHER')   NOT NULL;
