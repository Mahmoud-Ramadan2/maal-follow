# Maal Flow - Missing Features and Enhancements

## Feature 1: Excel Export Service (Requirement #5)

### What's Missing
- ExportTemplate entity exists in shared/settings but NO service, NO controller
- Apache POI dependency not in pom.xml

### Dependencies to Add (pom.xml)
```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

### Files to Create
- See `code/enhancements/ExcelExportService.java`
- See `code/enhancements/ExcelExportController.java`

---

## Feature 2: Notification Service (Requirements #17, #19)

### What's Missing
- Notification entity and enums exist but NO repository, NO service, NO controller
- PaymentReminderService.sendReminder() is a stub (TODO comment)
- No actual notification delivery mechanism

### Files to Create
- See `code/enhancements/NotificationRepository.java`
- See `code/enhancements/NotificationService.java`
- See `code/enhancements/NotificationController.java`

---

## Feature 3: Collection Route Service (Requirement #6)

### What's Missing
- CollectionRoute and CollectionRouteItem entities exist
- Repos exist in schedule/repo
- NO service, NO controller, NO DTOs

### Files to Create
- See `code/enhancements/CollectionRouteService.java`
- See `code/enhancements/CollectionRouteController.java`
- See `code/enhancements/CollectionRouteDTO.java`

---

## Feature 4: Zakat Calculation (Requirement #9)

### What's Missing
- Commented out in ProfitProcessingService lines 50 and 135
- No configuration entity for zakat rules
- Needs business rules clarification for:
  - Zakat calculation base (on profit? on capital? on both?)
  - When to deduct (monthly? annually?)
  - How to handle fractional year investments

### Implementation Path
Uncomment and implement in ProfitProcessingService.processProfit():
```java
// Calculate zakat (2.5% of annual profit, prorated monthly)
BigDecimal zakat = calculateZakat(netProfitAfterExpenses);
BigDecimal netProfit = netProfitAfterManagement.subtract(zakat);
```

---

## Feature 5: User Service and Controller

### What's Missing
- User entity and UserRepository exist
- NO UserService, NO UserController
- Can't manage users via API

### Files to Create
- See `code/enhancements/UserService.java`
- See `code/enhancements/UserController.java`

---

## Feature 6: Audit Log Service

### What's Missing
- AuditLog entity exists
- NO repository, NO service
- No automatic audit logging on entity changes

### Implementation Approach
Use Spring AOP or JPA EntityListeners to auto-record changes.

---

## Feature 7: Application Settings Service

### What's Missing
- ApplicationSetting entity exists
- NO repository, NO service, NO controller
- System configs are hardcoded (management fee %, reminder days, etc.)

---

## Feature 8: Cloud Backup/Sync (Requirement #18b)

### Options
1. MySQL scheduled dumps to cloud storage (S3/GCS)
2. Spring Boot Actuator + database backup endpoint
3. Flyway-based schema versioning (already done) + data export

### Recommendation
Add a scheduled MySQL dump to a cloud bucket using a @Scheduled job.

---

## Feature 9: API Documentation (Swagger/OpenAPI)

### Dependencies to Add (pom.xml)
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.6</version>
</dependency>
```

### application.properties additions
```properties
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
```

---

## Feature 10: Contract Completion Validation (Requirement #13)

### Current Issue
ContractService.markAsCompleted() has TODO: "Add checks to ensure all installments are paid"

### Fix (in ContractService.java)
```java
public ContractResponse markAsCompleted(Long contractId) {
    Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new ObjectNotFoundException("messages.contract.notFound", contractId));
    
    // VALIDATION: Check all schedules are PAID
    long unpaidCount = contract.getInstallmentSchedules().stream()
            .filter(s -> s.getStatus() != PaymentStatus.PAID && s.getStatus() != PaymentStatus.CANCELLED)
            .count();
    if (unpaidCount > 0) {
        throw new BusinessException("messages.contract.cannotComplete.unpaidInstallments");
    }
    
    contract.setStatus(ContractStatus.COMPLETED);
    contract.setCompletionDate(LocalDate.now());
    return contractMapper.toContractResponse(contractRepository.save(contract));
}
```

---

## Feature 11: One-Shot Payment Contract

### Current Issue
ContractService has TODO for handling when downPayment == finalPrice

### Fix
When downPayment equals finalPrice, create contract with 0 months, mark as COMPLETED immediately, record full payment in ledger.

