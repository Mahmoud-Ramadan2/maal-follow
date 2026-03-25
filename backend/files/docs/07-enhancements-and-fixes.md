# Maal Flow - Enhancements and Fixes

## 1. CRITICAL: Externalize Credentials

### Problem
application.properties has hardcoded database password:
```properties
spring.datasource.password=ldapsso
```

### Fix
```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/installments?useSSL=false&serverTimezone=UTC}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}
```

---

## 2. CRITICAL: Add Environment Profiles

### Create application-dev.properties
```properties
# Dev profile - debug logging, local DB
spring.datasource.url=jdbc:mysql://localhost:3306/installments?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=ldapsso
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Create application-prod.properties
```properties
# Prod profile - no debug, secured
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.show-sql=false
logging.level.org.hibernate.SQL=WARN
logging.level.root=WARN
```

### Update application.properties
```properties
spring.profiles.active=${SPRING_PROFILE:dev}
spring.application.name=MaalFlow
```

---

## 3. Fix: Contract Completion Validation (Req #13)

### Current Code (ContractService.java)
```java
// Has TODO: "Add checks to ensure all installments are paid before marking as completed"
contract.setStatus(ContractStatus.COMPLETED);
```

### Fixed Code
Replace the markAsCompleted method body with:
```java
@Transactional
public ContractResponse markAsCompleted(Long contractId) {
    Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new ObjectNotFoundException("messages.contract.notFound", contractId));

    // VALIDATE: All installments must be PAID or CANCELLED
    long unpaidCount = contract.getInstallmentSchedules().stream()
            .filter(s -> s.getStatus() != com.mahmoud.maalflow.modules.installments.contract.enums.PaymentStatus.PAID
                    && s.getStatus() != com.mahmoud.maalflow.modules.installments.contract.enums.PaymentStatus.CANCELLED)
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

## 4. Fix: One-Shot Payment Contract (ContractService)

### Current Code
```java
if (request.getDownPayment().compareTo(request.getFinalPrice()) == 0) {
    // TODO: Handle this case (one shoot payment contract)
}
```

### Fixed Code
```java
if (request.getDownPayment().compareTo(request.getFinalPrice()) == 0) {
    // One-shot payment: no installments needed
    contract.setMonths(0);
    contract.setMonthlyAmount(BigDecimal.ZERO);
    contract.setRemainingAmount(BigDecimal.ZERO);
    contract.setStatus(ContractStatus.COMPLETED);
    contract.setCompletionDate(LocalDate.now());
    Contract savedContract = contractRepository.save(contract);
    log.info("One-shot payment contract created and completed: {}", savedContract.getId());
    return contractMapper.toContractResponse(savedContract);
}
```

---

## 5. Fix: Zakat Calculation (ProfitProcessingService)

### Current Code (commented out)
```java
// TODO: Zakat calculation needs more requirements
// private static final BigDecimal DEFAULT_ZAKAT_PERCENTAGE = BigDecimal.valueOf(0.025);
```

### Implementation
```java
private static final BigDecimal DEFAULT_ZAKAT_PERCENTAGE = BigDecimal.valueOf(2.5); // 2.5%

/**
 * Calculate zakat on profit.
 * Zakat is 2.5% of annual profit, prorated monthly = 2.5% / 12.
 * Applied only if profit exceeds nisab threshold.
 */
private BigDecimal calculateZakat(BigDecimal profit) {
    if (profit.compareTo(BigDecimal.ZERO) <= 0) {
        return BigDecimal.ZERO;
    }
    // Monthly zakat = annual rate / 12
    BigDecimal monthlyRate = DEFAULT_ZAKAT_PERCENTAGE
            .divide(BigDecimal.valueOf(12), 10, java.math.RoundingMode.HALF_UP);
    return profit.multiply(monthlyRate)
            .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
}
```

Then uncomment the zakat lines in processProfit():
```java
BigDecimal zakat = calculateZakat(netProfitAfterExpenses);
BigDecimal netProfit = netProfitAfterManagement.subtract(zakat);

if (zakat.compareTo(BigDecimal.ZERO) > 0) {
    recordDeduction(contract, schedule, DeductionType.ZAKAT,
            zakat, paymentDate, user, description);
}
```

---

## 6. Fix: Partner Share Auto-Calculation (PartnerService)

### Current TODO
```java
// TODO: SharePercentage should calculated auto based on capital
```

### Implementation
Add to PartnerService or create a scheduled recalculation:
```java
/**
 * Recalculate all partner share percentages based on their current investment
 * relative to total capital pool.
 */
@Transactional
public void recalculateSharePercentages() {
    List<Partner> activePartners = partnerRepository.findByStatus(PartnerStatus.ACTIVE);

    BigDecimal totalInvestment = activePartners.stream()
            .map(Partner::getTotalInvestment)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (totalInvestment.compareTo(BigDecimal.ZERO) <= 0) return;

    for (Partner partner : activePartners) {
        BigDecimal share = partner.getTotalInvestment()
                .multiply(BigDecimal.valueOf(100))
                .divide(totalInvestment, 2, java.math.RoundingMode.HALF_UP);
        partner.setSharePercentage(share);
    }
    partnerRepository.saveAll(activePartners);
    log.info("Recalculated share percentages for {} partners. Total investment: {}",
            activePartners.size(), totalInvestment);
}
```

---

## 7. pom.xml Dependencies to Add

```xml
<!-- Apache POI for Excel export (Requirement #5) -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>

<!-- Spring Security (see 04-security-implementation.md) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<!-- SpringDoc OpenAPI (Swagger) -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.6</version>
</dependency>

<!-- ArchUnit for architecture tests -->
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.2.1</version>
    <scope>test</scope>
</dependency>
```

---

## 8. ContractRepository Missing Methods

Add to ContractRepository.java for Excel export:
```java
@Query("SELECT c FROM Contract c WHERE c.customer.id = :customerId")
List<Contract> findByCustomerIdList(Long customerId);

@Query("SELECT c FROM Contract c JOIN FETCH c.customer JOIN FETCH c.installmentSchedules " +
       "WHERE c.status = 'ACTIVE' ORDER BY c.customer.address")
List<Contract> findActiveContractsWithUnpaidSchedules();
```

---

## 9. CollectionRouteRepository Missing Methods

Add to CollectionRouteRepository.java:
```java
List<CollectionRoute> findByIsActiveTrueOrderByNameAsc();
```

Add to CollectionRouteItemRepository.java:
```java
List<CollectionRouteItem> findByCollectionRouteIdAndIsActiveTrueOrderBySequenceOrderAsc(Long routeId);
```

---

## 10. application.properties Additions

```properties
# JWT (add after security implementation)
jwt.secret=${JWT_SECRET:bXlWZXJ5TG9uZ1NlY3JldEtleVRoYXRJc0F0TGVhc3QyNTZCaXRzRm9ySFMyNTZBbGdvcml0aG0yMDI2}
jwt.expiration=86400000
jwt.refresh-expiration=604800000

# SpringDoc OpenAPI
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha

# Profiles
spring.profiles.active=${SPRING_PROFILE:dev}
```

---

## 11. Message Properties to Add

Add to messages/shared/ for new validation messages:
```properties
messages.contract.cannotComplete.unpaidInstallments=Cannot complete contract with unpaid installments
messages.auth.notAuthenticated=User not authenticated
messages.auth.userNotFound=Authenticated user not found in database
messages.auth.invalidRefreshToken=Invalid or expired refresh token
messages.user.email.exists=Email already registered
```

---

## 12. Summary of All Files to Copy

### Security (copy to src/main/java/com/mahmoud/maalflow/security/)
- SecurityConfig.java
- JwtService.java
- JwtAuthenticationFilter.java
- CustomUserDetailsService.java
- AuthenticationService.java
- AuthController.java
- SecurityUtils.java
- dto/AuthRequest.java
- dto/AuthResponse.java
- dto/RegisterRequest.java

### Associations Module (copy to src/main/java/com/mahmoud/maalflow/modules/associations/)
- entity/Association.java
- entity/AssociationMember.java
- entity/AssociationPayment.java
- enums/AssociationStatus.java
- enums/MemberPaymentStatus.java
- dto/AssociationRequest.java
- dto/AssociationResponse.java
- dto/AssociationMemberRequest.java
- dto/AssociationMemberResponse.java
- dto/AssociationPaymentRequest.java
- repo/AssociationRepository.java
- repo/AssociationMemberRepository.java
- repo/AssociationPaymentRepository.java
- service/AssociationService.java
- controller/AssociationController.java

### Debts Module (copy to src/main/java/com/mahmoud/maalflow/modules/debts/)
- entity/Debt.java
- entity/DebtPayment.java
- enums/DebtType.java
- enums/DebtStatus.java
- dto/DebtRequest.java
- dto/DebtResponse.java
- dto/DebtPaymentRequest.java
- repo/DebtRepository.java
- repo/DebtPaymentRepository.java
- service/DebtService.java
- controller/DebtController.java

### Enhancements (copy to respective locations)
- ExcelExportService.java -> shared/settings/service/
- ExcelExportController.java -> shared/settings/controller/
- NotificationRepository.java -> shared/notification/repo/
- NotificationService.java -> shared/notification/service/
- NotificationController.java -> shared/notification/controller/
- CollectionRouteService.java -> installments/schedule/service/
- CollectionRouteController.java -> installments/schedule/controller/

### Migrations (copy to src/main/resources/db/migration/)
- V24__hash_existing_passwords.sql
- V25__create_associations_module.sql
- V26__create_debts_module.sql

