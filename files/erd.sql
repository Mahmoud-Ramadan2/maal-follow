create table application_setting
(
    id            bigint auto_increment
        primary key,
    setting_key   varchar(100)                         not null,
    setting_value text                                 null,
    description   varchar(255)                         null,
    category      varchar(50)                          null,
    is_encrypted  tinyint(1) default 0                 null,
    created_at    timestamp  default CURRENT_TIMESTAMP not null,
    updated_at    timestamp                            null on update CURRENT_TIMESTAMP,
    constraint setting_key
        unique (setting_key)
);

create table capital_pool
(
    id                    bigint auto_increment
        primary key,
    total_amount          decimal(15, 2)                      not null,
    owner_contribution    decimal(15, 2)                      not null,
    partner_contributions decimal(15, 2)                      not null,
    description           text                                null,
    created_at            timestamp default CURRENT_TIMESTAMP not null,
    updated_at            timestamp                           null on update CURRENT_TIMESTAMP
);

create table customer
(
    id          bigint auto_increment
        primary key,
    name        varchar(200)                         not null,
    phone       varchar(20)                          null,
    address     varchar(255)                         null,
    national_id varchar(50)                          null,
    notes       text                                 null,
    created_at  timestamp  default CURRENT_TIMESTAMP not null,
    updated_at  timestamp                            null on update CURRENT_TIMESTAMP,
    active      tinyint(1) default 1                 not null
)
    comment 'Customer information for installment contracts';

create index idx_customer_address
    on customer (address);

create index idx_customer_name
    on customer (name);

create index idx_customer_phone
    on customer (phone);

create table flyway_schema_history
(
    installed_rank int                                 not null
        primary key,
    version        varchar(50)                         null,
    description    varchar(200)                        not null,
    type           varchar(20)                         not null,
    script         varchar(1000)                       not null,
    checksum       int                                 null,
    installed_by   varchar(100)                        not null,
    installed_on   timestamp default CURRENT_TIMESTAMP not null,
    execution_time int                                 not null,
    success        tinyint(1)                          not null
);

create index flyway_schema_history_s_idx
    on flyway_schema_history (success);

create table user
(
    id         bigint auto_increment
        primary key,
    name       varchar(200)                        not null,
    email      varchar(200)                        null,
    password   varchar(255)                        not null,
    role       enum ('ADMIN', 'COLLECTOR')         not null,
    phone      varchar(20)                         null,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    constraint email
        unique (email)
);

create table audit_log
(
    id          bigint auto_increment
        primary key,
    user_id     bigint                              null,
    action      varchar(100)                        not null,
    entity_type varchar(50)                         not null,
    entity_id   bigint                              null,
    old_value   text                                null,
    new_value   text                                null,
    ip_address  varchar(45)                         null,
    user_agent  text                                null,
    created_at  timestamp default CURRENT_TIMESTAMP not null,
    constraint fk_audit_log_user
        foreign key (user_id) references user (id)
);

create table collection_route
(
    id          bigint auto_increment
        primary key,
    name        varchar(200)                                             not null,
    description text                                                     null,
    route_type  enum ('BY_ADDRESS', 'BY_DATE', 'BY_COLLECTOR', 'CUSTOM') not null,
    is_active   tinyint(1) default 1                                     null,
    created_by  bigint                                                   not null,
    created_at  timestamp  default CURRENT_TIMESTAMP                     not null,
    updated_at  timestamp                                                null on update CURRENT_TIMESTAMP,
    constraint fk_route_created_by
        foreign key (created_by) references user (id)
)
    comment 'Collection routes for payment collection optimization';

create index idx_route_type
    on collection_route (route_type);

create table collection_route_item
(
    id                        bigint auto_increment
        primary key,
    collection_route_id       bigint                               not null,
    customer_id               bigint                               not null,
    sequence_order            int        default 1                 not null,
    estimated_collection_time time                                 null,
    notes                     text                                 null,
    is_active                 tinyint(1) default 1                 null,
    created_at                timestamp  default CURRENT_TIMESTAMP not null,
    constraint fk_route_item_customer
        foreign key (customer_id) references customer (id),
    constraint fk_route_item_route
        foreign key (collection_route_id) references collection_route (id)
            on delete cascade
)
    comment 'Customers assigned to collection routes';

create index idx_route_sequence
    on collection_route_item (collection_route_id, sequence_order);

create table customer_account_link
(
    id                       bigint auto_increment
        primary key,
    customer_id              bigint                                                                          not null,
    linked_customer_id       bigint                                                                          not null,
    relationship_type        enum ('SAME_PERSON', 'FAMILY_MEMBER', 'BUSINESS_PARTNER', 'GUARANTOR', 'OTHER') not null,
    relationship_description varchar(255)                                                                    null,
    is_active                tinyint(1) default 1                                                            null,
    notes                    text                                                                            null,
    created_at               timestamp  default CURRENT_TIMESTAMP                                            not null,
    updated_at               timestamp                                                                       null on update CURRENT_TIMESTAMP,
    created_by               bigint                                                                          not null,
    constraint unique_customer_link
        unique (customer_id, linked_customer_id),
    constraint fk_link_created_by
        foreign key (created_by) references user (id),
    constraint fk_link_customer
        foreign key (customer_id) references customer (id),
    constraint fk_link_linked_customer
        foreign key (linked_customer_id) references customer (id)
);

create index idx_relationship_type
    on customer_account_link (relationship_type);

create table export_template
(
    id              bigint auto_increment
        primary key,
    name            varchar(200)                                                                                         not null,
    template_type   enum ('CUSTOMER_PAYMENTS', 'COLLECTION_BY_ADDRESS', 'COLLECTION_BY_DATE', 'PROFIT_REPORT', 'CUSTOM') not null,
    template_config json                                                                                                 not null comment 'Template configuration in JSON format',
    is_default      tinyint(1) default 0                                                                                 null,
    created_by      bigint                                                                                               not null,
    created_at      timestamp  default CURRENT_TIMESTAMP                                                                 not null,
    updated_at      timestamp                                                                                            null on update CURRENT_TIMESTAMP,
    constraint fk_template_created_by
        foreign key (created_by) references user (id)
)
    comment 'Export templates for Excel and other formats';

create index idx_template_type
    on export_template (template_type);

create table file_document
(
    id          bigint auto_increment
        primary key,
    entity_type enum ('CUSTOMER', 'VENDOR', 'PURCHASE', 'CONTRACT', 'PAYMENT') null,
    entity_id   bigint                                                         null,
    file_url    varchar(500)                                                   not null,
    file_type   enum ('IMAGE', 'PDF', 'DOC', 'OTHER')                          not null,
    description text                                                           null,
    file        mediumblob                                                     null,
    uploaded_at timestamp default CURRENT_TIMESTAMP                            not null,
    uploaded_by bigint                                                         not null,
    constraint fk_document_user
        foreign key (uploaded_by) references user (id)
);

create table monthly_profit_distribution
(
    id                        bigint auto_increment
        primary key,
    month_year                varchar(7)                                                                        not null comment 'Format: YYYY-MM',
    total_profit              decimal(12, 2)                                          default 0.00              not null,
    management_fee_percentage decimal(5, 2)                                           default 0.00              null,
    zakat_percentage          decimal(5, 2)                                           default 0.00              null,
    management_fee_amount     decimal(12, 2)                                          default 0.00              null,
    zakat_amount              decimal(12, 2)                                          default 0.00              null,
    distributable_profit      decimal(15, 2)                                          default 0.00              not null,
    owner_profit              decimal(12, 2)                                          default 0.00              null,
    partners_total_profit     decimal(12, 2)                                          default 0.00              null,
    status                    enum ('PENDING', 'CALCULATED', 'DISTRIBUTED', 'LOCKED') default 'PENDING'         null,
    calculation_notes         text                                                                              null,
    created_at                timestamp                                               default CURRENT_TIMESTAMP not null,
    updated_at                timestamp                                                                         null on update CURRENT_TIMESTAMP,
    calculated_by             bigint                                                                            null,
    constraint unique_month_year
        unique (month_year),
    constraint fk_profit_calculated_by
        foreign key (calculated_by) references user (id)
);

create index idx_month_year_status
    on monthly_profit_distribution (month_year, status);

create table notification
(
    id                  bigint auto_increment
        primary key,
    user_id             bigint                                                                not null,
    title               varchar(255)                                                          not null,
    message             text                                                                  not null,
    type                enum ('PAYMENT_REMINDER', 'CONTRACT_EXPIRY', 'SYSTEM_ALERT', 'OTHER') not null,
    priority            enum ('LOW', 'MEDIUM', 'HIGH', 'URGENT') default 'MEDIUM'             null,
    status              enum ('UNREAD', 'READ', 'ARCHIVED')      default 'UNREAD'             null,
    related_entity_type varchar(50)                                                           null,
    related_entity_id   bigint                                                                null,
    created_at          timestamp                                default CURRENT_TIMESTAMP    not null,
    read_at             timestamp                                                             null,
    constraint fk_notification_user
        foreign key (user_id) references user (id)
);

create table partner
(
    id                             bigint auto_increment
        primary key,
    name                           varchar(200)                                           not null,
    phone                          varchar(20)                                            null,
    address                        varchar(255)                                           null,
    partnership_type               enum ('INVESTOR', 'AFFILIATE', 'DISTRIBUTOR', 'OTHER') not null,
    share_percentage               decimal(5, 2)                                          null,
    status                         enum ('ACTIVE', 'INACTIVE') default 'ACTIVE'           null,
    investment_start_date          date                                                   null comment 'When partner started investing',
    profit_calculation_start_month varchar(7)                                             null comment 'Month to start calculating profit (YYYY-MM)',
    total_investment               decimal(15, 2)              default 0.00               null comment 'Total amount invested',
    total_withdrawals              decimal(15, 2)              default 0.00               null comment 'Total amount withdrawn',
    current_balance                decimal(15, 2)              default 0.00               null comment 'Current investment balance',
    profit_sharing_active          tinyint(1)                  default 1                  null comment 'Is partner receiving profit share',
    notes                          text                                                   null,
    created_by                     bigint                                                 not null,
    created_at                     timestamp                   default CURRENT_TIMESTAMP  not null,
    updated_at                     timestamp                                              null on update CURRENT_TIMESTAMP,
    constraint fk_partner_created_by
        foreign key (created_by) references user (id)
)
    comment 'Business partners who invest in the business';

create table capital_transaction
(
    id               bigint auto_increment
        primary key,
    transaction_type enum ('INVESTMENT', 'WITHDRAWAL', 'PROFIT_DISTRIBUTION') not null,
    amount           decimal(12, 2)                                           not null,
    source_type      enum ('OWNER', 'PARTNER')                                not null,
    source_id        bigint                                                   null,
    description      text                                                     null,
    transaction_date timestamp default CURRENT_TIMESTAMP                      not null,
    constraint fk_capital_transaction_partner
        foreign key (source_id) references partner (id)
);

create table daily_ledger
(
    id             bigint auto_increment
        primary key,
    type           enum ('INCOME', 'EXPENSE')               not null,
    amount         decimal(12, 2)                           not null,
    source         enum ('COLLECTION', 'PURCHASE', 'OTHER') not null,
    reference_type enum ('PAYMENT', 'PURCHASE', 'MANUAL')   null,
    reference_id   bigint                                   null,
    description    text                                     null,
    date           date                                     not null,
    created_at     timestamp default CURRENT_TIMESTAMP      not null,
    user_id        bigint                                   not null,
    partner_id     bigint                                   null,
    constraint fk_ledger_partner
        foreign key (partner_id) references partner (id),
    constraint fk_ledger_user
        foreign key (user_id) references user (id)
);

create index idx_ledger_date
    on daily_ledger (date);

create table partner_investment
(
    id              bigint auto_increment
        primary key,
    partner_id      bigint                                                              not null,
    amount          decimal(12, 2)                                                      not null,
    investment_type enum ('INITIAL', 'ADDITIONAL')                                      not null,
    status          enum ('CONFIRMED', 'PENDING', 'RETURNED') default 'PENDING'         null,
    invested_at     timestamp                                 default CURRENT_TIMESTAMP not null,
    returned_at     timestamp                                                           null,
    notes           text                                                                null,
    constraint fk_investment_partner
        foreign key (partner_id) references partner (id)
);

create table partner_monthly_profit
(
    id                     bigint auto_increment
        primary key,
    partner_id             bigint                                                            not null,
    profit_distribution_id bigint                                                            not null,
    investment_amount      decimal(12, 2)                                                    not null,
    share_percentage       decimal(5, 2)                                                     not null,
    calculated_profit      decimal(12, 2)                                                    not null,
    status                 enum ('CALCULATED', 'PAID', 'DEFERRED') default 'CALCULATED'      null,
    payment_date           date                                                              null,
    payment_method         enum ('CASH', 'BANK_TRANSFER', 'REINVEST')                        null,
    notes                  text                                                              null,
    created_at             timestamp                               default CURRENT_TIMESTAMP not null,
    paid_by                bigint                                                            null,
    constraint unique_partner_month_profit
        unique (partner_id, profit_distribution_id),
    constraint fk_partner_profit_distribution
        foreign key (profit_distribution_id) references monthly_profit_distribution (id),
    constraint fk_partner_profit_paid_by
        foreign key (paid_by) references user (id),
    constraint fk_partner_profit_partner
        foreign key (partner_id) references partner (id)
);

create table partner_withdrawal
(
    id               bigint auto_increment
        primary key,
    partner_id       bigint                                                                           not null,
    amount           decimal(12, 2)                                                                   not null,
    withdrawal_type  enum ('FROM_PRINCIPAL', 'FROM_PROFIT', 'FROM_BOTH')                              not null,
    principal_amount decimal(12, 2)                                         default 0.00              null,
    profit_amount    decimal(12, 2)                                         default 0.00              null,
    status           enum ('PENDING', 'APPROVED', 'COMPLETED', 'CANCELLED') default 'PENDING'         null,
    request_reason   text                                                                             null,
    requested_at     timestamp                                              default CURRENT_TIMESTAMP not null,
    approved_at      timestamp                                                                        null,
    processed_at     timestamp                                                                        null,
    notes            text                                                                             null,
    processed_by     bigint                                                                           null,
    approved_by      bigint                                                                           null,
    constraint fk_withdrawal_approved_by
        foreign key (approved_by) references user (id),
    constraint fk_withdrawal_partner
        foreign key (partner_id) references partner (id),
    constraint fk_withdrawal_processed_by
        foreign key (processed_by) references user (id)
);

create index idx_withdrawal_status
    on partner_withdrawal (status);

create table vendor
(
    id         bigint auto_increment
        primary key,
    name       varchar(200)                         not null,
    phone      varchar(20)                          null,
    address    varchar(255)                         null,
    notes      text                                 null,
    created_at timestamp  default CURRENT_TIMESTAMP not null,
    updated_at timestamp                            null on update CURRENT_TIMESTAMP,
    active     tinyint(1) default 1                 not null
)
    comment 'Vendor/supplier information for product purchases';

create table product_purchase
(
    id            bigint auto_increment
        primary key,
    vendor_id     bigint                              not null,
    product_name  varchar(255)                        null,
    buy_price     decimal(12, 2)                      not null,
    purchase_date date                                not null,
    notes         text                                null,
    created_at    timestamp default CURRENT_TIMESTAMP not null,
    created_by    bigint                              not null,
    constraint fk_purchase_user
        foreign key (created_by) references user (id),
    constraint fk_purchase_vendor
        foreign key (vendor_id) references vendor (id)
);

create table installment_contract
(
    id                          bigint auto_increment
        primary key,
    customer_id                 bigint                                                                      not null,
    product_purchase_id         bigint                                                                      not null,
    final_price                 decimal(12, 2)                                                              not null,
    original_price              decimal(12, 2)                                    default 0.00              not null comment 'Original product price without markup',
    additional_costs            decimal(12, 2)                                    default 0.00              null comment 'Shipping, insurance, etc.',
    cash_discount_rate          decimal(5, 2)                                     default 0.00              null comment 'Discount percentage for cash payment',
    early_payment_discount_rate decimal(5, 2)                                     default 0.00              null comment 'Discount for early payments',
    profit_amount               decimal(12, 2)                                    default 0.00              not null comment 'Total profit from this contract',
    agreed_payment_day          int                                               default 1                 not null comment 'Day of month for payment (1-31)',
    partner_id                  bigint                                                                      null comment 'Partner who owns this contract',
    contract_number             varchar(50)                                                                 null comment 'Unique contract reference',
    total_expenses              decimal(12, 2)                                    default 0.00              null comment 'Sum of all contract expenses',
    net_profit                  decimal(12, 2)                                    default 0.00              null comment 'Profit after deducting expenses',
    completion_date             date                                                                        null comment 'Date when contract was completed',
    down_payment                decimal(12, 2)                                                              not null,
    remaining_amount            decimal(12, 2)                                                              not null,
    months                      int                                                                         not null,
    monthly_amount              decimal(12, 2)                                                              not null,
    start_date                  date                                                                        not null,
    status                      enum ('ACTIVE', 'COMPLETED', 'LATE', 'CANCELLED') default 'ACTIVE'          null,
    notes                       text                                                                        null,
    created_at                  timestamp                                         default CURRENT_TIMESTAMP not null,
    updated_at                  timestamp                                                                   null on update CURRENT_TIMESTAMP,
    created_by                  bigint                                                                      not null,
    updated_by                  bigint                                                                      not null,
    responsible_user_id         bigint                                                                      not null,
    constraint fk_contract_created_by
        foreign key (created_by) references user (id),
    constraint fk_contract_customer
        foreign key (customer_id) references customer (id),
    constraint fk_contract_partner
        foreign key (partner_id) references partner (id),
    constraint fk_contract_purchase
        foreign key (product_purchase_id) references product_purchase (id),
    constraint fk_contract_responsible_user
        foreign key (responsible_user_id) references user (id),
    constraint fk_contract_updated_by
        foreign key (updated_by) references user (id)
)
    comment 'Main installment contracts with customers';

create table contract_expense
(
    id                      bigint auto_increment
        primary key,
    installment_contract_id bigint                                                          not null,
    expense_type            enum ('SHIPPING', 'INSURANCE', 'MAINTENANCE', 'TAX', 'OTHER')   not null,
    amount                  decimal(12, 2)                                                  not null,
    description             varchar(255)                                                    null,
    expense_date            date                                                            not null,
    paid_by                 enum ('OWNER', 'PARTNER', 'CUSTOMER') default 'OWNER'           null,
    partner_id              bigint                                                          null,
    receipt_number          varchar(100)                                                    null,
    notes                   text                                                            null,
    created_at              timestamp                             default CURRENT_TIMESTAMP not null,
    created_by              bigint                                                          not null,
    constraint fk_expense_contract
        foreign key (installment_contract_id) references installment_contract (id)
            on delete cascade,
    constraint fk_expense_created_by
        foreign key (created_by) references user (id),
    constraint fk_expense_partner
        foreign key (partner_id) references partner (id)
);

create index idx_expense_date
    on contract_expense (expense_date);

create index idx_expense_type
    on contract_expense (expense_type);

create definer = root@localhost trigger calculate_contract_totals_after_delete
    after delete
on contract_expense
    for each row
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
END;

create definer = root@localhost trigger calculate_contract_totals_after_insert
    after insert
    on contract_expense
    for each row
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
END;

create definer = root@localhost trigger calculate_contract_totals_after_update
    after update
                     on contract_expense
                     for each row
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
END;

create index idx_contract_number
    on installment_contract (contract_number);

create index idx_contract_start_date
    on installment_contract (start_date);

create index idx_contract_status
    on installment_contract (status);

create table installment_schedule
(
    id                      bigint auto_increment
        primary key,
    installment_contract_id bigint                                             not null,
    sequence_number         int                              default 1         not null comment 'Payment sequence (1, 2, 3...)',
    profit_month            varchar(7)                                         not null comment 'Month this payment counts for profit (YYYY-MM)',
    discount_applied        decimal(12, 2)                   default 0.00      null comment 'Discount amount applied',
    is_final_payment        tinyint(1)                       default 0         null comment 'Is this the last payment',
    original_amount         decimal(12, 2)                   default 0.00      not null comment 'Amount before any discounts',
    principal_amount        decimal(12, 2)                   default 0.00      null comment 'Principal portion of payment',
    profit_amount           decimal(12, 2)                   default 0.00      null comment 'Profit portion of payment',
    due_date                date                                               not null,
    amount                  decimal(12, 2)                                     not null,
    status                  enum ('PENDING', 'PAID', 'LATE') default 'PENDING' null,
    paid_amount             decimal(12, 2)                                     null,
    paid_date               date                                               null,
    collector_id            bigint                                             null,
    notes                   text                                               null,
    constraint fk_schedule_contract
        foreign key (installment_contract_id) references installment_contract (id)
)
    comment 'Individual payment schedules for each contract';

create index idx_schedule_due_date
    on installment_schedule (due_date);

create index idx_schedule_profit_month
    on installment_schedule (profit_month);

create index idx_schedule_sequence
    on installment_schedule (installment_contract_id, sequence_number);

create index idx_schedule_status
    on installment_schedule (status);

create table partner_commission
(
    id              bigint auto_increment
        primary key,
    partner_id      bigint                                                          not null,
    purchase_id     bigint                                                          null,
    contract_id     bigint                                                          null,
    amount          decimal(12, 2)                                                  not null,
    commission_type enum ('PERCENTAGE', 'FIXED')                                    not null,
    status          enum ('PENDING', 'PAID', 'CANCELLED') default 'PENDING'         null,
    calculated_at   timestamp                             default CURRENT_TIMESTAMP not null,
    paid_at         timestamp                                                       null,
    notes           text                                                            null,
    constraint fk_commission_contract
        foreign key (contract_id) references installment_contract (id),
    constraint fk_commission_partner
        foreign key (partner_id) references partner (id),
    constraint fk_commission_purchase
        foreign key (purchase_id) references product_purchase (id)
);

create table payment
(
    id                      bigint auto_increment
        primary key,
    installment_schedule_id bigint                                                   not null,
    amount                  decimal(12, 2)                                           not null,
    payment_method          enum ('CASH', 'VODAFONE_CASH', 'BANK_TRANSFER', 'OTHER') not null,
    payment_date            timestamp      default CURRENT_TIMESTAMP                 not null,
    actual_payment_date     date                                                     not null comment 'Actual date payment was received',
    agreed_payment_month    varchar(7)                                               not null comment 'Month this payment was scheduled for (YYYY-MM)',
    is_early_payment        tinyint(1)     default 0                                 null comment 'Was this paid before due date',
    discount_amount         decimal(12, 2) default 0.00                              null comment 'Early payment discount applied',
    net_amount              decimal(12, 2) default 0.00                              not null comment 'Amount after discounts',
    collector_id            bigint                                                   null,
    receipt_document_id     bigint                                                   null,
    notes                   text                                                     null,
    created_at              timestamp      default CURRENT_TIMESTAMP                 not null,
    received_by             bigint                                                   not null,
    constraint fk_payment_schedule
        foreign key (installment_schedule_id) references installment_schedule (id),
    constraint fk_payment_user
        foreign key (received_by) references user (id)
)
    comment 'Actual payments received from customers';

create table partner_profit_sharing
(
    id               bigint auto_increment
        primary key,
    partner_id       bigint                                                          not null,
    contract_id      bigint                                                          null,
    payment_id       bigint                                                          null,
    amount           decimal(12, 2)                                                  not null,
    share_percentage decimal(5, 2)                                                   not null,
    status           enum ('PENDING', 'PAID', 'CANCELLED') default 'PENDING'         null,
    calculated_at    timestamp                             default CURRENT_TIMESTAMP not null,
    paid_at          timestamp                                                       null,
    notes            text                                                            null,
    constraint fk_profit_sharing_contract
        foreign key (contract_id) references installment_contract (id),
    constraint fk_profit_sharing_partner
        foreign key (partner_id) references partner (id),
    constraint fk_profit_sharing_payment
        foreign key (payment_id) references payment (id)
);

create index idx_payment_date
    on payment (payment_date);

create table payment_reminder
(
    id                      bigint auto_increment
        primary key,
    installment_schedule_id bigint                                                                    not null,
    reminder_date           date                                                                      not null,
    reminder_type           enum ('5_DAYS_BEFORE', 'DUE_DATE', 'OVERDUE_1_DAY', 'OVERDUE_WEEKLY')     not null,
    status                  enum ('PENDING', 'SENT', 'DISMISSED', 'FAILED') default 'PENDING'         null,
    message                 text                                                                      null,
    reminder_method         enum ('SMS', 'PHONE_CALL', 'WHATSAPP', 'VISIT') default 'SMS'             null,
    created_at              timestamp                                       default CURRENT_TIMESTAMP not null,
    sent_at                 timestamp                                                                 null,
    failed_reason           text                                                                      null,
    created_by              bigint                                                                    null,
    constraint fk_reminder_created_by
        foreign key (created_by) references user (id),
    constraint fk_reminder_schedule
        foreign key (installment_schedule_id) references installment_schedule (id)
            on delete cascade
);

create index idx_reminder_date_status
    on payment_reminder (reminder_date, status);

create index idx_reminder_type
    on payment_reminder (reminder_type);

create index idx_vendor_name
    on vendor (name);

create definer = root@localhost view v_active_contracts_summary as
select `ic`.`id`                                                    AS `id`,
       `ic`.`contract_number`                                       AS `contract_number`,
       `c`.`name`                                                   AS `customer_name`,
       `c`.`phone`                                                  AS `customer_phone`,
       `c`.`address`                                                AS `customer_address`,
       `ic`.`final_price`                                           AS `final_price`,
       `ic`.`down_payment`                                          AS `down_payment`,
       `ic`.`remaining_amount`                                      AS `remaining_amount`,
       `ic`.`monthly_amount`                                        AS `monthly_amount`,
       `ic`.`months`                                                AS `months`,
       `ic`.`start_date`                                            AS `start_date`,
       `ic`.`agreed_payment_day`                                    AS `agreed_payment_day`,
       `ic`.`profit_amount`                                         AS `profit_amount`,
       `ic`.`net_profit`                                            AS `net_profit`,
       `p`.`name`                                                   AS `partner_name`,
       `ic`.`status`                                                AS `status`,
       coalesce(`paid_schedules`.`paid_count`, 0)                   AS `payments_made`,
       coalesce(`paid_schedules`.`paid_amount`, 0)                  AS `total_paid`,
       (`ic`.`months` - coalesce(`paid_schedules`.`paid_count`, 0)) AS `payments_remaining`
from (((`installments`.`installment_contract` `ic` join `installments`.`customer` `c`
        on ((`ic`.`customer_id` = `c`.`id`))) left join `installments`.`partner` `p`
       on ((`ic`.`partner_id` = `p`.`id`))) left join (select `installments`.`installment_schedule`.`installment_contract_id` AS `installment_contract_id`,
                                                              count(0)                                                        AS `paid_count`,
                                                              sum(`installments`.`installment_schedule`.`paid_amount`)        AS `paid_amount`
                                                       from `installments`.`installment_schedule`
                                                       where (`installments`.`installment_schedule`.`status` = 'PAID')
                                                       group by `installments`.`installment_schedule`.`installment_contract_id`) `paid_schedules`
      on ((`ic`.`id` = `paid_schedules`.`installment_contract_id`)))
where (`ic`.`status` = 'ACTIVE');

-- comment on column v_active_contracts_summary.contract_number not supported: Unique contract reference

-- comment on column v_active_contracts_summary.agreed_payment_day not supported: Day of month for payment (1-31)

-- comment on column v_active_contracts_summary.profit_amount not supported: Total profit from this contract

-- comment on column v_active_contracts_summary.net_profit not supported: Profit after deducting expenses

create definer = root@localhost view v_overdue_payments as
select `s`.`id`                                       AS `schedule_id`,
       `s`.`due_date`                                 AS `due_date`,
       `s`.`amount`                                   AS `amount`,
       `c`.`id`                                       AS `customer_id`,
       `c`.`name`                                     AS `customer_name`,
       `c`.`phone`                                    AS `customer_phone`,
       `c`.`address`                                  AS `customer_address`,
       `ic`.`id`                                      AS `contract_id`,
       `ic`.`contract_number`                         AS `contract_number`,
       `ic`.`agreed_payment_day`                      AS `agreed_payment_day`,
       (to_days(curdate()) - to_days(`s`.`due_date`)) AS `days_overdue`,
       `p`.`name`                                     AS `partner_name`
from (((`installments`.`installment_schedule` `s` join `installments`.`installment_contract` `ic`
        on ((`s`.`installment_contract_id` = `ic`.`id`))) join `installments`.`customer` `c`
       on ((`ic`.`customer_id` = `c`.`id`))) left join `installments`.`partner` `p` on ((`ic`.`partner_id` = `p`.`id`)))
where ((`s`.`status` = 'PENDING') and (`s`.`due_date` < curdate()) and (`ic`.`status` = 'ACTIVE'))
order by `s`.`due_date`;

-- comment on column v_overdue_payments.contract_number not supported: Unique contract reference

-- comment on column v_overdue_payments.agreed_payment_day not supported: Day of month for payment (1-31)

create definer = root@localhost view v_upcoming_payments as
select `s`.`id`                                       AS `schedule_id`,
       `s`.`due_date`                                 AS `due_date`,
       `s`.`amount`                                   AS `amount`,
       `c`.`id`                                       AS `customer_id`,
       `c`.`name`                                     AS `customer_name`,
       `c`.`phone`                                    AS `customer_phone`,
       `c`.`address`                                  AS `customer_address`,
       `ic`.`id`                                      AS `contract_id`,
       `ic`.`contract_number`                         AS `contract_number`,
       `ic`.`agreed_payment_day`                      AS `agreed_payment_day`,
       (to_days(`s`.`due_date`) - to_days(curdate())) AS `days_until_due`,
       `p`.`name`                                     AS `partner_name`
from (((`installments`.`installment_schedule` `s` join `installments`.`installment_contract` `ic`
        on ((`s`.`installment_contract_id` = `ic`.`id`))) join `installments`.`customer` `c`
       on ((`ic`.`customer_id` = `c`.`id`))) left join `installments`.`partner` `p` on ((`ic`.`partner_id` = `p`.`id`)))
where ((`s`.`status` = 'PENDING') and (`s`.`due_date` between curdate() and (curdate() + interval 7 day)) and
       (`ic`.`status` = 'ACTIVE'))
order by `s`.`due_date`, `c`.`address`;

-- comment on column v_upcoming_payments.contract_number not supported: Unique contract reference

-- comment on column v_upcoming_payments.agreed_payment_day not supported: Day of month for payment (1-31)

