# 🏦 Maal Flow Backend

<div align="center">

![Java](https://img.shields.io/badge/Java-21-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-green.svg)
![MySQL](https://img.shields.io/badge/MySQL-8-orange.svg)
![Maven](https://img.shields.io/badge/Maven-Build-red.svg)

**Robust backend service powering Maal Flow financial platform**

</div>

---

## 📌 Overview

The **Maal Flow Backend** is a modular, production-ready Spring Boot application designed to handle complex financial workflows including:

- Installment-based contracts
- Capital pool management
- Payment processing
- Profit distribution
- Financial auditing & reporting

It follows clean architecture principles with strong separation of concerns and scalable modular design.

---

## 🧠 Core Concepts

### 💰 Pooled Capital Model
Instead of isolated accounts, all investments are managed in a **shared capital pool**, enabling:

- Better capital utilization
- Flexible allocation across contracts
- Real-time tracking of available vs locked capital

### 📊 Financial Accuracy
- Profit is separated from principal
- Every transaction is tracked (before/after state)
- Supports overpayments, partial payments, and early settlement

### 🌍 Bilingual Ready
- Arabic (default)
- English support via i18n

---

## 🏗 Architecture

```
modules/
├── installments/
│   ├── customer/
│   ├── vendor/
│   ├── contract/
│   ├── schedule/
│   ├── payment/
│   ├── capital/
│   ├── profit/
│   ├── document/
│   └── ledger/
└── shared/
    ├── user/
    └── partner/
```

### Layered Structure
Each module follows:

- Controller → API layer
- Service → business logic
- Repository → data access
- Entity → database model
- DTO / Mapper → data transformation

---

## ⚙️ Tech Stack

### Core
- Java 21
- Spring Boot 4.x
- Spring Data JPA
- Flyway (DB migrations)
- MySQL

### Tooling
- Maven
- MapStruct (mapping)
- Lombok (boilerplate reduction)
- Bean Validation
- Actuator + Prometheus (monitoring)

---

## 🚀 Getting Started

### 1️⃣ Prerequisites

- Java 21+
- Maven 3.8+
- MySQL 8+
- Docker (optional)

---

### 2️⃣ Run Database (Docker)

```bash
docker compose up -d
```

Default:
```
Host: localhost
Port: 3307
```

---

### 3️⃣ Configure Application

Update:

```
src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/installments
spring.datasource.username=root
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
```

---

### 4️⃣ Run Application

```bash
mvn clean install
mvn spring-boot:run
```

---

### 5️⃣ Verify

```
http://localhost:8080/actuator/health
```

---

## 📡 API Overview

### Customers
```
POST   /api/v1/customers
GET    /api/v1/customers
GET    /api/v1/customers/{id}
PUT    /api/v1/customers/{id}
DELETE /api/v1/customers/{id}
```

### Contracts
```
POST   /api/v1/contracts
GET    /api/v1/contracts
PUT    /api/v1/contracts/{id}/close
```

### Payments
```
POST   /api/v1/payments
GET    /api/v1/payments/{id}
```

### Capital
```
GET    /api/v1/capital/pool/status
POST   /api/v1/capital/transactions
```

---

## 🗄 Database Management

- Managed via **Flyway**
- Migration scripts:
```
src/main/resources/db/migration
```

### Naming Convention
```
V1__init.sql
V2__add_feature.sql
```

---

## 📈 Monitoring & Observability

Spring Boot Actuator endpoints:

```
/actuator/health
/actuator/info
/actuator/metrics
```

Prometheus metrics enabled via:
```
micrometer-registry-prometheus
```

---

## 🧪 Testing

Run all tests:

```bash
mvn test
```

Run specific:

```bash
mvn -Dtest=CustomerServiceTest test
```

---

## 🛠 Development Guidelines

- Follow modular structure
- Keep business logic inside services
- Avoid logic inside controllers
- Use DTOs (never expose entities directly)
- Write tests for critical flows

---

## 🚀 Deployment

### Build JAR

```bash
mvn clean package
```

### Run

```bash
java -jar target/maal-flow-0.0.1-SNAPSHOT.jar
```

---

## 🔮 Future Enhancements

- Associations (group financing)
- Debt tracking module
- Advanced reporting dashboards
- Multi-currency support

---

## 🤝 Contributing

1. Create branch
2. Add feature
3. Write tests
4. Open PR

---

##  Author

**Mahmoud Ramadan**  
Email: [mahmoudramadan385@gmail.com](mailto:mahmoudramadan385@gmail.com)


---

## 📄 License

MIT License

---

<div align="center">

**Backend built for scalable financial systems**

</div>
