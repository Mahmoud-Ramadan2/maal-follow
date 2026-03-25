-- Migration to enhance installments module with all required features
-- Version: V9__enhance_installments_module.sql
-- Description: Add missing tables and columns for complete installments functionality

-- 1. Create monthly_profit_distribution Table

CREATE TABLE monthly_profit_distribution (
                                             id BIGINT NOT NULL AUTO_INCREMENT,
                                             month_year VARCHAR(7) NOT NULL COMMENT 'Format: YYYY-MM',
                                             total_profit DECIMAL(12,2) NOT NULL DEFAULT 0,
                                             management_fee_percentage DECIMAL(5,2) DEFAULT 0,
                                             zakat_percentage DECIMAL(5,2) DEFAULT 0,
                                             management_fee_amount DECIMAL(12,2) DEFAULT 0,
                                             zakat_amount DECIMAL(12,2) DEFAULT 0,
                                             distributable_profit DECIMAL(15,2) NOT NULL DEFAULT 0,
                                             owner_profit DECIMAL(12,2) DEFAULT 0,
                                             partners_total_profit DECIMAL(12,2) DEFAULT 0,
                                             status ENUM('PENDING', 'CALCULATED', 'DISTRIBUTED', 'LOCKED') DEFAULT 'PENDING',
                                             calculation_notes TEXT,
                                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
                                             calculated_by BIGINT NULL,
                                             PRIMARY KEY (id),
                                             UNIQUE KEY unique_month_year (month_year),
                                             KEY idx_month_year_status (month_year, status),
                                             CONSTRAINT fk_profit_calculated_by FOREIGN KEY (calculated_by) REFERENCES user (id)
);

-- 2. Create Partner Withdrawals Table
CREATE TABLE partner_withdrawal (
                                    id BIGINT NOT NULL AUTO_INCREMENT,
                                    partner_id BIGINT NOT NULL,
                                    amount DECIMAL(12,2) NOT NULL,
                                    withdrawal_type ENUM('FROM_PRINCIPAL', 'FROM_PROFIT', 'FROM_BOTH') NOT NULL,
                                    principal_amount DECIMAL(12,2) DEFAULT 0,
                                    profit_amount DECIMAL(12,2) DEFAULT 0,
                                    status ENUM('PENDING', 'APPROVED', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
                                    request_reason TEXT,
                                    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    approved_at TIMESTAMP NULL,
                                    processed_at TIMESTAMP NULL,
                                    notes TEXT,
                                    processed_by BIGINT NULL,
                                    approved_by BIGINT NULL,
                                    PRIMARY KEY (id),
                                    KEY fk_withdrawal_partner (partner_id),
                                    KEY fk_withdrawal_processed_by (processed_by),
                                    KEY fk_withdrawal_approved_by (approved_by),
                                    KEY idx_withdrawal_status (status),
                                    CONSTRAINT fk_withdrawal_partner FOREIGN KEY (partner_id) REFERENCES partner (id),
                                    CONSTRAINT fk_withdrawal_processed_by FOREIGN KEY (processed_by) REFERENCES user (id),
                                    CONSTRAINT fk_withdrawal_approved_by FOREIGN KEY (approved_by) REFERENCES user (id)
);

-- 3. Create Payment Reminders Table
CREATE TABLE payment_reminder (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  installment_schedule_id BIGINT NOT NULL,
                                  reminder_date DATE NOT NULL,
                                  reminder_type ENUM('5_DAYS_BEFORE', 'DUE_DATE', 'OVERDUE_1_DAY', 'OVERDUE_WEEKLY') NOT NULL,
                                  status ENUM('PENDING', 'SENT', 'DISMISSED', 'FAILED') DEFAULT 'PENDING',
                                  message TEXT,
                                  reminder_method ENUM('SMS', 'PHONE_CALL', 'WHATSAPP', 'VISIT') DEFAULT 'SMS',
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  sent_at TIMESTAMP NULL,
                                  failed_reason TEXT,
                                  created_by BIGINT NULL,
                                  PRIMARY KEY (id),
                                  KEY fk_reminder_schedule (installment_schedule_id),
                                  KEY fk_reminder_created_by (created_by),
                                  KEY idx_reminder_date_status (reminder_date, status),
                                  KEY idx_reminder_type (reminder_type),
                                  CONSTRAINT fk_reminder_schedule FOREIGN KEY (installment_schedule_id) REFERENCES installment_schedule (id) ON DELETE CASCADE,
                                  CONSTRAINT fk_reminder_created_by FOREIGN KEY (created_by) REFERENCES user (id)
);

-- 4. Create Customer Account Linkage Table
CREATE TABLE customer_account_link (
                                       id BIGINT NOT NULL AUTO_INCREMENT,
                                       customer_id BIGINT NOT NULL,
                                       linked_customer_id BIGINT NOT NULL,
                                       relationship_type ENUM('SAME_PERSON', 'FAMILY_MEMBER', 'BUSINESS_PARTNER', 'GUARANTOR', 'OTHER') NOT NULL,
                                       relationship_description VARCHAR(255),
                                       is_active BOOLEAN DEFAULT TRUE,
                                       notes TEXT,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
                                       created_by BIGINT NOT NULL,
                                       PRIMARY KEY (id),
                                       KEY fk_link_customer (customer_id),
                                       KEY fk_link_linked_customer (linked_customer_id),
                                       KEY fk_link_created_by (created_by),
                                       KEY idx_relationship_type (relationship_type),
                                       UNIQUE KEY unique_customer_link (customer_id, linked_customer_id),
                                       CONSTRAINT fk_link_customer FOREIGN KEY (customer_id) REFERENCES customer (id),
                                       CONSTRAINT fk_link_linked_customer FOREIGN KEY (linked_customer_id) REFERENCES customer (id),
                                       CONSTRAINT fk_link_created_by FOREIGN KEY (created_by) REFERENCES user (id)
);

-- 5. Create Contract Expenses Table
CREATE TABLE contract_expense (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  installment_contract_id BIGINT NOT NULL,
                                  expense_type ENUM('SHIPPING', 'INSURANCE', 'MAINTENANCE', 'TAX', 'OTHER') NOT NULL,
                                  amount DECIMAL(12,2) NOT NULL,
                                  description VARCHAR(255),
                                  expense_date DATE NOT NULL,
                                  paid_by ENUM('OWNER', 'PARTNER', 'CUSTOMER') DEFAULT 'OWNER',
                                  partner_id BIGINT NULL,
                                  receipt_number VARCHAR(100),
                                  notes TEXT,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  created_by BIGINT NOT NULL,
                                  PRIMARY KEY (id),
                                  KEY fk_expense_contract (installment_contract_id),
                                  KEY fk_expense_partner (partner_id),
                                  KEY fk_expense_created_by (created_by),
                                  KEY idx_expense_type (expense_type),
                                  KEY idx_expense_date (expense_date),
                                  CONSTRAINT fk_expense_contract FOREIGN KEY (installment_contract_id) REFERENCES installment_contract (id) ON DELETE CASCADE,
                                  CONSTRAINT fk_expense_partner FOREIGN KEY (partner_id) REFERENCES partner (id),
                                  CONSTRAINT fk_expense_created_by FOREIGN KEY (created_by) REFERENCES user (id)
);

-- 6. Create Partner Monthly Profit Table
CREATE TABLE partner_monthly_profit (
                                        id BIGINT NOT NULL AUTO_INCREMENT,
                                        partner_id BIGINT NOT NULL,
                                        profit_distribution_id BIGINT NOT NULL,
                                        investment_amount DECIMAL(12,2) NOT NULL,
                                        share_percentage DECIMAL(5,2) NOT NULL,
                                        calculated_profit DECIMAL(12,2) NOT NULL,
                                        status ENUM('CALCULATED', 'PAID', 'DEFERRED') DEFAULT 'CALCULATED',
                                        payment_date DATE NULL,
                                        payment_method ENUM('CASH', 'BANK_TRANSFER', 'REINVEST') NULL,
                                        notes TEXT,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        paid_by BIGINT NULL,
                                        PRIMARY KEY (id),
                                        KEY fk_partner_profit_partner (partner_id),
                                        KEY fk_partner_profit_distribution (profit_distribution_id),
                                        KEY fk_partner_profit_paid_by (paid_by),
                                        UNIQUE KEY unique_partner_month_profit (partner_id, profit_distribution_id),
                                        CONSTRAINT fk_partner_profit_partner FOREIGN KEY (partner_id) REFERENCES partner (id),
                                        CONSTRAINT fk_partner_profit_distribution FOREIGN KEY (profit_distribution_id) REFERENCES monthly_profit_distribution (id),
                                        CONSTRAINT fk_partner_profit_paid_by FOREIGN KEY (paid_by) REFERENCES user (id)
);

-- 7. Enhance Installment Contract Table
ALTER TABLE installment_contract
    ADD COLUMN original_price DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT 'Original product price without markup' AFTER final_price,
    ADD COLUMN additional_costs DECIMAL(12,2) DEFAULT 0 COMMENT 'Shipping, insurance, etc.' AFTER original_price,
    ADD COLUMN cash_discount_rate DECIMAL(5,2) DEFAULT 0 COMMENT 'Discount percentage for cash payment' AFTER additional_costs,
    ADD COLUMN early_payment_discount_rate DECIMAL(5,2) DEFAULT 0 COMMENT 'Discount for early payments' AFTER cash_discount_rate,
    ADD COLUMN profit_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT 'Total profit from this contract' AFTER early_payment_discount_rate,
    ADD COLUMN agreed_payment_day INT NOT NULL DEFAULT 1 COMMENT 'Day of month for payment (1-31)' AFTER profit_amount,
    ADD COLUMN partner_id BIGINT NULL COMMENT 'Partner who owns this contract' AFTER agreed_payment_day,
    ADD COLUMN contract_number VARCHAR(50) NULL COMMENT 'Unique contract reference' AFTER partner_id,
    ADD COLUMN total_expenses DECIMAL(12,2) DEFAULT 0 COMMENT 'Sum of all contract expenses' AFTER contract_number,
    ADD COLUMN net_profit DECIMAL(12,2) DEFAULT 0 COMMENT 'Profit after deducting expenses' AFTER total_expenses,
    ADD COLUMN completion_date DATE NULL COMMENT 'Date when contract was completed' AFTER net_profit;

-- Add foreign key for partner
ALTER TABLE installment_contract
    ADD CONSTRAINT fk_contract_partner FOREIGN KEY (partner_id) REFERENCES partner (id);

-- Add index for contract number
CREATE INDEX idx_contract_number ON installment_contract(contract_number);

-- 8. Enhance Installment Schedule Table
ALTER TABLE installment_schedule
    ADD COLUMN sequence_number INT NOT NULL DEFAULT 1 COMMENT 'Payment sequence (1, 2, 3...)' AFTER installment_contract_id,
    ADD COLUMN profit_month VARCHAR(7) NOT NULL COMMENT 'Month this payment counts for profit (YYYY-MM)' AFTER sequence_number,
    ADD COLUMN discount_applied DECIMAL(12,2) DEFAULT 0 COMMENT 'Discount amount applied' AFTER profit_month,
    ADD COLUMN is_final_payment BOOLEAN DEFAULT FALSE COMMENT 'Is this the last payment' AFTER discount_applied,
    ADD COLUMN original_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT 'Amount before any discounts' AFTER is_final_payment,
    ADD COLUMN principal_amount DECIMAL(12,2) DEFAULT 0 COMMENT 'Principal portion of payment' AFTER original_amount,
    ADD COLUMN profit_amount DECIMAL(12,2) DEFAULT 0 COMMENT 'Profit portion of payment' AFTER principal_amount;

-- Add index for profit month
CREATE INDEX idx_schedule_profit_month ON installment_schedule(profit_month);

-- Add index for sequence number
CREATE INDEX idx_schedule_sequence ON installment_schedule(installment_contract_id, sequence_number);

-- 9. Enhance Payment Table
ALTER TABLE payment
    ADD COLUMN actual_payment_date DATE NOT NULL COMMENT 'Actual date payment was received' AFTER payment_date,
    ADD COLUMN agreed_payment_month VARCHAR(7) NOT NULL COMMENT 'Month this payment was scheduled for (YYYY-MM)' AFTER actual_payment_date,
    ADD COLUMN is_early_payment BOOLEAN DEFAULT FALSE COMMENT 'Was this paid before due date' AFTER agreed_payment_month,
    ADD COLUMN discount_amount DECIMAL(12,2) DEFAULT 0 COMMENT 'Early payment discount applied' AFTER is_early_payment,
    ADD COLUMN net_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT 'Amount after discounts' AFTER discount_amount;

-- Add index for agreed payment month

-- 10. Enhance Partner Table
ALTER TABLE partner
    ADD COLUMN investment_start_date DATE NULL COMMENT 'When partner started investing' AFTER status,
    ADD COLUMN profit_calculation_start_month VARCHAR(7) NULL COMMENT 'Month to start calculating profit (YYYY-MM)' AFTER investment_start_date,
    ADD COLUMN total_investment DECIMAL(15,2) DEFAULT 0 COMMENT 'Total amount invested' AFTER profit_calculation_start_month,
    ADD COLUMN total_withdrawals DECIMAL(15,2) DEFAULT 0 COMMENT 'Total amount withdrawn' AFTER total_investment,
    ADD COLUMN current_balance DECIMAL(15,2) DEFAULT 0 COMMENT 'Current investment balance' AFTER total_withdrawals,
    ADD COLUMN profit_sharing_active BOOLEAN DEFAULT TRUE COMMENT 'Is partner receiving profit share' AFTER current_balance;

-- 11. Create Collection Route Table
CREATE TABLE collection_route (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  name VARCHAR(200) NOT NULL,
                                  description TEXT,
                                  route_type ENUM('BY_ADDRESS', 'BY_DATE', 'BY_COLLECTOR', 'CUSTOM') NOT NULL,
                                  is_active BOOLEAN DEFAULT TRUE,
                                  created_by BIGINT NOT NULL,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
                                  PRIMARY KEY (id),
                                  KEY fk_route_created_by (created_by),
                                  KEY idx_route_type (route_type),
                                  CONSTRAINT fk_route_created_by FOREIGN KEY (created_by) REFERENCES user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Collection routes for payment collection optimization';

-- 12. Create Collection Route Items Table
CREATE TABLE collection_route_item (
                                       id BIGINT NOT NULL AUTO_INCREMENT,
                                       collection_route_id BIGINT NOT NULL,
                                       customer_id BIGINT NOT NULL,
                                       sequence_order INT NOT NULL DEFAULT 1,
                                       estimated_collection_time TIME NULL,
                                       notes TEXT,
                                       is_active BOOLEAN DEFAULT TRUE,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       PRIMARY KEY (id),
                                       KEY fk_route_item_route (collection_route_id),
                                       KEY fk_route_item_customer (customer_id),
                                       KEY idx_route_sequence (collection_route_id, sequence_order),
                                       CONSTRAINT fk_route_item_route FOREIGN KEY (collection_route_id) REFERENCES collection_route (id) ON DELETE CASCADE,
                                       CONSTRAINT fk_route_item_customer FOREIGN KEY (customer_id) REFERENCES customer (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Customers assigned to collection routes';

-- 13. Create Export Templates Table
CREATE TABLE export_template (
                                 id BIGINT NOT NULL AUTO_INCREMENT,
                                 name VARCHAR(200) NOT NULL,
                                 template_type ENUM('CUSTOMER_PAYMENTS', 'COLLECTION_BY_ADDRESS', 'COLLECTION_BY_DATE', 'PROFIT_REPORT', 'CUSTOM') NOT NULL,
                                 template_config JSON NOT NULL COMMENT 'Template configuration in JSON format',
                                 is_default BOOLEAN DEFAULT FALSE,
                                 created_by BIGINT NOT NULL,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
                                 PRIMARY KEY (id),
                                 KEY fk_template_created_by (created_by),
                                 KEY idx_template_type (template_type),
                                 CONSTRAINT fk_template_created_by FOREIGN KEY (created_by) REFERENCES user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Export templates for Excel and other formats';

-- 14. Add indices for better performance
CREATE INDEX idx_customer_phone ON customer(phone);
CREATE INDEX idx_customer_address ON customer(address);
CREATE INDEX idx_vendor_name ON vendor(name);
CREATE INDEX idx_contract_start_date ON installment_contract(start_date);
