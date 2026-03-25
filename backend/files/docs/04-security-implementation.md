# Maal Flow - Security Implementation Guide

## Current State

- security/ folder is COMPLETELY EMPTY
- No Spring Security dependency in pom.xml
- No JWT library
- All services hardcode userId = 1 (15+ TODO comments)
- User entity has password field but no hashing
- UserRole enum has: ADMIN, USER, MANAGER, COLLECTOR
- CorsConfig exists but no security filter chain

## What to Implement

1. Add Maven dependencies
2. Create SecurityConfig (filter chain)
3. Create JwtService (token generation/validation)
4. Create JwtAuthenticationFilter
5. Create AuthenticationService (login/register)
6. Create AuthController
7. Create SecurityUtils (replace all hardcoded userId=1)
8. Add password hashing migration
9. Update all services to use SecurityUtils

## Step 1: Maven Dependencies

Add to pom.xml inside `<dependencies>`:

```xml
<!-- Spring Security -->
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
```

## Step 2: application.properties additions

```properties
# JWT Configuration
jwt.secret=${JWT_SECRET:myVeryLongSecretKeyThatIsAtLeast256BitsForHS256Algorithm2026}
jwt.expiration=86400000
jwt.refresh-expiration=604800000
```

## Step 3-8: Copy-Paste Ready Files

All Java files are in `files/code/security/` directory:

| File | Purpose |
|------|---------|
| SecurityConfig.java | Filter chain, CORS, public/private endpoints |
| JwtService.java | Token generation, validation, parsing |
| JwtAuthenticationFilter.java | Extract JWT from requests |
| AuthenticationService.java | Login, register, token refresh |
| AuthController.java | /api/v1/auth/login, /register, /refresh |
| AuthRequest.java | Login request DTO |
| AuthResponse.java | Login response DTO (token) |
| RegisterRequest.java | Registration request DTO |
| SecurityUtils.java | Get current user, replace all userId=1 |
| CustomUserDetailsService.java | Load user by email for Spring Security |
| V24__hash_existing_passwords.sql | Migration to hash existing passwords |

## Step 9: Update All Services

After implementing security, replace ALL occurrences of:
```java
// OLD (appears 15+ times):
User currentUser = userRepository.findById(1L).orElse(null);

// NEW:
User currentUser = SecurityUtils.getCurrentUser();
```

Files to update:
- ContractService.java (3 occurrences)
- PaymentService.java (1 occurrence)
- LedgerService.java (1 occurrence)
- CustomerService.java (1 occurrence)
- PartnerService.java (2 occurrences)
- PartnerWithdrawalService.java (2 occurrences)

## Endpoint Permission Mapping

| Role | Endpoints |
|------|-----------|
| ADMIN | ALL endpoints |
| MANAGER | All except user management |
| COLLECTOR | GET all + POST payments + PUT schedules |
| USER | GET own data only |

Public endpoints (no auth required):
- POST /api/v1/auth/login
- POST /api/v1/auth/register
- GET /api/v1/auth/refresh
- GET /swagger-ui/**
- GET /api-docs/**

