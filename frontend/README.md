# 🎨 Maal Flow Frontend

<div align="center">

![React](https://img.shields.io/badge/React-19-blue.svg)
![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6.svg)
![Vite](https://img.shields.io/badge/Vite-7-646CFF.svg)
![Material UI](https://img.shields.io/badge/Material%20UI-7-007FFF.svg)

**Modern, bilingual web application for Maal Flow financial operations**

</div>

---

## 📌 Overview

The **Maal Flow Frontend** is a scalable React application that delivers a clear and efficient user experience for installment-based financial workflows.

It is built to support operational teams with:
- Fast and intuitive interfaces
- Reliable API integration with the backend
- Consistent design system and reusable UI components
- Arabic and English localization

---

## 🧠 Core Principles

### 🧩 Modular UI Architecture
The frontend is organized by reusable layers (`components`, `pages`, `hooks`, `services`, `contexts`) to keep development clean and maintainable.

### 🌍 Internationalization First
The application is prepared for multilingual usage through `i18next`, with Arabic and English support.

### 🔐 Production-Oriented Patterns
The codebase uses type-safe configuration, route protection, and validated form handling patterns suited for business-critical workflows.

---

## 🏗 Project Structure

```text
src/
├── assets/               # Icons, images, static assets
├── components/
│   ├── common/           # Shared business components
│   ├── layout/           # Page shells and navigation layout
│   └── ui/               # Reusable UI primitives
├── config/               # Environment, i18n, theme configuration
├── contexts/             # Auth, language, theme contexts
├── hooks/
│   ├── common/           # Shared hooks
│   └── modules/          # Domain-specific hooks
├── pages/
│   ├── auth/
│   ├── Dashboard/
│   └── modules/
├── router/               # Route definitions and protection logic
├── services/api/         # API layer and request utilities
├── styles/               # Global styles and variables
├── types/                # Shared and module-specific types
└── utils/                # Constants, helpers, validators
```

---

## ⚙️ Tech Stack

### Core
- React 19
- TypeScript
- Vite 7
- React Router 7
- Material UI 7

### Supporting Libraries
- Axios (API requests)
- i18next + react-i18next (localization)
- React Hook Form + Zod (forms and validation)
- Zustand (state management)
- date-fns (date utilities)

---

## 🚀 Getting Started

### 1️⃣ Prerequisites

- Node.js 20+
- npm 10+
- Running backend API (default: `http://localhost:8080`)

---

### 2️⃣ Install dependencies

```powershell
npm install
```

---

### 3️⃣ Run development server

```powershell
npm run dev
```

Application URL:

```text
http://localhost:3000
```

---

### 4️⃣ Build for production

```powershell
npm run build
```

---

### 5️⃣ Preview production build

```powershell
npm run preview
```

---

## 🔧 Environment Configuration

Frontend environment files:
- `.env.development`
- `.env.production`

Important variables:

```dotenv
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_APP_NAME=MaalFlow
VITE_APP_VERSION=0.0.0
VITE_DEFAULT_LANGUAGE=ar
VITE_ENABLE_DEBUG=true
VITE_DEFAULT_PAGE_SIZE=10
```

---

## 🔌 Development Server Behavior

From `vite.config.ts`:
- Dev server port: `3000`
- Auto-open browser: enabled
- Proxy: `/api` -> `http://localhost:8080`

Path aliases are available (examples):
- `@`, `@components`, `@pages`, `@services`, `@hooks`, `@utils`, `@contexts`, `@config`, `@assets`

---

## 📜 Available Scripts

```powershell
npm run dev
npm run build
npm run lint
npm run preview
```

---

## 🛠 Development Guidelines

- Keep UI logic in components and domain hooks
- Keep API interactions in `services/api`
- Keep shared types in `types/`
- Use validators from `utils/validators` and schema-based form validation
- Reuse design tokens from theme and global style configuration

---

## 🤝 Contributing

1. Create a feature branch
2. Implement changes with clear commit messages
3. Run lint and build checks
4. Open a pull request with a concise description

---

## Author

**Mahmoud Ramadan**  
Email: [mahmoudramadan385@gmail.com](mailto:mahmoudramadan385@gmail.com)

---

## 📄 License

MIT License

---

<div align="center">

**Frontend crafted for clarity, speed, and financial accuracy**

</div>

