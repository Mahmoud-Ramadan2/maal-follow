
-- 15. Insert default export templates
INSERT INTO export_template (name, template_type, template_config, is_default, created_by) VALUES
                                                                                               ('Customer Payment Schedule', 'CUSTOMER_PAYMENTS', '{"columns": ["customer_name", "contract_id", "due_date", "amount", "status"], "sort_by": "due_date", "include_contact": true}', true, 1),
                                                                                               ('Collection by Address', 'COLLECTION_BY_ADDRESS', '{"columns": ["customer_name", "address", "due_date", "amount", "phone"], "sort_by": "address", "group_by": "area"}', true, 1),
                                                                                               ('Collection by Due Date', 'COLLECTION_BY_DATE', '{"columns": ["due_date", "customer_name", "amount", "address", "phone"], "sort_by": "due_date", "group_by": "date"}', true, 1);

-- 16. Insert default application settings
INSERT INTO application_setting(setting_key, setting_value, description, category) VALUES
                                                                                       ('default_management_fee_percentage', '0.0', 'Default management fee percentage', 'PROFIT_CALCULATION'),
                                                                                       ('default_zakat_percentage', '2.5', 'Default Zakat percentage', 'PROFIT_CALCULATION'),
                                                                                       ('reminder_days_before_due', '5', 'Days before due date to send reminder', 'NOTIFICATIONS'),
                                                                                       ('profit_calculation_delay_months', '1', 'Months to wait before calculating partner profit', 'PROFIT_CALCULATION'),
                                                                                       ('early_payment_discount_percentage', '0.0', 'Default early payment discount percentage', 'PAYMENTS'),
                                                                                       ('export_file_retention_days', '30', 'Days to keep exported files', 'SYSTEM');

-- 17. Create triggers for automatic calculations
DELIMITER //

CREATE TRIGGER calculate_contract_totals_after_insert
    AFTER INSERT ON contract_expense
    FOR EACH ROW
BEGIN
    UPDATE installment_contract
    SET
        total_expenses = (
            SELECT COALESCE(SUM(amount), 0)
            FROM contract_expense
            WHERE installment_contract_id = NEW.installment_contract_id
        ),
        net_profit = profit_amount - (
            SELECT COALESCE(SUM(amount), 0)
            FROM contract_expense
            WHERE installment_contract_id = NEW.installment_contract_id
        )
    WHERE id = NEW.installment_contract_id;
END//

CREATE TRIGGER calculate_contract_totals_after_update
    AFTER UPDATE ON contract_expense
    FOR EACH ROW
BEGIN
    UPDATE installment_contract
    SET
        total_expenses = (
            SELECT COALESCE(SUM(amount), 0)
            FROM contract_expense
            WHERE installment_contract_id = NEW.installment_contract_id
        ),
        net_profit = profit_amount - (
            SELECT COALESCE(SUM(amount), 0)
            FROM contract_expense
            WHERE installment_contract_id = NEW.installment_contract_id
        )
    WHERE id = NEW.installment_contract_id;
END//

CREATE TRIGGER calculate_contract_totals_after_delete
    AFTER DELETE ON contract_expense
    FOR EACH ROW
BEGIN
    UPDATE installment_contract
    SET
        total_expenses = (
            SELECT COALESCE(SUM(amount), 0)
            FROM contract_expense
            WHERE installment_contract_id = OLD.installment_contract_id
        ),
        net_profit = profit_amount - (
            SELECT COALESCE(SUM(amount), 0)
            FROM contract_expense
            WHERE installment_contract_id = OLD.installment_contract_id
        )
    WHERE id = OLD.installment_contract_id;
END//

DELIMITER ;

-- 18. Create views for common queries
CREATE VIEW v_active_contracts_summary AS
SELECT
    ic.id,
    ic.contract_number,
    c.name as customer_name,
    c.phone as customer_phone,
    c.address as customer_address,
    ic.final_price,
    ic.down_payment,
    ic.remaining_amount,
    ic.monthly_amount,
    ic.months,
    ic.start_date,
    ic.agreed_payment_day,
    ic.profit_amount,
    ic.net_profit,
    p.name as partner_name,
    ic.status,
    COALESCE(paid_schedules.paid_count, 0) as payments_made,
    COALESCE(paid_schedules.paid_amount, 0) as total_paid,
    (ic.months - COALESCE(paid_schedules.paid_count, 0)) as payments_remaining
FROM installment_contract ic
         JOIN customer c ON ic.customer_id = c.id
         LEFT JOIN partner p ON ic.partner_id = p.id
         LEFT JOIN (
    SELECT
        installment_contract_id,
        COUNT(*) as paid_count,
        SUM(paid_amount) as paid_amount
    FROM installment_schedule
    WHERE status = 'PAID'
    GROUP BY installment_contract_id
) paid_schedules ON ic.id = paid_schedules.installment_contract_id
WHERE ic.status = 'ACTIVE';

CREATE VIEW v_overdue_payments AS
SELECT
    s.id as schedule_id,
    s.due_date,
    s.amount,
    c.id as customer_id,
    c.name as customer_name,
    c.phone as customer_phone,
    c.address as customer_address,
    ic.id as contract_id,
    ic.contract_number,
    ic.agreed_payment_day,
    DATEDIFF(CURRENT_DATE, s.due_date) as days_overdue,
    p.name as partner_name
FROM installment_schedule s
         JOIN installment_contract ic ON s.installment_contract_id = ic.id
         JOIN customer c ON ic.customer_id = c.id
         LEFT JOIN partner p ON ic.partner_id = p.id
WHERE s.status = 'PENDING'
  AND s.due_date < CURRENT_DATE
  AND ic.status = 'ACTIVE'
ORDER BY s.due_date ASC;

CREATE VIEW v_upcoming_payments AS
SELECT
    s.id as schedule_id,
    s.due_date,
    s.amount,
    c.id as customer_id,
    c.name as customer_name,
    c.phone as customer_phone,
    c.address as customer_address,
    ic.id as contract_id,
    ic.contract_number,
    ic.agreed_payment_day,
    DATEDIFF(s.due_date, CURRENT_DATE) as days_until_due,
    p.name as partner_name
FROM installment_schedule s
         JOIN installment_contract ic ON s.installment_contract_id = ic.id
         JOIN customer c ON ic.customer_id = c.id
         LEFT JOIN partner p ON ic.partner_id = p.id
WHERE s.status = 'PENDING'
  AND s.due_date BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY)
  AND ic.status = 'ACTIVE'
ORDER BY s.due_date ASC, c.address ASC;

-- 19. Add comments to existing tables for better documentation
ALTER TABLE customer COMMENT = 'Customer information for installment contracts';
ALTER TABLE vendor COMMENT = 'Vendor/supplier information for product purchases';
ALTER TABLE installment_contract COMMENT = 'Main installment contracts with customers';
ALTER TABLE installment_schedule COMMENT = 'Individual payment schedules for each contract';
ALTER TABLE payment COMMENT = 'Actual payments received from customers';
ALTER TABLE partner COMMENT = 'Business partners who invest in the business';