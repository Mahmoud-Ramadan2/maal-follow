-- Align partner acquisition schema with JPA entity and add missing investment config table.

-- 1) Create missing partner_investment_config table used by PartnerInvestmentConfig entity.
CREATE TABLE partner_investment_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    minimum_investment_amount DECIMAL(12,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_partner_investment_config_active (is_active),
    INDEX idx_partner_investment_config_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2) Add missing columns expected by PartnerCustomerAcquisition entity.
ALTER TABLE partner_customer_acquisition
    ADD COLUMN commission_percentage DECIMAL(5,2) NULL,
    ADD COLUMN total_commission_earned DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN acquisition_notes TEXT NULL,
    ADD COLUMN acquired_at TIMESTAMP NULL,
    ADD COLUMN deactivated_at TIMESTAMP NULL;

-- 3) Backfill newly added columns from legacy fields where possible.
UPDATE partner_customer_acquisition
SET commission_percentage = commission_rate
WHERE commission_percentage IS NULL AND commission_rate IS NOT NULL;

UPDATE partner_customer_acquisition
SET acquisition_notes = notes
WHERE acquisition_notes IS NULL AND notes IS NOT NULL;

UPDATE partner_customer_acquisition
SET acquired_at = created_at
WHERE acquired_at IS NULL;

-- 4) Enforce acquired_at as required by entity mapping.
ALTER TABLE partner_customer_acquisition
    MODIFY COLUMN acquired_at TIMESTAMP NOT NULL;

