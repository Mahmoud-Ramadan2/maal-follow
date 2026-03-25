-- V21: Add Capital Tracking Tables
-- Purpose: Track capital investments, allocations, and returns

-- ============== CAPITAL ACCOUNT TABLE ==============
-- Tracks available and locked capital per partner
CREATE TABLE IF NOT EXISTS capital_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    partner_id BIGINT NOT NULL,
    total_invested DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Total amount invested by partner',
    available_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Amount available for new contracts',
    locked_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Amount locked in active contracts',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_capital_account_partner FOREIGN KEY (partner_id) REFERENCES partner(id),
    CONSTRAINT uk_capital_account_partner UNIQUE (partner_id),
    CONSTRAINT chk_capital_amounts CHECK (available_amount >= 0 AND locked_amount >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============== CAPITAL TRANSACTION TABLE ==============

CREATE TABLE IF NOT EXISTS capital_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    capital_account_id BIGINT NOT NULL,
    transaction_type ENUM('INVESTMENT', 'WITHDRAWAL', 'ALLOCATION', 'RETURN', 'MANUAL') NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL COMMENT 'Available balance after this transaction',

    -- Reference to related entity
    reference_type ENUM('PAYMENT', 'CONTRACT', 'MANUAL') NULL,
    reference_id BIGINT NULL,
    contract_id BIGINT NULL COMMENT 'Contract related to this transaction',

    description VARCHAR(500),
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,

    CONSTRAINT fk_capital_transaction_account FOREIGN KEY (capital_account_id) REFERENCES capital_account(id),
    CONSTRAINT fk_capital_transaction_contract FOREIGN KEY (contract_id) REFERENCES installment_contract(id),
    CONSTRAINT fk_capital_transaction_created_by FOREIGN KEY (created_by) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DELIMITER $$

CREATE PROCEDURE AddCapitalColumnsToContract()
BEGIN
    -- Add capital_allocated column if it doesn't exist
    IF NOT EXISTS (
        SELECT * FROM information_schema.columns
        WHERE table_schema = DATABASE()
        AND table_name = 'installment_contract'
        AND column_name = 'capital_allocated'
    ) THEN
        ALTER TABLE installment_contract
        ADD COLUMN capital_allocated DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Capital locked for this contract' AFTER product_purchase_id;
    END IF;
    
    -- Add capital_returned column if it doesn't exist
    IF NOT EXISTS (
        SELECT * FROM information_schema.columns
        WHERE table_schema = DATABASE()
        AND table_name = 'installment_contract'
        AND column_name = 'capital_returned'
    ) THEN
        ALTER TABLE installment_contract
        ADD COLUMN capital_returned DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Capital returned from payments' AFTER capital_allocated;
    END IF;
END$$

DELIMITER ;

CALL AddCapitalColumnsToContract();
DROP PROCEDURE AddCapitalColumnsToContract;

-- ============== UPDATE INSTALLMENT SCHEDULE ==============
-- Add principal/profit paid tracking if columns don't exist

DELIMITER $$

CREATE PROCEDURE AddPaymentTrackingColumnsToSchedule()
BEGIN
    -- Add principal_paid column if it doesn't exist
    IF NOT EXISTS (
        SELECT * FROM information_schema.columns
        WHERE table_schema = DATABASE()
        AND table_name = 'installment_schedule'
        AND column_name = 'principal_paid'
    ) THEN
        ALTER TABLE installment_schedule
        ADD COLUMN principal_paid DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Principal portion paid' AFTER profit_amount;
    END IF;
    
    -- Add profit_paid column if it doesn't exist
    IF NOT EXISTS (
        SELECT * FROM information_schema.columns
        WHERE table_schema = DATABASE()
        AND table_name = 'installment_schedule'
        AND column_name = 'profit_paid'
    ) THEN
        ALTER TABLE installment_schedule
        ADD COLUMN profit_paid DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Profit portion paid' AFTER principal_paid;
    END IF;
END$$

DELIMITER ;

CALL AddPaymentTrackingColumnsToSchedule();
DROP PROCEDURE AddPaymentTrackingColumnsToSchedule;

-- ============== REMOVE agreed_payment_month COLUMN ==============
--
-- DELIMITER $$
--
-- CREATE PROCEDURE RemoveAgreedPaymentMonthColumn()
-- BEGIN
--     -- Check if the column exists before attempting to drop it
--     IF EXISTS (
--         SELECT * FROM information_schema.columns
--         WHERE table_schema = DATABASE()
--         AND table_name = 'payment'
--         AND column_name = 'agreed_payment_month'
--     ) THEN
--         ALTER TABLE payment DROP COLUMN agreed_payment_month;
--     END IF;
-- END$$
--
-- DELIMITER ;
--
-- CALL RemoveAgreedPaymentMonthColumn();
-- DROP PROCEDURE RemoveAgreedPaymentMonthColumn;


