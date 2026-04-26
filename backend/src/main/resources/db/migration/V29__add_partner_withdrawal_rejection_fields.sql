ALTER TABLE partner_withdrawal
    ADD COLUMN rejected_by BIGINT NULL after approved_by,
    ADD COLUMN rejected_at DATETIME NULL,
    ADD COLUMN rejection_reason TEXT NULL;

ALTER TABLE partner_withdrawal
    ADD CONSTRAINT fk_partner_withdrawal_rejected_by
        FOREIGN KEY (rejected_by) REFERENCES user(id);

