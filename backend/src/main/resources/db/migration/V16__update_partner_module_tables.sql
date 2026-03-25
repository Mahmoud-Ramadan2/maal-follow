
   -- update Partner Management Module: Create tables for partner customer acquisition tracking and profit calculation configuration

-- 3. Create table for partner customer acquisition tracking
CREATE TABLE partner_customer_acquisition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    partner_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    acquisition_date DATE NOT NULL,
    acquisition_cost DECIMAL(10,2) NULL,
    commission_rate DECIMAL(5,2) NULL,
    status ENUM('PENDING', 'ACTIVE', 'INACTIVE', 'TRANSFERRED', 'TERMINATED') NOT NULL DEFAULT 'PENDING',
    notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NULL,
    
    CONSTRAINT fk_customer_acquisition_partner FOREIGN KEY (partner_id) REFERENCES partner(id),
    CONSTRAINT fk_customer_acquisition_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_customer_acquisition_created_by FOREIGN KEY (created_by) REFERENCES user(id),
    CONSTRAINT fk_customer_acquisition_updated_by FOREIGN KEY (updated_by) REFERENCES user(id),
    
    INDEX idx_customer_acquisition_partner (partner_id),
    INDEX idx_customer_acquisition_customer (customer_id),
    INDEX idx_customer_acquisition_status (status),
    INDEX idx_customer_acquisition_date (acquisition_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Track customers acquired by partners for commission purposes';

-- 2. Create table for partner profit calculation configuration
CREATE TABLE partner_profit_calculation_config (
                                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                   management_fee_percentage DECIMAL(5,2) NOT NULL DEFAULT 5.00,
                                                   zakat_percentage DECIMAL(5,2) NOT NULL DEFAULT 2.50,
                                                   profit_payment_day INT NOT NULL DEFAULT 10,
                                                   new_partner_delay_months INT NOT NULL DEFAULT 2,
                                                   is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                                   notes TEXT,
                                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                   updated_at TIMESTAMP NULL,
                                                   created_by BIGINT NOT NULL,
                                                   updated_by BIGINT NULL,

                                                   CONSTRAINT fk_config_created_by FOREIGN KEY (created_by) REFERENCES user(id),
                                                   CONSTRAINT fk_config_updated_by FOREIGN KEY (updated_by) REFERENCES user(id),

                                                   INDEX idx_config_is_active (is_active),
                                                   INDEX idx_config_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Configuration for partner profit calculation parameters';


-- 3. Drop and recreate partner_commission  table with updated structure
DROP TABLE IF EXISTS partner_commission;
CREATE TABLE partner_commission (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    commission_amount DECIMAL(12,2) NOT NULL,
                                    commission_percentage DECIMAL(5,2) NOT NULL,
                                    commission_type ENUM('CUSTOMER_ACQUISITION', 'SALES_COMMISSION',  'REFERRAL_BONUS', 'PERFORMANCE_BONUS' ) NOT NULL,
                                    status ENUM('PENDING', 'PAID', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
                                    base_amount DECIMAL(12,2),
                                    calculated_at TIMESTAMP NULL,
                                    paid_at TIMESTAMP NULL,
                                    notes TEXT,
                                    partner_id BIGINT NOT NULL,
                                    contract_id BIGINT,
                                    purchase_id BIGINT,
                                    customer_id BIGINT,
                                    approved_by BIGINT,

                                    INDEX idx_partner_commission_partner (partner_id),
                                    INDEX idx_partner_commission_contract (contract_id),
                                    INDEX idx_partner_commission_customer (customer_id),
                                    INDEX idx_partner_commission_status (status),
                                    INDEX idx_partner_commission_calculated_at (calculated_at),

                                    CONSTRAINT fk_partner_commission_partner
                                        FOREIGN KEY (partner_id) REFERENCES partner(id),
                                    CONSTRAINT fk_partner_commission_contract
                                        FOREIGN KEY (contract_id) REFERENCES installment_contract(id),
                                    CONSTRAINT fk_partner_commission_purchase
                                        FOREIGN KEY (purchase_id) REFERENCES product_purchase(id),
                                    CONSTRAINT fk_partner_commission_customer
                                        FOREIGN KEY (customer_id) REFERENCES customer(id),
                                    CONSTRAINT fk_partner_commission_approved_by
                                        FOREIGN KEY (approved_by) REFERENCES user(id)
);


