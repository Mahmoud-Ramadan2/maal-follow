# 🏦 Maal Flow

<div align="center">

![Full Stack](https://img.shields.io/badge/Full%20Stack-Application-blue.svg)
![Backend](https://img.shields.io/badge/Backend-Spring%20Boot%204-green.svg)
![Frontend](https://img.shields.io/badge/Frontend-React%2019-blue.svg)
![Database](https://img.shields.io/badge/Database-MySQL-orange.svg)

**A modern financial operations platform for installment-based businesses**

</div>

---

## 📌 Overview

**Maal Flow** is a full-stack financial system designed to manage installment workflows with:

- Clear financial tracking
- Accurate profit distribution
- Strong auditability
- Scalable architecture

It provides a complete solution for handling:

- Customers & contracts
- Installment payments
- Capital investments
- Financial reporting

---

## 🧠 System Architecture

```
maalflow/
├── backend/     # Spring Boot API
├── frontend/    # React application
└── README.md    # Project overview
```

### 🔗 Documentation

- 📦 Backend → `backend/README.md`
- 🎨 Frontend → `frontend/README.md`

---

## ⚙️ Tech Stack

### Backend
- Java 21
- Spring Boot 4
- Spring Data JPA
- Flyway
- MySQL
- Maven

### Frontend
- React 19
- TypeScript
- Vite
- Material UI
- i18next

---

## 🚀 Quick Start

### 1️⃣ Start Database

```bash
cd backend
docker compose up -d
```

---

### 2️⃣ Run Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend runs on:
```
http://localhost:8080
```

---

### 3️⃣ Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on:
```
http://localhost:3000
```

---

## 🔄 How It Works

1. Create customers
2. Create installment contracts
3. Track payments
4. Manage capital pool
5. Generate financial reports

---

## 🎯 Key Features

- 📊 Full financial lifecycle management
- 💰 Pooled capital model
- 🔍 Complete audit trail
- 🌍 Arabic & English support
- ⚡ Modular and scalable architecture

---

## 📂 Project Philosophy

- Separation of concerns (frontend vs backend)
- Domain-driven modular backend
- Clean API design
- Database versioning with Flyway

---

## 🛠 Development Workflow

```bash
# Backend
cd backend
mvn spring-boot:run

# Frontend
cd frontend
npm run dev
```

---

## 🔮 Roadmap

- Associations module (group financing)
- Debt tracking
- Advanced dashboards
- Multi-currency support

---

## 🤝 Contributing

1. Fork repository
2. Create feature branch
3. Commit changes
4. Open Pull Request

---


##  Author

**Mahmoud Ramadan**  
Email: [mahmoudramadan385@gmail.com](mailto:mahmoudramadan385@gmail.com)

---

## 📄 License

MIT License

---

<div align="center">

**Maal Flow — Financial clarity, built for scale**

</div>
