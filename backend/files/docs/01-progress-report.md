# Maal Flow - Progress Report

## 1. Requirements Status (Installments Module - 19 Requirements)

| # | Requirement (AR) | English | Status | Where |
|---|-----------------|---------|--------|-------|
| 1 | حساب وتقسيم نسبة مكسبة السلعة على دفع الشهر | Profit distribution across monthly payments | DONE | ProfitProcessingService, PaymentProcessingService |
| 2 | عدم تسجيل حساب زبون جديد إلا بعد استكمال البيانات | Customer validation before creation | DONE | CustomerService.create() |
| 3 | عمل خانة للملاحظات لكل زبون و لكل قسط شهري | Notes field for customer and installment | DONE | Customer.notes, InstallmentSchedule.notes, Payment.notes |
| 4 | خانة لميعاد الدفع المتفق عليه وليوم الدفع الفعلي | Agreed vs actual payment date fields | DONE | Contract.agreedPaymentDay, Payment.agreedPaymentMonth, Payment.actualPaymentDate |
| 5 | يتم عمل قائمة اكسل بحسابات الزباين | Excel export of customer accounts | NOT DONE | ExportTemplate entity exists, no service/controller/POI dependency |
| 6 | قائمة بمن لم يدفع بالترتيب + قائمة مرتبة حسب العنوان | Unpaid list by date + by address | PARTIAL | Controllers exist, CollectionRoute entity exists, no service for routes |
| 7 | آلية لعمل الخصم عند الدفع مبكرا أو في آخر قسط | Early payment / final installment discount | DONE | PaymentDiscountService with configurable rates |
| 8 | إضافة خانة لإضافة و سحب فلوس من شخص معين يشارك معي بالمال | Partner add/withdraw money | DONE | PartnerInvestmentService, PartnerWithdrawalService |
| 9 | وضع خانة لحساب نسبة الإدارة و الزكاة شهريا | Mgmt fee, zakat, monthly profit shares | PARTIAL | Management fee works, zakat is commented out (TODO) |
| 10 | يحسب مكسب المشترك الجديد بعد شهرين من دفعه | New partner profit after 2 months | DONE | PartnerProfitCalculationService.isEligibleForProfit() |
| 11 | عمل آلية لمسحوبات العميل هل هي من الأصل أم من المكسب | Withdrawal from principal vs profit tracking | DONE | WithdrawalType enum, calculateEffectiveInvestment() |
| 12 | حساب مجموع الأقساط المفترض ورودها شهريا والواردة فعليا | Expected vs actual monthly totals | DONE | getTotalMonthlyExpected(), PaymentStatisticsService |
| 13 | حذف الحساب بعد انتهاءه حتى لا يحسب في مجموع الأقساط الشهريه | Remove completed contracts from totals | PARTIAL | markAsCompleted() exists, TODO: validate all installments paid first |
| 14 | يكون اعتبار الدفع حسب شهر الدفع المتفق عليه مع الزبون | Profit by agreed month, not actual date | DONE | Payment.agreedPaymentMonth used for profit distribution |
| 15 | حساب سعر السلعة الأصلي + فرق الكاش + مصاريفها | Original price + markup + expenses calculation | DONE | Contract.originalPrice, additionalCosts, ContractExpense, netProfit |
| 16 | يتم عمل خانة لتنزيل الأقساط مباشر | Quick payment entry showing paid/remaining | DONE | PaymentController.processPayment() |
| 17 | عمل تذكير بالمطالبة قبلها بخمس أيام إلى أن يدفع | 5-day payment reminder until paid | DONE (stub) | PaymentReminderService - but sendReminder() is a TODO stub |
| 18 | كيفية ربط الحسابات ببعضها + الحفظ | Account linking + cloud backup | PARTIAL | CustomerAccountLink done, cloud backup NOT DONE |
| 19 | عمل تذكير بمواعيد الدفع للمطالبة في وقتها | Payment date reminders for timely collection | DONE (stub) | Same as 17, sending is stub |

## 2. Module-Level Status

### Installments Sub-modules

| Sub-Module | Ctrl | DTO | Entity | Mapper | Repo | Service | Status |
|-----------|------|-----|--------|--------|------|---------|--------|
| customer | Y | Y | Y | Y | Y | Y | Working |
| vendor | Y | Y | Y | Y | Y | Y | Working |
| purchase | Y | Y | Y | Y | Y | Y | Working |
| contract | Y | Y | Y | Y | Y | Y | Working, TODOs |
| payment | Y | Y | Y | Y | Y | Y | Working, TODOs |
| partner | Y | Y | Y | Y | Y | Y | Working, TODOs |
| profit | Y | Y | Y | - | Y | Y | Partial (zakat) |
| ledger | Y | Y | Y | Y | Y | Y | Working |
| capital | Y | Y | Y | Y | Y | Y | Working |
| schedule | - | - | Y | - | Y | - | Entities only |
| document | Y | Y | Y | Y | Y | Y | Working |

### Top-Level Modules

| Module | Status |
|--------|--------|
| Installments (modules/installments/) | ~65% complete |
| Associations (modules/associations/) | 0% - empty folder |
| Debts (modules/debts/) | 0% - empty folder |
| Shared (modules/shared/) | Partial - entities exist, most services missing |

### Shared Sub-modules

| Sub-Module | Status |
|-----------|--------|
| user | Entity+Repo only, NO service/controller/auth |
| notification | Entity+Enums only, NO repo/service/controller |
| audit | Entity only, NO service |
| settings | Entity only, NO service |

### Security Module
- security/ folder is COMPLETELY EMPTY
- No Spring Security dependency in pom.xml
- ALL services hardcode userId = 1

## 3. TODO Items in Codebase (15+ items)

| File | TODO Description |
|------|-----------------|
| ContractService.java | Auto set createdBy, updatedBy from Security (appears 3 times) |
| ContractService.java | Handle one-shot payment contract case |
| ContractService.java | Check for unpaid installments before changing purchase |
| ContractService.java | Add checks to ensure all installments paid before marking complete |
| PaymentService.java | Get user from security context (hardcoded to 1) |
| PaymentService.java | Record overpayment as credit on customer account or refund |
| PaymentReminderService.java | Implement actual reminder sending (SMS, email, notification) |
| PartnerService.java | SharePercentage should be auto-calculated based on capital |
| PartnerService.java | Auto set user from security |
| PartnerWithdrawalService.java | Set approvedBy user from security |
| PartnerWithdrawalService.java | Set processedBy user from security |
| ProfitProcessingService.java | Zakat calculation needs more requirements (commented out) |
| PaymentProcessingService.java | CHECK capital return condition |
| LedgerService.java | Get user from security context |
| CustomerService.java | Set createdBy from security context |

## 4. Database Migrations (V1-V23)

| Migration | Description |
|-----------|-------------|
| V1 | Initial tables (customer, vendor, purchase, contract, schedule, payment, ledger, document, user) |
| V2-V5 | Add active flags, updated_at, user relations |
| V6 | Partner management schema |
| V7 | Notification config tables |
| V8 | Indexing and payment constraints |
| V9 | Enhanced installments (profit fields, schedule fields) |
| V10-V12 | Triggers, customer created_by, schedule status |
| V13 | Idempotency and status columns |
| V14-V15 | Ledger type modifications, payment discount config |
| V16-V17 | Partner module updates, deduction table |
| V18-V20 | Contract expenses, schedule references |
| V21-V23 | Capital tracking tables, reminder method, pooled capital refactor |

WARNING: V1 migration has double extension: V1__create_initial_tables.sql.sql

## 5. Test Coverage

| Test File | Type |
|-----------|------|
| NagiebApplicationTests.java | Context load only |
| ContractServiceTest.java | Unit test |
| CustomerServiceTest.java | Unit test |
| VendorServiceTest.java | Unit test |

CRITICAL: No integration tests, no controller tests, no payment/partner/ledger tests.

