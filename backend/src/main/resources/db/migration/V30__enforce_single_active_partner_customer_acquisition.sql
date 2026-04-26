-- Enforce at most one ACTIVE partner acquisition per customer while keeping history.

ALTER TABLE partner_customer_acquisition
    ADD COLUMN active_customer_id BIGINT
        GENERATED ALWAYS AS (
            CASE
                WHEN status = 'ACTIVE' THEN customer_id
                ELSE NULL
            END
        ) STORED;

CREATE UNIQUE INDEX uk_partner_customer_acquisition_active_customer
    ON partner_customer_acquisition (active_customer_id);

