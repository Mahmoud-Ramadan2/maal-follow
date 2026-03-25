-- ===========================
-- DEBTS MODULE (الديون)
-- ===========================
-- Copy to: src/main/resources/db/migration/V26__create_debts_module.sql

CREATE TABLE debt (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    person_name VARCHAR(200) NOT NULL,
    phone VARCHAR(20),
    debt_type ENUM('RECEIVABLE','PAYABLE') NOT NULL COMMENT 'RECEIVABLE=owed to me, PAYABLE=I owe',
    original_amount DECIMAL(12,2) NOT NULL,
    remaining_amount DECIMAL(12,2) NOT NULL,
    description TEXT,
    due_date DATE,
    status ENUM('ACTIVE','SETTLED','OVERDUE','CANCELLED') DEFAULT 'ACTIVE',
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_debt_created_by FOREIGN KEY (created_by) REFERENCES user(id)
);

CREATE TABLE debt_payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    debt_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(20) DEFAULT 'CASH',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dpayment_debt FOREIGN KEY (debt_id) REFERENCES debt(id)
);

CREATE INDEX idx_debt_type ON debt(debt_type);
CREATE INDEX idx_debt_status ON debt(status);
CREATE INDEX idx_debt_person ON debt(person_name);
CREATE INDEX idx_debt_due_date ON debt(due_date);
CREATE INDEX idx_dpayment_debt ON debt_payment(debt_id);

