# Maal Flow - Architecture Review

## 1. Is This a Modular Monolith?

### Current State: PARTIALLY Modular Monolith

The project attempts a modular monolith architecture but falls short in several areas.

### What's Good (Modular)
```
modules/
  installments/          <-- Module 1: Installment Sales
    customer/            <-- Sub-module with full layers
      controller/
      dto/
      entity/
      enums/
      mapper/
      repo/
      service/
    vendor/              <-- Same pattern repeated
    contract/
    payment/
    partner/
    ...
  associations/          <-- Module 2 (empty)
  debts/                 <-- Module 3 (empty)
  shared/                <-- Shared kernel
    user/
    notification/
    audit/
    settings/
```

Each sub-module follows consistent layered architecture (controller -> service -> repo -> entity) which is good.

### What's NOT Modular (Problems)

#### Problem 1: No Module Boundaries
Modules directly import each other's internal classes. For example:
- `PaymentService` imports `ContractRepository` directly
- `PaymentProcessingService` imports `CapitalService`, `ProfitProcessingService`
- `ContractService` imports `PartnerRepository`, `PurchaseRepository`

In a proper modular monolith, modules should communicate through:
- Public API interfaces (not internal repos)
- Events (ApplicationEvent)
- Shared DTOs (not entities)

#### Problem 2: Entity Leakage Across Modules
Services in one module directly manipulate entities from another module:
- `PaymentProcessingService` directly modifies `InstallmentSchedule` entity
- `ContractService` directly reads `Purchase` entity
- `LedgerService` directly reads `Partner` entity

#### Problem 3: No Access Control Between Modules
Any class can import any other class. No package-private restrictions, no module-info.java.

#### Problem 4: Circular Dependencies
The codebase already identified one:
- PaymentService -> InstallmentScheduleService (extracted PaymentProcessingService to break it)
- But more subtle ones exist through shared repository access

#### Problem 5: Shared Module is Too Thin
The `shared/` module has entities but almost no services. Things that should be shared services:
- UserService (doesn't exist)
- NotificationService (doesn't exist)
- AuditService (doesn't exist)

## 2. How to Make It a Proper Modular Monolith

### Step 1: Define Module APIs

Each module should expose a public service interface:

```
modules/installments/
  api/                           <-- PUBLIC (other modules use this)
    InstallmentModuleApi.java    <-- Interface
    dto/                         <-- Shared DTOs
  internal/                      <-- PRIVATE (only this module)
    customer/
    contract/
    payment/
    ...
```

Example:
```java
// modules/installments/api/InstallmentModuleApi.java
public interface InstallmentModuleApi {
    ContractSummaryDTO getContractSummary(Long contractId);
    BigDecimal getMonthlyExpectedAmount();
    BigDecimal getTotalNetProfit();
}
```

### Step 2: Use Events for Cross-Module Communication

Instead of direct calls:
```java
// BAD: PaymentService directly calling CapitalService
capitalService.returnCapitalFromPayment(contract, principalPaid, paymentId, user);

// GOOD: Publish event, let capital module handle it
applicationEventPublisher.publishEvent(new PaymentProcessedEvent(
    contract.getId(), principalPaid, paymentId
));
```

Create event classes:
```java
public record PaymentProcessedEvent(Long contractId, BigDecimal principalPaid, Long paymentId) {}
public record ContractCreatedEvent(Long contractId, BigDecimal capitalRequired) {}
public record ContractCompletedEvent(Long contractId) {}
```

### Step 3: Use ArchUnit to Enforce Boundaries

Add to tests:
```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.2.1</version>
    <scope>test</scope>
</dependency>
```

```java
@ArchTest
static final ArchRule paymentShouldNotAccessContractInternals =
    noClasses().that().resideInAPackage("..payment..")
        .should().accessClassesThat().resideInAPackage("..contract.repo..");
```

### Step 4: Package-Private Internal Classes

Make internal services package-private. Only the API interface is public.

## 3. Other Architecture Issues

### Issue 1: Spring Boot 4.0.0 (Bleeding Edge)
Spring Boot 4.0.0 is very new. Many libraries may not be compatible yet.
Consider if 3.4.x is more stable for production.

### Issue 2: Hardcoded Credentials in application.properties
```properties
spring.datasource.password=ldapsso  # NEVER commit passwords
```
Use environment variables or Spring profiles:
```properties
spring.datasource.password=${DB_PASSWORD}
```

### Issue 3: SQL Logging Enabled in All Environments
```properties
logging.level.org.hibernate.SQL=DEBUG
spring.jpa.show-sql=true
```
This should only be in dev profile, not production.

### Issue 4: Missing application-dev.properties / application-prod.properties
No environment-specific configuration. Should have:
- application-dev.properties (debug logging, local DB)
- application-prod.properties (no debug, secured DB, no show-sql)

### Issue 5: No API Versioning Strategy
APIs use `/api/v1/` prefix which is good, but no documentation or migration strategy.

### Issue 6: Flyway Migration Naming
V1 has double extension: `V1__create_initial_tables.sql.sql`
This may cause issues with Flyway validation.

### Issue 7: No Transaction Boundary Documentation
Complex operations like processPayment() span multiple services with @Transactional.
Need to document which operations are atomic.

### Issue 8: BigDecimal Everywhere Without Scale Standardization
Different entities use different scales (scale=2, scale=5, scale=10).
Should standardize: financial amounts = scale 2, percentages = scale 4.

## 4. Recommended Architecture Changes (Priority Order)

1. **CRITICAL**: Add Spring Security (see 04-security-implementation.md)
2. **HIGH**: Externalize configuration (passwords, URLs)
3. **HIGH**: Add environment profiles (dev/prod)
4. **MEDIUM**: Define module API boundaries
5. **MEDIUM**: Add Spring ApplicationEvents for cross-module communication
6. **MEDIUM**: Add comprehensive tests (integration + controller)
7. **LOW**: Add ArchUnit rules
8. **LOW**: Consider moving to Spring Modulith for automatic boundary enforcement

## 5. Dependency Graph (Current)

```
                    +--[ shared ]--+
                    |    user      |
                    |    notification (entity only)
                    |    audit (entity only)
                    |    settings (entity only)
                    +--------------+
                          |
    +----------+----------+----------+----------+
    |          |          |          |          |
customer   vendor    purchase   partner    capital
    |          |          |          |          |
    +-----+----+-----+----+-----+----+---------+
          |          |          |
       contract   payment   profit/ledger
          |          |          |
          +-----+----+-----+----+
                |
         PaymentProcessingService
         (breaks circular dep)
```

The fact that PaymentProcessingService was created specifically to break a circular
dependency indicates the architecture is becoming tangled. Events would solve this cleanly.

