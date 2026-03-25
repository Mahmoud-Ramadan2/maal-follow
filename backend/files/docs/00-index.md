# Maal Flow - Project Documentation Index

> Generated: February 28, 2026
> Tech Stack: Spring Boot 4.0.0 | Java 21 | MySQL | Flyway | MapStruct | Lombok

## Documentation Files

| # | File | Description |
|---|------|-------------|
| 01 | [01-progress-report.md](./01-progress-report.md) | Requirements status and TODO tracking |
| 02 | [02-missing-features.md](./02-missing-features.md) | Specifications for unimplemented features |
| 03 | [03-architecture-review.md](./03-architecture-review.md) | Modular monolith assessment |
| 04 | [04-security-implementation.md](./04-security-implementation.md) | Spring Security + JWT guide |
| 05 | [05-associations-module.md](./05-associations-module.md) | Associations module spec and code |
| 06 | [06-debts-module.md](./06-debts-module.md) | Debts module spec and code |
| 07 | [07-enhancements-and-fixes.md](./07-enhancements-and-fixes.md) | Code improvements and TODO fixes |

## Ready-to-Paste Code (in files/code/)

| Folder | Contents |
|--------|----------|
| code/security/ | SecurityConfig, JwtFilter, AuthService, AuthController, SecurityUtil |
| code/associations/ | Full module: entity, dto, repo, service, controller |
| code/debts/ | Full module: entity, dto, repo, service, controller |
| code/enhancements/ | ExcelExportService, NotificationService, CollectionRouteService, migration SQL |

## Overall Progress

| Module | Status | Completion |
|--------|--------|------------|
| Installments | In Progress | ~65% |
| Associations | Not Started | 0% |
| Debts | Not Started | 0% |
| Cash Flow | Partial | ~30% |
| Security | Not Started | 0% |

## Reading Order

1. 01-progress-report -> understand current state
2. 03-architecture-review -> structural understanding
3. 04-security -> implement FIRST (blocks everything)
4. 05/06 -> new modules
5. 07 -> fixes
6. 02 -> remaining features

