
--  Migration script to alter the 'status' column in the 'installment_schedule' table
ALTER TABLE installment_schedule
    MODIFY COLUMN status ENUM('PENDING','PAID','LATE','PARTIALLY_PAID', 'CANCELLED') DEFAULT 'PENDING';