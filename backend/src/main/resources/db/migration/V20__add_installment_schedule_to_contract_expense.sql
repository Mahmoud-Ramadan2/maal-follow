-- V20__add_installment_schedule_to_contract_expense.sql
ALTER TABLE contract_expense
    ADD COLUMN installment_schedule_id BIGINT NULL,
    ADD CONSTRAINT fk_contract_expense_installment_schedule
        FOREIGN KEY (installment_schedule_id)
            REFERENCES installment_schedule(id);

CREATE INDEX idx_contract_expense_installment_schedule
    ON contract_expense(installment_schedule_id);
