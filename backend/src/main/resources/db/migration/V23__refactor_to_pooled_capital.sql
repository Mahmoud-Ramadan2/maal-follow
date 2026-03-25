-- V22: Refactor to Pooled Capital Model
-- Purpose: Single capital pool with transaction history
-- Remove per-partner capital_account, enhance capital_pool with tracking

-- ============== DROP OLD PER-PARTNER CAPITAL ==============
DROP TABLE IF EXISTS capital_transaction CASCADE;
DROP TABLE IF EXISTS capital_account CASCADE;

-- ============== ENHANCE CAPITAL POOL TABLE ==============
-- Update existing capital_pool or create if doesn't exist

ALTER TABLE capital_pool ADD COLUMN (
    available_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Amount available for new contracts',
    locked_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Amount locked in active contracts',
    returned_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Capital returned from payments'
);

-- Add constraints for pool amounts
ALTER TABLE capital_pool
ADD CONSTRAINT chk_pool_amounts CHECK (
    available_amount >= 0
    AND locked_amount >= 0
    AND returned_amount >= 0
    AND (available_amount + locked_amount) <= total_amount
);

-- ============== RECREATE CAPITAL TRANSACTION TABLE ==============
-- Keep transaction history but reference pool directly (no per-partner account)

CREATE TABLE IF NOT EXISTS capital_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    capital_pool_id BIGINT NOT NULL,
    transaction_type ENUM('INVESTMENT', 'WITHDRAWAL', 'ALLOCATION', 'RETURN', 'MANUAL') NOT NULL
        COMMENT 'Type of transaction: INVESTMENT (added to pool), WITHDRAWAL (removed), ALLOCATION (locked), RETURN (freed), MANUAL (admin)',
    amount DECIMAL(15,2) NOT NULL COMMENT 'Transaction amount',

    -- Before/after balance tracking
    available_before DECIMAL(15,2) NOT NULL COMMENT 'Available balance before transaction',
    available_after DECIMAL(15,2) NOT NULL COMMENT 'Available balance after transaction',
    locked_before DECIMAL(15,2) NOT NULL COMMENT 'Locked balance before transaction',
    locked_after DECIMAL(15,2) NOT NULL COMMENT 'Locked balance after transaction',

    -- Reference to related entity
    reference_type ENUM('PAYMENT', 'CONTRACT', 'PARTNER_CONTRIBUTION', 'MANUAL') NULL
        COMMENT 'Type of entity that triggered this transaction',
    reference_id BIGINT NULL COMMENT 'ID of reference_type entity that triggered transaction',
    contract_id BIGINT NULL COMMENT 'Related contract ID if applicable',
    partner_id BIGINT NULL COMMENT 'Related partner ID if applicable',
    payment_id BIGINT NULL COMMENT 'Related payment ID if applicable',


    description VARCHAR(500) ,
    transaction_date DATE NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,

    CONSTRAINT fk_capital_transaction_pool FOREIGN KEY (capital_pool_id) REFERENCES capital_pool(id),
    CONSTRAINT fk_capital_transaction_created_by FOREIGN KEY (created_by) REFERENCES user(id),
    CONSTRAINT fk_capital_transaction_payment FOREIGN KEY (payment_id) REFERENCES payment(id),
    CONSTRAINT fk_capital_transaction_contract FOREIGN KEY (contract_id) REFERENCES installment_contract(id),
    CONSTRAINT fk_capital_transaction_partner FOREIGN KEY (partner_id) REFERENCES partner(id),

    INDEX idx_transaction_type (transaction_type),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_transaction_partner (partner_id),
    INDEX idx_transaction_contract (contract_id),
    INDEX idx_transaction_payment (payment_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
