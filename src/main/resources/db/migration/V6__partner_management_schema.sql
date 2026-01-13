-- Create partners table
CREATE TABLE partner (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(255),
    partnership_type ENUM('INVESTOR', 'AFFILIATE', 'DISTRIBUTOR', 'OTHER') NOT NULL,
    share_percentage DECIMAL(5,2),
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    notes TEXT,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_partner_created_by
        FOREIGN KEY (created_by) REFERENCES user(id)
);

-- Create profit sharing table
CREATE TABLE partner_profit_sharing (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    partner_id BIGINT NOT NULL,
    contract_id BIGINT,
    payment_id BIGINT,
    amount DECIMAL(12,2) NOT NULL,
    share_percentage DECIMAL(5,2) NOT NULL,
    status ENUM('PENDING', 'PAID', 'CANCELLED') DEFAULT 'PENDING',
    calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP NULL,
    notes TEXT,

    CONSTRAINT fk_profit_sharing_partner
        FOREIGN KEY (partner_id) REFERENCES partner(id),
    CONSTRAINT fk_profit_sharing_contract
        FOREIGN KEY (contract_id) REFERENCES installment_contract(id),
    CONSTRAINT fk_profit_sharing_payment
        FOREIGN KEY (payment_id) REFERENCES payment(id)
);

-- Create partner commissions table
CREATE TABLE partner_commission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    partner_id BIGINT NOT NULL,
    purchase_id BIGINT,
    contract_id BIGINT,
    amount DECIMAL(12,2) NOT NULL,
    commission_type ENUM('PERCENTAGE', 'FIXED') NOT NULL,
    status ENUM('PENDING', 'PAID', 'CANCELLED') DEFAULT 'PENDING',
    calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP NULL,
    notes TEXT,

    CONSTRAINT fk_commission_partner
        FOREIGN KEY (partner_id) REFERENCES partner(id),
    CONSTRAINT fk_commission_purchase
        FOREIGN KEY (purchase_id) REFERENCES product_purchase(id),
    CONSTRAINT fk_commission_contract
        FOREIGN KEY (contract_id) REFERENCES installment_contract(id)
);

-- Track total capital and its sources
CREATE TABLE capital_pool (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              total_amount DECIMAL(15,2) NOT NULL,
                              owner_contribution DECIMAL(15,2) NOT NULL,
                              partner_contributions DECIMAL(15,2) NOT NULL,
                              description TEXT,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP
);

-- Track individual partner investments
CREATE TABLE partner_investment (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    partner_id BIGINT NOT NULL,
                                    amount DECIMAL(12,2) NOT NULL,
                                    investment_type ENUM('INITIAL', 'ADDITIONAL') NOT NULL,
                                    status ENUM('CONFIRMED', 'PENDING', 'RETURNED') DEFAULT 'PENDING',
                                    invested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    returned_at TIMESTAMP NULL,
                                    notes TEXT,

                                    CONSTRAINT fk_investment_partner
                                        FOREIGN KEY (partner_id) REFERENCES partner(id)
);

-- Track capital movements
CREATE TABLE capital_transaction (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     transaction_type ENUM('INVESTMENT', 'WITHDRAWAL', 'PROFIT_DISTRIBUTION') NOT NULL,
                                     amount DECIMAL(12,2) NOT NULL,
                                     source_type ENUM('OWNER', 'PARTNER') NOT NULL,
                                     source_id BIGINT, -- References partner_id if source_type is PARTNER
                                     description TEXT,
                                     transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT fk_capital_transaction_partner
                                         FOREIGN KEY (source_id) REFERENCES partner(id)
);


-- Add partner reference to daily_ledger
ALTER TABLE daily_ledger
    ADD COLUMN partner_id BIGINT,
    ADD CONSTRAINT fk_ledger_partner
        FOREIGN KEY (partner_id) REFERENCES partner(id);
