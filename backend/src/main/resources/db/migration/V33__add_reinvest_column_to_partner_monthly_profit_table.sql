--  This migration adds a new column 'reinvested_amount' to the 'partner_monthly_profit' table to track the amount reinvested by partners. It also updates the 'status' and 'payment_method' columns to include new values for better tracking of profit distribution.
ALTER TABLE partner_monthly_profit

    MODIFY COLUMN status ENUM('CALCULATED', 'PAID', 'REINVESTED', 'DEFERRED')
    DEFAULT 'CALCULATED',
    MODIFY COLUMN payment_method ENUM('CASH','VODAFONE_CASH','INSTAPAY','BANK_TRANSFER','OTHER') NULL;