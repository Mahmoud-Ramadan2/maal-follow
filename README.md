# 🏦 Nagieb - Comprehensive Installment Management System

<div align="center">

![Java](https://img.shields.io/badge/Java-17-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

A complete enterprise-grade installment management system for Arabic/Middle Eastern business requirements.

[Features](#-features) • [Quick Start](#-quick-start) • [Architecture](#-architecture) • [API Documentation](#-api-documentation) • [Business Requirements](#-business-requirements)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Business Requirements](#-business-requirements)
- [Core Features](#-core-features)
- [System Architecture](#-system-architecture)
- [Technology Stack](#-technology-stack)
- [Project Structure](#-project-structure)
- [Quick Start](#-quick-start)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [Capital Management](#-capital-management)
- [Business Logic](#-business-logic)
- [Internationalization](#-internationalization)
- [Testing](#-testing)
- [Deployment](#-deployment)

## 🎯 Overview

**Nagieb** is a comprehensive installment management system designed specifically for Arabic/Middle Eastern business practices. It handles the complete lifecycle of installment sales, from customer onboarding to final payment collection, with advanced capital tracking and profit distribution.

### Core Business Model
- **Installment Sales (الأقساط)**: Complete installment sales management with customer tracking, payment schedules, and profit calculation
- **Associations/Groups (الجمعيات)**: Group financing and association management 
- **Debt Management (الديون)**: Comprehensive debt tracking for personal and business obligations
- **Cash Management (الكاشات)**: Daily cash flow and transaction management

### Key Differentiators
- ✅ **Arabic-First Design**: Native Arabic support with RTL interface considerations
- ✅ **Islamic Finance Compliant**: Profit-based calculations instead of interest
- ✅ **Pooled Capital Model**: Advanced capital allocation and tracking system
- ✅ **Complete Audit Trail**: Every transaction tracked with before/after states
- ✅ **Flexible Payment Processing**: Handles overpayments, partial payments, and early settlements
- ✅ **Multi-Partner Support**: Capital contribution tracking and profit distribution

---

## 📊 Business Requirements

Based on the original Arabic requirements document, the system implements:

### 1. Installment Sales Module (الأقساط)
- **Customer Management**: Complete customer lifecycle with validation and tracking
- **Product Purchases**: Track products, costs, margins, and vendor relationships
- **Contract Creation**: Generate installment contracts with flexible terms
- **Payment Schedules**: Automated schedule generation with due date tracking
- **Payment Processing**: Handle payments with automatic profit/principal allocation
- **Capital Tracking**: Track capital allocation and returns for investors
- **Reminder System**: Automated payment reminders (5 days before due)
- **Reporting**: Customer lists, payment schedules, profit summaries

### 2. Capital Management (إدارة رؤوس الأموال)
- **Pooled Capital Model**: Single shared capital pool for all contracts
- **Investor Tracking**: Track multiple investors and their contributions
- **Profit Distribution**: Proportional profit sharing based on capital contributions
- **Capital Allocation**: Automatic capital locking for new contracts
- **Capital Returns**: Principal payments automatically free up capital
- **Management Fees**: Configurable management and Zakat deductions

### 3. Payment & Financial Management
- **Flexible Payment Processing**: Handle full, partial, and overpayments
- **Early Payment Discounts**: Configurable discount policies
- **Late Payment Handling**: Track overdue payments with penalty options
- **Cash Flow Management**: Daily ledger with complete transaction history
- **Financial Reporting**: Profit/loss statements, cash flow reports

---

## 🚀 Core Features

### Customer Management
- Complete customer lifecycle (CRUD operations)
- Soft delete with restoration capabilities
- Advanced search and filtering
- Customer document management
- Payment history tracking
- Automated reminder system

### Contract & Schedule Management
- Flexible installment contract creation
- Automatic payment schedule generation
- Payment processing with overpayment handling
- Early settlement calculations
- Contract modification and closure

### Capital Management (Pooled Model)
- Single capital pool for all contracts
- Real-time available/locked capital tracking
- Automated capital allocation and returns
- Complete transaction audit trail
- Multi-investor support with profit sharing

### Advanced Payment Processing
- Smart payment allocation (principal vs. profit)
- Overpayment handling with future schedule updates
- Partial payment support
- Early payment discounts
- Payment method tracking

### Financial Reporting
- Daily ledger with complete transaction history
- Profit distribution calculations
- Capital utilization reports
- Customer payment summaries
- Overdue payment tracking

### Document Management
- File upload and management
- Document categorization
- Secure file storage
- Document versioning

---

## 🏗 System Architecture

### Modular Design
```
📁 Nagieb Application
├── 👥 Customer Module          # Customer management
├── 🏢 Vendor Module            # Supplier/vendor management  
├── 📄 Contract Module          # Installment contracts
├── 📅 Schedule Module          # Payment schedules
├── 💳 Payment Module           # Payment processing
├── 💰 Capital Module           # Capital tracking (pooled model)
├── 📊 Profit Module            # Profit calculations & distribution
├── 📁 Document Module          # File management
├── 📖 Ledger Module            # Financial ledger
└── 👤 User Module              # User management
```

### Capital Flow Architecture
```
Investment → Capital Pool → Contract Allocation → Payment Returns → Available Capital
     ↓              ↓              ↓                    ↓                  ↓
Investor     Single Pool    Lock Capital      Free Capital       Re-allocate
Deposits   (Shared Fund)   (Contract $)      (Principal $)      (New Contracts)
```

---

## 🛠 Technology Stack

### Backend
- **Java 17+** - Latest LTS version
- **Spring Boot 3.x** - Application framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Data persistence
- **Hibernate** - ORM framework
- **MySQL 8.0+** - Primary database
- **Flyway** - Database migration management

### Development Tools
- **Maven** - Dependency management
- **MapStruct** - Object mapping
- **Lombok** - Boilerplate reduction
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **Jakarta Bean Validation** - Data validation

### Additional Features
- **Internationalization (i18n)** - Arabic & English support
- **RESTful API** - Complete REST endpoints
- **Exception Handling** - Centralized error management
- **Logging** - Comprehensive application logging
- **Audit Trail** - Complete transaction tracking

---

## 📁 Project Structure

```
src/main/java/com/mahmoud/nagieb/
├── 🔧 config/                          # Configuration classes
├── ⚠️ exception/                       # Custom exceptions & handlers
├── 📦 modules/
│   ├── installments/
│   │   ├── 👥 customer/                # Customer management
│   │   │   ├── controller/             # REST controllers
│   │   │   ├── dto/                    # Data transfer objects
│   │   │   ├── entity/                 # JPA entities
│   │   │   ├── mapper/                 # MapStruct mappers
│   │   │   ├── repo/                   # Repositories
│   │   │   └── service/                # Business logic
│   │   ├── 🏢 vendor/                  # Vendor management
│   │   ├── 📄 contract/                # Contract management
│   │   ├── 📅 schedule/                # Payment schedules
│   │   ├── 💳 payment/                 # Payment processing
│   │   ├── 💰 capital/                 # Capital management (pooled)
│   │   ├── 📊 profit/                  # Profit calculations
│   │   ├── 📁 document/                # Document management
│   │   └── 📖 ledger/                  # Financial ledger
│   └── shared/                         # Shared utilities
│       ├── user/                       # User management
│       └── partner/                    # Partner management
└── NagiebApplication.java              # Main application class

src/main/resources/
├── 🗄️ db/migration/                    # Flyway SQL migrations
│   ├── V1__create_initial_tables.sql
│   ├── V21__add_capital_tracking_tables.sql
│   └── V22__refactor_to_pooled_capital.sql
├── 🌐 messages/                        # i18n message files
│   ├── customer/messages/              # Customer module messages
│   ├── capital/messages/               # Capital module messages
│   └── shared/messages/                # Shared messages
└── application.properties              # Application configuration
```

---

## ⚡ Quick Start

### Prerequisites
- **Java 17+** (OpenJDK or Oracle)
- **Maven 3.8+**
- **MySQL 8.0+**
- **IDE** (IntelliJ IDEA recommended)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/nagieb.git
   cd nagieb
   ```

2. **Configure the database**
   ```properties
   # src/main/resources/application.properties
   spring.datasource.url=jdbc:mysql://localhost:3306/nagieb_db?useSSL=false&serverTimezone=UTC
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   
   # Enable Arabic collation
   spring.jpa.properties.hibernate.connection.characterEncoding=utf8mb4
   spring.jpa.properties.hibernate.connection.useUnicode=true
   ```

3. **Create database**
   ```sql
   CREATE DATABASE nagieb_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

4. **Build and run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **Verify installation**
   ```bash
   curl http://localhost:8080/api/v1/customers
   ```

### Initial Setup

1. **Initialize Capital Pool**
   ```bash
   POST /api/v1/capital/pool/initialize
   {
     "totalAmount": 100000,
     "ownerContribution": 50000,
     "partnerContributions": 50000,
     "description": "Initial capital pool setup"
   }
   ```

2. **Create first customer**
   ```bash
   POST /api/v1/customers
   {
     "name": "أحمد محمد",
     "phone": "+966501234567",
     "address": "الرياض، السعودية",
     "nationalId": "1234567890",
     "notes": "عميل جديد"
   }
   ```

---

## 📚 API Documentation

### Core Modules API

#### Customer Management
```http
POST   /api/v1/customers                 # Create customer
PUT    /api/v1/customers/{id}            # Update customer
GET    /api/v1/customers/{id}            # Get customer
GET    /api/v1/customers                 # List customers (paginated)
DELETE /api/v1/customers/{id}            # Soft delete customer
```

#### Contract Management
```http
POST   /api/v1/contracts                 # Create contract
GET    /api/v1/contracts/{id}            # Get contract
GET    /api/v1/contracts                 # List contracts
PUT    /api/v1/contracts/{id}/close      # Close contract
```

#### Payment Processing
```http
POST   /api/v1/payments                  # Process payment
GET    /api/v1/payments/{id}             # Get payment
GET    /api/v1/payments/schedule/{scheduleId}  # Payments for schedule
PUT    /api/v1/payments/{id}/modify      # Modify payment
```

#### Capital Management
```http
GET    /api/v1/capital/pool/status       # Get pool status
POST   /api/v1/capital/transactions      # Manual investment/withdrawal
GET    /api/v1/capital/transactions      # List transactions
GET    /api/v1/capital/reports/summary   # Capital summary report
```

#### Schedule Management
```http
GET    /api/v1/schedules/contract/{contractId}     # Get contract schedules
GET    /api/v1/schedules/overdue                   # Get overdue payments
PUT    /api/v1/schedules/{id}/skip-month           # Skip month payment
```

### Request/Response Examples

#### Create Contract
```json
POST /api/v1/contracts
{
  "customerId": 1,
  "productPurchaseId": 1,
  "totalAmount": 50000,
  "downPayment": 10000,
  "installmentAmount": 2000,
  "installmentCount": 20,
  "startDate": "2024-03-01",
  "profitRate": 15.0,
  "notes": "عقد تقسيط جهاز كمبيوتر"
}
```

#### Process Payment
```json
POST /api/v1/payments
{
  "scheduleId": 1,
  "amount": 2000,
  "actualPaymentDate": "2024-03-15",
  "paymentMethod": "CASH",
  "notes": "دفعة شهر مارس"
}
```

#### Capital Pool Status
```json
GET /api/v1/capital/pool/status
{
  "id": 1,
  "totalAmount": 100000.00,
  "availableAmount": 75000.00,
  "lockedAmount": 25000.00,
  "returnedAmount": 5000.00,
  "ownerContribution": 50000.00,
  "partnerContributions": 50000.00,
  "utilizationPercentage": 25.0
}
```

---

## 🗄️ Database Schema

### Core Tables

#### Customer & Vendor
- `customer` - Customer information
- `vendor` - Vendor/supplier information
- `product_purchase` - Product purchase records

#### Contracts & Payments
- `installment_contract` - Contract details with capital allocation
- `installment_schedule` - Payment schedules with tracking
- `payment` - Payment transactions with profit/principal split
- `payment_reminder` - Automated reminder system

#### Capital Management (Pooled Model)
- `capital_pool` - Single shared capital pool
  - `available_amount` - Capital available for new contracts
  - `locked_amount` - Capital allocated to active contracts  
  - `returned_amount` - Capital returned from payments
- `capital_transaction` - Complete audit trail
  - `available_before/after` - Balance before/after transaction
  - `locked_before/after` - Locked amount before/after transaction

#### Financial Tracking
- `daily_ledger` - Daily financial transactions
- `monthly_profit_distribution` - Profit sharing calculations
- `contract_expense` - Contract-related expenses

#### Supporting Tables
- `file_document` - Document management
- `user` - Application users
- `partner` - Investment partners

### Key Relationships
```sql
Customer 1:N ProductPurchase 1:N InstallmentContract 1:N InstallmentSchedule 1:N Payment
Contract N:1 CapitalPool (pooled capital allocation)
Payment 1:N CapitalTransaction (audit trail)
Contract 1:N ContractExpense
Payment 1:1 DailyLedger (financial recording)
```

---

## 💰 Capital Management

### Pooled Capital Model

The system uses a **revolutionary pooled capital approach** instead of per-partner capital accounts:

#### Traditional Model (Removed)
```
Partner A → Capital Account A → Contracts A1, A2, A3
Partner B → Capital Account B → Contracts B1, B2
❌ Problem: Capital isolated per partner, inflexible
```

#### Pooled Model (Implemented)
```
All Partners → Single Capital Pool → All Contracts
✅ Benefit: Shared capital, better utilization, flexible allocation
```

### Capital Flow Process

1. **Investment Phase**
   ```
   Partner Investment → Pool.totalAmount ↑
                     → Pool.availableAmount ↑
                     → Pool.partnerContributions ↑
   ```

2. **Contract Creation**
   ```
   Contract Created → Pool.availableAmount ↓
                   → Pool.lockedAmount ↑
                   → Contract.capitalAllocated = amount
   ```

3. **Payment Processing**
   ```
   Payment Received → Principal portion → Pool.lockedAmount ↓
                                      → Pool.availableAmount ↑
                                      → Pool.returnedAmount ↑
   ```

### Transaction Audit Trail

Every capital movement is recorded with complete before/after state:

```json
{
  "transactionType": "ALLOCATION",
  "amount": 10000,
  "availableBefore": 80000,
  "availableAfter": 70000,
  "lockedBefore": 20000,
  "lockedAfter": 30000,
  "contractId": 123,
  "description": "Capital allocated for contract 123"
}
```

### Benefits
- ✅ **Better Capital Utilization**: All capital available for any contract
- ✅ **Complete Audit Trail**: Every transaction tracked with before/after states  
- ✅ **Flexible Allocations**: No per-partner restrictions
- ✅ **Real-time Tracking**: Always know available vs locked capital
- ✅ **Proportional Profits**: Distribute profits by contribution percentage

---

## 🧮 Business Logic

### Payment Allocation Algorithm

When a payment is received, it's automatically split:

1. **Principal Portion**: Based on remaining principal balance
2. **Profit Portion**: Based on expected profit for the period

```java
// Simplified algorithm
BigDecimal totalRemaining = schedule.getPrincipalAmount().add(schedule.getProfitAmount());
BigDecimal principalRatio = schedule.getPrincipalAmount().divide(totalRemaining, 4, HALF_UP);
BigDecimal profitRatio = schedule.getProfitAmount().divide(totalRemaining, 4, HALF_UP);

BigDecimal principalPaid = paymentAmount.multiply(principalRatio);
BigDecimal profitPaid = paymentAmount.multiply(profitRatio);
```

### Overpayment Processing

When payment exceeds schedule amount:

1. **Complete current schedule** (principal + profit)
2. **Apply excess to future schedules** (principal only)
3. **Update capital pool** (only once for original principal)
4. **Record single transaction** (no duplicate entries)

### Early Settlement Discounts

Configurable discount policies for early payments:
- Percentage-based discounts
- Fixed amount discounts  
- Time-based discount tiers

### Profit Distribution

Monthly profit distribution based on capital contribution percentages:
```
Partner A Contribution: 60% → Gets 60% of monthly profits
Partner B Contribution: 40% → Gets 40% of monthly profits
```

---

## 🌐 Internationalization

### Supported Languages
- **Arabic (العربية)** - Default, primary language
- **English** - Secondary language

### Message Files Structure
```
src/main/resources/messages/
├── customer/messages/
│   ├── messages.properties         # Default (Arabic)
│   ├── messages_ar.properties      # Arabic
│   └── messages_en.properties      # English
├── capital/messages/
└── shared/messages/
```

### Usage
```http
# Arabic (default)
GET /api/v1/customers

# English  
GET /api/v1/customers?lang=en

# Arabic (explicit)
GET /api/v1/customers?lang=ar
```

### Sample Messages
```properties
# Arabic (messages_ar.properties)
messages.customer.created=تم إنشاء العميل بنجاح
messages.payment.processed=تم معالجة الدفعة بنجاح
messages.capital.allocated=تم تخصيص رأس المال

# English (messages_en.properties)  
messages.customer.created=Customer created successfully
messages.payment.processed=Payment processed successfully
messages.capital.allocated=Capital allocated successfully
```

---

## 🧪 Testing

### Test Structure
```
src/test/java/com/mahmoud/nagieb/
├── modules/installments/
│   ├── customer/service/CustomerServiceTest.java
│   ├── capital/service/CapitalServiceTest.java
│   ├── payment/service/PaymentServiceTest.java
│   └── contract/service/ContractServiceTest.java
└── integration/
    ├── CustomerIntegrationTest.java
    └── PaymentFlowIntegrationTest.java
```

### Running Tests
```bash
# All tests
mvn test

# Specific module
mvn test -Dtest=CustomerServiceTest

# Integration tests only
mvn test -Dtest="*IntegrationTest"

# With coverage report
mvn test jacoco:report
```

### Key Test Scenarios
- Customer CRUD operations
- Payment processing with overpayments
- Capital allocation and returns
- Contract lifecycle management
- Profit calculations
- Early settlement scenarios

---

## 🚀 Deployment

### Production Build
```bash
# Create production JAR
mvn clean package -Pprod

# Run application
java -jar target/nagieb-1.0.0.jar
```

### Environment Configuration
```properties
# Production database
spring.datasource.url=jdbc:mysql://prod-db:3306/nagieb_prod
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Security
spring.security.enabled=true
management.endpoints.web.exposure.include=health,info

# Logging
logging.level.com.mahmoud.nagieb=INFO
logging.file.name=logs/nagieb.log
```

### Docker Deployment
```dockerfile
FROM openjdk:17-jre-slim
COPY target/nagieb-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Health Checks
```http
GET /actuator/health        # Application health
GET /actuator/info          # Application information
GET /api/v1/capital/pool/status  # Capital pool status
```

---

## 🎯 Business Requirements Compliance

### ✅ Implemented Requirements

#### Original Arabic Requirements Coverage:

1. **الأقساط (Installments)**
   - ✅ Customer and vendor registration with complete data validation
   - ✅ Product cost tracking with margin calculations  
   - ✅ Automatic payment schedule generation
   - ✅ Monthly payment processing with profit/principal split
   - ✅ Capital tracking for partners with pooled model
   - ✅ Profit distribution calculations
   - ✅ Payment reminders (5 days before due)
   - ✅ Overdue payment tracking by date and location
   - ✅ Early payment discounts
   - ✅ Partner capital contributions and withdrawals
   - ✅ Monthly profit distribution with configurable percentages

2. **إدارة رؤوس الأموال (Capital Management)**
   - ✅ Pooled capital model (single shared fund)
   - ✅ Real-time available/locked capital tracking
   - ✅ Automatic capital allocation on contract creation
   - ✅ Automatic capital returns on principal payments
   - ✅ Complete audit trail with before/after balances
   - ✅ Multi-partner support with contribution tracking
   - ✅ Management fee and Zakat deduction support

3. **المالية والتقارير (Financial & Reporting)**
   - ✅ Daily ledger with complete transaction history
   - ✅ Monthly profit calculation and distribution
   - ✅ Customer payment summaries
   - ✅ Capital utilization reports
   - ✅ Overdue payment tracking
   - ✅ Contract completion handling

### 🔮 Roadmap (Future Modules)

#### Phase 2: Associations (الجمعيات)
- Group financing management
- Multi-participant associations  
- Contribution scheduling
- Association profit sharing

#### Phase 3: Debt Management (الديون)
- Personal debt tracking
- Business debt management
- Debt consolidation features
- Payment scheduling for debts

#### Phase 4: Cash Management (الكاشات)
- Daily cash flow tracking
- Cash reconciliation
- Multi-currency support
- Cash forecasting

---

## 📞 Support & Contributing

### Getting Help
- 📧 **Email**: support@nagieb-system.com
- 📖 **Documentation**: [Wiki](https://github.com/your-repo/nagieb/wiki)
- 🐛 **Issues**: [GitHub Issues](https://github.com/your-repo/nagieb/issues)

### Contributing
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Spring Boot best practices
- Write comprehensive tests
- Update documentation
- Support both Arabic and English
- Maintain backward compatibility

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- Built for Arabic/Middle Eastern business requirements
- Designed with Islamic finance principles
- Community feedback and requirements gathering
- Open source Spring Boot ecosystem

---

<div align="center">

**Made with ❤️ for the Arabic business community**

[⬆ Back to Top](#-nagieb---comprehensive-installment-management-system)

</div>
