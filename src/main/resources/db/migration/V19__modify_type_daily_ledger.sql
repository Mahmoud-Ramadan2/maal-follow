 -- v19_ Modify the 'source' column in the 'daily_ledger' table to include extra ENUM value



ALTER TABLE daily_ledger

 MODIFY COLUMN source ENUM('COLLECTION','PURCHASE','INVESTMENT','WITHDRAWAL','PROFIT_DISTRIBUTION','OPERATING_EXPENSE','MANAGEMENT_FEE','ZAKAT','DISCOUNT','MANUAL') NOT NULL;
