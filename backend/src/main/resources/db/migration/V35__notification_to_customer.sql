-- Move notifications to customers instead of users
ALTER TABLE notification
    DROP FOREIGN KEY fk_notification_user;

ALTER TABLE notification
    CHANGE user_id customer_id BIGINT NOT NULL;

ALTER TABLE notification
    ADD INDEX idx_notification_customer (customer_id);

ALTER TABLE notification
    ADD CONSTRAINT fk_notification_customer
        FOREIGN KEY (customer_id) REFERENCES customer(id);

