-- V22__add_payment_reminder_method_column.sql
-- This migration drops reminderMethod (wrong name) column  and adds a new column 'reminder_method' to the 'payment_reminder' table.

ALTER TABLE payment_reminder

    DROP COLUMN reminderMethod,
    ADD COLUMN reminder_method ENUM('SMS', 'PHONE_CALL', 'WHATSAPP', 'VISIT')
         NOT NULL DEFAULT 'PHONE_CALL' AFTER status;