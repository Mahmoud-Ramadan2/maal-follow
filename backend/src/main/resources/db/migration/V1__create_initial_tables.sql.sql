-- ===========================
-- 1) CUSTOMER
-- ===========================
CREATE TABLE customer (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(200) NOT NULL,
                          phone VARCHAR(20),
                          address VARCHAR(255),
                          national_id VARCHAR(50),
                          notes TEXT,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP
);

-- ===========================
-- 2) VENDOR (TRADE SOURCE)
-- ===========================
CREATE TABLE vendor (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(200) NOT NULL,
                        phone VARCHAR(20),
                        address VARCHAR(255),
                        notes TEXT,
                        created_at TIMESTAMP NOT null DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP
);

-- ===========================
-- 3) PRODUCT PURCHASE
-- ===========================
CREATE TABLE product_purchase (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  vendor_id BIGINT NOT NULL,
                                  product_name VARCHAR(255),
                                  buy_price DECIMAL(12,2) NOT NULL,
                                  purchase_date DATE NOT NULL,
                                  notes TEXT,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_purchase_vendor
                                      FOREIGN KEY (vendor_id) REFERENCES vendor(id)
);

-- ===========================
-- 4) INSTALLMENT CONTRACT
-- ===========================
CREATE TABLE installment_contract (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      customer_id BIGINT NOT NULL,
                                      product_purchase_id BIGINT NOT NULL,
                                      final_price DECIMAL(12,2) NOT NULL,
                                      down_payment DECIMAL(12,2) NOT NULL,
                                      remaining_amount DECIMAL(12,2) NOT NULL,
                                      months INT NOT NULL,
                                      monthly_amount DECIMAL(12,2) NOT NULL,
                                      start_date DATE NOT NULL,
                                      status ENUM('ACTIVE','COMPLETED','LATE','CANCELLED') DEFAULT 'ACTIVE',
                                      notes TEXT,
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      CONSTRAINT fk_contract_customer
                                          FOREIGN KEY (customer_id) REFERENCES customer(id),

                                      CONSTRAINT fk_contract_purchase
                                          FOREIGN KEY (product_purchase_id) REFERENCES product_purchase(id)
);

-- ===========================
-- 5) INSTALLMENT SCHEDULE
-- ===========================
CREATE TABLE installment_schedule (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      installment_contract_id BIGINT NOT NULL,
                                      due_date DATE NOT NULL,
                                      amount DECIMAL(12,2) NOT NULL,
                                      status ENUM('PENDING','PAID','LATE') DEFAULT 'PENDING',
                                      paid_amount DECIMAL(12,2),
                                      paid_date DATE,
                                      collector_id BIGINT,
                                      notes TEXT,

                                      CONSTRAINT fk_schedule_contract
                                          FOREIGN KEY (installment_contract_id) REFERENCES installment_contract(id)
);

-- ===========================
-- 6) PAYMENT
-- ===========================
CREATE TABLE payment (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         installment_schedule_id BIGINT NOT NULL,
                         amount DECIMAL(12,2) NOT NULL,
                         payment_method ENUM('CASH','VODAFONE_CASH','BANK_TRANSFER','OTHER') NOT NULL,
                         payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         collector_id BIGINT,
                         receipt_document_id BIGINT,
                         notes TEXT,
                         created_at TIMESTAMP NOT null DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_payment_schedule
                             FOREIGN KEY (installment_schedule_id) REFERENCES installment_schedule(id)
);

-- ===========================
-- 7) DAILY LEDGER
-- ===========================
CREATE TABLE daily_ledger (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              type ENUM('INCOME','EXPENSE') NOT NULL,
                              amount DECIMAL(12,2) NOT NULL,
                              source ENUM('COLLECTION','PURCHASE','OTHER') NOT NULL,
                              reference_type ENUM('PAYMENT','PURCHASE','MANUAL'),
                              reference_id BIGINT,
                              description TEXT,
                              date DATE NOT NULL,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

);

-- ===========================
-- 8) FILE DOCUMENTS
-- ===========================
CREATE TABLE file_document (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               entity_type ENUM('CUSTOMER','VENDOR','PURCHASE','CONTRACT','PAYMENT'),
                               entity_id BIGINT,
                               file_url VARCHAR(500) NOT NULL,
                               file_type ENUM('IMAGE','PDF','DOC','OTHER') NOT NULL,
                               description TEXT,
                               file mediumblob,
                               uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ===========================
-- 9) USER (ADMIN / COLLECTOR)
-- ===========================
CREATE TABLE user (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      name VARCHAR(200) NOT NULL,
                      email VARCHAR(200) UNIQUE,
                      password VARCHAR(255) NOT NULL,
                      role ENUM('ADMIN','COLLECTOR') NOT NULL,
                      phone VARCHAR(20),
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
