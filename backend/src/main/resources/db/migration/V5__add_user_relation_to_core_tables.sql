--  Add user_id column to purchase table
ALTER TABLE product_purchase
    ADD COLUMN created_by BIGINT NOT NULL;

ALTER TABLE product_purchase
    ADD CONSTRAINT fk_purchase_user
        FOREIGN KEY (created_by)
            REFERENCES user(id);

--  Add user_id column to installment_contract table
ALTER TABLE installment_contract
    ADD COLUMN created_by BIGINT NOT NULL;
ALTER TABLE installment_contract
    ADD COLUMN updated_by BIGINT NOT NULL;
ALTER TABLE installment_contract
    ADD COLUMN responsible_user_id BIGINT NOT NULL;

ALTER TABLE installment_contract
    ADD CONSTRAINT fk_contract_created_by
        FOREIGN KEY (created_by)
            REFERENCES user(id);
ALTER TABLE installment_contract
    ADD CONSTRAINT fk_contract_updated_by
FOREIGN KEY (updated_by) REFERENCES user(id);
ALTER TABLE installment_contract
    ADD CONSTRAINT fk_contract_responsible_user
        FOREIGN KEY (responsible_user_id) REFERENCES user(id);

--  Add user_id column to payment table
ALTER TABLE payment
    ADD COLUMN received_by BIGINT NOT NULL;

ALTER TABLE payment
    ADD CONSTRAINT fk_payment_user
        FOREIGN KEY (received_by)
            REFERENCES user(id);

ALTER TABLE daily_ledger
    ADD COLUMN user_id BIGINT NOT NULL;

ALTER TABLE daily_ledger
    ADD CONSTRAINT fk_ledger_user
        FOREIGN KEY (user_id)
            REFERENCES user(id);

--  Add user_id column to document table
ALTER TABLE file_document
    ADD COLUMN uploaded_by BIGINT NOT NULL;

ALTER TABLE file_document
    ADD CONSTRAINT fk_document_user
        FOREIGN KEY (uploaded_by)
            REFERENCES user(id);