# MaalFlow Frontend — Architecture Documentation

> **Bilingual installments management app** (Arabic default / English)  
> React 19 · TypeScript 5.9 · Vite 7 · MUI 7 · Spring Boot backend

---

## Table of Contents

1. [What Is This Project?](#1-what-is-this-project)
2. [Tech Stack & Why Each Choice](#2-tech-stack--why-each-choice)
3. [Project File Tree](#3-project-file-tree)
4. [High-Level Architecture](#4-high-level-architecture)
5. [Layer 1 — Configuration](#5-layer-1--configuration)
6. [Layer 2 — Styling & RTL System](#6-layer-2--styling--rtl-system)
7. [Layer 3 — Internationalization (i18n)](#7-layer-3--internationalization-i18n)
8. [Layer 4 — HTTP Client & API Layer](#8-layer-4--http-client--api-layer)
9. [Layer 5 — TypeScript Types](#9-layer-5--typescript-types)
10. [Layer 6 — Custom Hooks](#10-layer-6--custom-hooks)
11. [Layer 7 — Router & Navigation](#11-layer-7--router--navigation)
12. [Layer 8 — Layout Components](#12-layer-8--layout-components)
13. [Layer 9 — Reusable UI Components](#13-layer-9--reusable-ui-components)
14. [Layer 10 — Purchase Module (First Domain Module)](#14-layer-10--purchase-module)
15. [Layer 11 — Vendor Module (Second Domain Module)](#15-layer-11--vendor-module)
16. [Layer 12 — Customer Module (Third Domain Module)](#16-layer-12--customer-module)
17. [Layer 13 — Contract Module (Fourth Domain Module)](#17-layer-13--contract-module)
18. [Layer 14 — Partner Module (Fifth Domain Module)](#18-layer-14--partner-module)
19. [Layer 15 — Payment Module (Sixth Domain Module)](#19-layer-15--payment-module)
20. [Layer 16 — Capital Module (Seventh Domain Module)](#20-layer-16--capital-module)
21. [Data Flow: Full Request Lifecycle](#21-data-flow-full-request-lifecycle)
22. [What's Next](#22-whats-next)

---

## 1. What Is This Project?

**MaalFlow** is an installments management system. The frontend is a React Single Page Application that connects to a Spring Boot REST API.

The app manages eight business modules:

| Module | Purpose | Status |
|---|---|---|
| **Purchases** | Record items bought from vendors | ✅ Complete |
| **Vendors** | Manage vendor contacts & info | ✅ Complete |
| **Customers** | Customer profiles, linked accounts, contracts | ✅ Complete |
| **Payments** | Payment processing, statistics, discounts, cancel/refund | ✅ Complete |
| **Partners** | Partner management, investments, withdrawals, commissions | ✅ Complete |
| **Capital** | Capital pool dashboard, transactions, filters | ✅ Complete |
| **Contracts** | Installment contracts, schedules, expenses, financial tracking | ✅ Complete |

The app is **bilingual from day one** — Arabic is the default language, English is fully supported. The entire layout flips between LTR and RTL.

---

## 2. Tech Stack & Why Each Choice

| Technology | Version | Why We Chose It |
|---|---|---|
| **React** | 19 | Component model, huge ecosystem, team familiarity |
| **TypeScript** | 5.9 | Catch bugs at compile time, better IDE support, self-documenting code |
| **Vite** | 7 | Instant dev server startup (ESM-native), fast HMR, built-in proxy for backend |
| **MUI** | 7 | Pre-built accessible components (icons, etc.), RTL support out of the box |
| **Axios** | 1.13 | Interceptors for auth/error handling, better API than fetch for complex cases |
| **react-i18next** | 16 | Industry standard for React i18n, lazy namespace loading, localStorage detection |
| **react-router-dom** | 7 | File-based-like routing, lazy loading support, nested layouts |
| **react-hook-form** | 7 | Performant forms (no re-render per keystroke), integrates with Zod |
| **Zod** | 4 | Schema validation that generates TypeScript types |
| **Zustand** | 5 | Simple state management, no boilerplate vs Redux |
| **react-toastify** | 11 | Toast notifications for API success/error feedback |
| **date-fns** | 4 | Lightweight date formatting (vs Moment.js) |

---

## 3. Project File Tree

```
frontend/
├── .env.development          # Dev environment variables
├── .env.production           # Prod environment variables
├── vite.config.ts            # Vite: aliases, proxy, build optimization
├── tsconfig.json             # Root TS config: strict mode, path aliases
├── tsconfig.app.json         # App source TS config
├── tsconfig.node.json        # Vite config TS config
│
├── public/
│   ├── logo.png
│   └── locales/              # Translation JSON files
│       ├── en/               #   English translations
│       │   ├── common.json   #     Shared UI strings
│       │   ├── purchase.json #     Purchase module strings
│       │   ├── vendor.json   #     Vendor module strings
│       │   ├── customer.json #     Customer module strings
│       │   ├── contract.json #     Contract module strings (schedules, expenses, financial)
│       │   ├── partner.json  #     Partner module strings (investments, withdrawals)
│       │   ├── payment.json #     Payment module strings (statistics, methods)
│       │   ├── capital.json #     Capital module strings (pool, transactions)
│       │   ├── validation.json #   Form validation messages
│       │   └── ...           #     (one file per namespace)
│       └── ar/               #   Arabic translations (same structure)
│           ├── common.json
│           └── ...
│
└── src/
    ├── main.tsx              # App entry point
    ├── App.tsx               # Root component (providers + router)
    │
    ├── config/               # ⚙️ CONFIGURATION
    │   ├── env.config.ts     #   Type-safe ENV wrapper
    │   ├── i18n.config.ts    #   i18next setup (languages, namespaces)
    │   └── index.ts          #   Barrel export
    │
    ├── styles/               # 🎨 STYLING
    │   ├── variables.css     #   Design tokens (colors, spacing, RTL vars)
    │   ├── globals.css       #   Utility classes (flex, grid, text)
    │   └── index.css         #   CSS reset + base body styles
    │
    ├── services/             # 🌐 HTTP CLIENT
    │   └── api/
    │       ├── client/
    │       │   ├── axios.config.ts   # Axios instance (baseURL, timeout)
    │       │   └── interceptors.ts   # JWT auth, error handling, logging
    │       ├── modules/
    │       │   ├── purchase.api.ts   # Purchase CRUD methods
    │       │   ├── vendor.api.ts     # Vendor CRUD methods
    │       │   ├── customer.api.ts   # Customer CRUD + linked accounts + stats
    │       │   ├── contract.api.ts   # Contract CRUD + status filter + complete
    │       │   ├── contractExpense.api.ts     # Expense CRUD per contract
    │       │   ├── installmentSchedule.api.ts # Generate, reschedule, queries
    │       │   ├── partner.api.ts    # Partner CRUD + status filter
    │       │   ├── partnerInvestment.api.ts   # Investment create, confirm, getByPartner
    │       │   ├── partnerWithdrawal.api.ts   # Withdrawal create, approve, process
    │       │   ├── partnerCommission.api.ts   # Commission create, approve, pay
    │       │   └── payment.api.ts    # Payment CRUD + statistics + cancel/refund + discounts
    │       │   └── capital.api.ts    # CapitalPool + CapitalTransaction APIs
    │       └── index.ts              # Typed get/post/put/patch/del helpers
    │
    ├── types/                # 📐 TYPE DEFINITIONS
    │   ├── common.types.ts   #   ApiResponse, PaginatedResponse, etc.
    │   └── modules/
    │       ├── purchase.types.ts  # Purchase, PurchaseRequest, PurchaseFilters
    │       ├── vendor.types.ts    # Vendor, VendorDetails, VendorRequest
    │       ├── customer.types.ts  # Customer, CustomerDetails, CustomerAccountLink, etc.
    │       ├── contract.types.ts  # Contract, ContractExpense, InstallmentSchedule, 5 enums
    │       ├── partner.types.ts   # Partner, Investment, Withdrawal, Commission, 10 enums
    │       ├── payment.types.ts   # Payment, PaymentSummary, Statistics, DiscountConfig, 5 enums
    │       └── capital.types.ts   # CapitalPool, CapitalTransaction, 3 enums
    │
    ├── hooks/                # 🪝 CUSTOM HOOKS
    │   ├── common/
    │   │   ├── usePagination.ts    # Page/size state management
    │   │   ├── useDebounce.ts      # Debounce a value
    │   │   ├── useLocalStorage.ts  # Persist state to localStorage
    │   │   ├── useToggle.ts        # Boolean toggle
    │   │   └── index.ts            # Barrel export
    │   └── modules/
    │       ├── usePurchases.ts     # Fetch purchase list
    │       ├── usePurchase.ts      # Fetch single purchase
    │       ├── usePurchaseCreate/Update/Delete.ts
    │       ├── useVendors.ts       # Fetch vendor list
    │       ├── useVendor.ts        # Fetch single vendor
    │       ├── useVendorCreate/Update/Delete.ts
    │       ├── useCustomers.ts     # Fetch customer list
    │       ├── useCustomer.ts      # Fetch single customer + linked accounts
    │       ├── useCustomerCreate/Update/Delete.ts
    │       ├── useContracts.ts     # Fetch contracts by status (paginated)
    │       ├── useContract.ts      # Fetch single contract + schedules + expenses
    │       ├── useContractCreate/Update.ts
    │       ├── useContractComplete.ts  # Mark contract as completed
    │       ├── useInstallmentSchedules.ts   # Fetch schedules for a contract
    │       ├── useInstallmentActions.ts     # Generate, swap, reschedule, skip
    │       ├── useContractExpenses.ts       # Fetch expenses for a contract
    │       ├── useContractExpenseActions.ts # Create, update, delete expenses
    │       ├── usePartners.ts      # Fetch partner list (optional status filter)
    │       ├── usePartner.ts       # Fetch single partner + investments + withdrawals
    │       ├── usePartnerCreate/Update/Delete.ts
    │       ├── usePayments.ts      # Paginated payment list (optional month filter)
    │       ├── usePaymentDetail.ts # Single payment by ID
    │       ├── usePaymentCreate.ts # Process new payment
    │       ├── usePaymentActions.ts# Cancel + Refund actions
    │       ├── usePaymentStatistics.ts # Monthly statistics
    │       ├── useCapitalPool.ts       # Current pool status
    │       ├── useCapitalPoolActions.ts # Create, update, recalculate pool
    │       ├── useCapitalTransactions.ts # Paginated transaction list + type filter
    │       ├── useCapitalTransactionCreate.ts # Record new transaction
    │       └── index.ts            # Barrel export
    │
    ├── utils/
    │   ├── validators/
    │   │   ├── purchase.validator.ts  # Zod schema for purchases
    │   │   ├── vendor.validator.ts    # Zod schema for vendors
    │   │   ├── customer.validator.ts  # Zod schema for customers
    │   │   ├── contract.validator.ts  # Zod schemas for contracts + expenses
    │   │   ├── partner.validator.ts   # Zod schema for partners
    │   │   ├── payment.validator.ts   # Zod schema for payments
    │   │   └── capital.validator.ts   # Zod schemas for pool + transactions
    │   ├── helpers/
    │   │   ├── format.helper.ts   # formatCurrency
    │   │   ├── date.helper.ts     # formatDate, formatDateTime
    │   │   └── index.ts
    │   └── constants/
    │       ├── endpoints.ts
    │       └── routes.ts
    │
    ├── router/               # 🧭 ROUTING
    │   ├── routes.config.ts  #   APP_ROUTES + ROUTE_HELPERS constants
    │   ├── index.tsx         #   createBrowserRouter with lazy loading
    │   └── ProtectedRoute.tsx #  (stub — auth coming later)
    │
    ├── components/           # 🧱 COMPONENTS
    │   ├── layout/
    │   │   ├── Header/       #   Top bar: logo, language switch, user menu
    │   │   ├── Sidebar/      #   Side nav: 8 module links, collapsible
    │   │   ├── MainLayout/   #   Shell: Header + Sidebar + <Outlet />
    │   │   └── index.ts
    │   ├── common/
    │   │   ├── Button/       #   Variants, sizes, loading state
    │   │   ├── Input/        #   Label, error, RTL, forwardRef
    │   │   ├── Modal/        #   Portal, ESC close, scroll lock
    │   │   ├── Table/        #   Generic, skeleton rows, empty state
    │   │   └── index.ts
    │   └── ui/
    │       ├── Card/         #   Header/footer, clickable
    │       ├── LoadingSpinner/ # Sizes, inherit color
    │       └── index.ts
    │
    ├── contexts/             # 🔄 REACT CONTEXTS
    │   ├── AuthContext.tsx    #   User state, login/logout
    │   └── LanguageContext.tsx #  Language + direction state
    │
    └── pages/                # 📄 PAGE COMPONENTS
        ├── Dashboard/
        ├── auth/
        ├── NotFoundPage.tsx
        └── modules/installments/
            ├── purchase/     #   List, Form, Details + Create/Edit/View wrappers (✅)
            ├── vendor/       #   List, Form, Details + Create/Edit/View wrappers (✅)
            ├── customer/     #   List, Form, Details + Create/Edit/View wrappers (✅)
            ├── contract/     #   List, Form, Details + Schedules + Expenses (✅)
            ├── payment/      #   List, Form, Details + cancel/refund + statistics (✅)
            ├── partner/      #   List, Form, Details + investments + withdrawals (✅)
            └── capital/      #   Pool dashboard + transactions + forms (✅)
```

---

## 4. High-Level Architecture

### How the pieces connect

```
┌─────────────────────────────────────────────────────────────────┐
│                         BROWSER                                 │
│                                                                 │
│  ┌──────────┐   ┌───────────┐   ┌──────────────────────────┐   │
│  │  Header   │   │  Sidebar  │   │     Page Component       │   │
│  │ (lang,    │   │ (nav      │   │   (e.g. PurchaseList)    │   │
│  │  user)    │   │  links)   │   │                          │   │
│  └──────────┘   └───────────┘   │  ┌────────────────────┐  │   │
│                                  │  │  UI Components     │  │   │
│  ◄──── MainLayout ──────────►    │  │  (Table, Button,   │  │   │
│                                  │  │   Modal, Input)    │  │   │
│                                  │  └────────┬───────────┘  │   │
│                                  │           │              │   │
│                                  │  ┌────────▼───────────┐  │   │
│                                  │  │  Custom Hooks      │  │   │
│                                  │  │  (usePagination,   │  │   │
│                                  │  │   useDebounce)     │  │   │
│                                  │  └────────┬───────────┘  │   │
│                                  └───────────┼──────────────┘   │
│                                              │                  │
│                                   ┌──────────▼───────────┐      │
│                                   │  API Service Layer   │      │
│                                   │  (purchaseApi.getAll) │     │
│                                   └──────────┬───────────┘      │
│                                              │                  │
│                                   ┌──────────▼───────────┐      │
│                                   │  api.get<T>()        │      │
│                                   │  (typed helpers)     │      │
│                                   └──────────┬───────────┘      │
│                                              │                  │
│                                   ┌──────────▼───────────┐      │
│                                   │  Axios Interceptors  │      │
│                                   │  + JWT token         │      │
│                                   │  + Accept-Language   │      │
│                                   │  + Error handling    │      │
│                                   └──────────┬───────────┘      │
│                                              │                  │
└──────────────────────────────────────────────┼──────────────────┘
                                               │
                                    ┌──────────▼───────────┐
                                    │  Vite Dev Proxy      │
                                    │  /api → :8080        │
                                    └──────────┬───────────┘
                                               │
                                    ┌──────────▼───────────┐
                                    │  Spring Boot :8080   │
                                    │  REST API            │
                                    └──────────────────────┘
```

### Why this layered architecture?

Each layer has **one job** and knows nothing about the layers above it:

| Layer | Knows About | Doesn't Know About |
|---|---|---|
| **Page** | Hooks, API service, UI components | How HTTP works, where tokens are stored |
| **Hooks** | API service, React state | Which page is using them |
| **API Service** | `api.get/post` helpers, TypeScript types | Axios config, interceptors |
| **api helpers** | Axios instance | JWT tokens, error toasts |
| **Interceptors** | Axios, localStorage, toast | Which endpoint is being called |

**Why?** When you change how authentication works (e.g., switching from localStorage to httpOnly cookies), you only touch `interceptors.ts`. No page, hook, or API service file changes.

---

## 5. Layer 1 — Configuration

### 5.1 Environment Variables (`.env.*`)

```env
# .env.development
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_APP_NAME=MaalFlow
VITE_APP_VERSION=0.0.0
VITE_DEFAULT_LANGUAGE=ar
VITE_ENABLE_DEBUG=true
VITE_DEFAULT_PAGE_SIZE=10
```

**Why the `VITE_` prefix?** Vite only exposes env vars starting with `VITE_` to the browser bundle for security. Without the prefix, secrets would leak into the JavaScript bundle.

**Why two files?**
- `.env.development` → used by `vite` (dev server)
- `.env.production` → used by `vite build`
- `.env.local` (git-ignored) → personal overrides that never get committed

### 5.2 Type-Safe ENV Wrapper (`env.config.ts`)

```
.env file → import.meta.env → getEnvVar() → ENV object → rest of app
```

**The problem it solves:** Without this, every file that needs the API URL would write `import.meta.env.VITE_API_BASE_URL` — a raw string with no type safety, no validation, and if you typo it, you get `undefined` at runtime with no error message.

**How it works:**
1. `getEnvVar(key, fallback?)` reads from `import.meta.env`
2. If the value is missing and no fallback exists → throws a clear error at startup
3. The exported `ENV` object has typed properties (`boolean` for debug, `number` for page size)

```typescript
// ❌ Without env.config — typo silently returns undefined
const url = import.meta.env.VITE_API_BASE_ULR  // undefined, no error

// ✅ With env.config — typo caught at compile time
import { ENV } from '@config/env.config'
const url = ENV.API_BASE_ULR  // TS error: Property 'API_BASE_ULR' does not exist
```

### 5.3 Vite Configuration (`vite.config.ts`)

Three key things configured:

#### Path Aliases
```typescript
'@components': path.resolve(__dirname, './src/components')
```
**Why?** Without aliases, deep imports look like this:
```typescript
// ❌ Fragile relative paths — break when you move the file
import Button from '../../../components/common/Button'

// ✅ Alias — works from any depth
import Button from '@components/common/Button'
```
The aliases are defined in **both** `vite.config.ts` (for the bundler) and `tsconfig.app.json` (for TypeScript's type checker). They must match.

#### Dev Server Proxy
```typescript
proxy: {
    '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
    },
}
```
**Why?** The browser enforces CORS — the frontend on `localhost:3000` can't call the backend on `localhost:8080` directly. The proxy makes the browser think the API is on the same origin.

```
Browser → localhost:3000/api/purchases → Vite proxy → localhost:8080/api/purchases
```

#### Build Optimization — Manual Chunks
```typescript
manualChunks: {
    vendor: ['react', 'react-dom', 'react-router-dom'],
    mui:    ['@mui/material', '@mui/icons-material'],
    i18n:   ['i18next', 'react-i18next'],
}
```
**Why?** Without this, Vite puts everything into one big JavaScript file. With manual chunks, the libraries are split into separate files that the browser caches independently. When you update your app code, users only re-download your code — not React, MUI, or i18next.

---

## 6. Layer 2 — Styling & RTL System

### 6.1 Three CSS Files, Three Jobs

| File | Job | Analogy |
|---|---|---|
| `variables.css` | Define design tokens (colors, spacing, fonts) | A design system's "dictionary" |
| `globals.css` | Reusable utility classes (`.flex`, `.grid`, `.gap-4`) | A mini Tailwind |
| `index.css` | CSS reset + body styles; imports the other two | The "main" entry point |

**Why not one file?** At 500+ lines combined, a single file becomes hard to navigate. With separation:
- Need to change the primary color? → `variables.css`
- Need a new utility class? → `globals.css`
- Need to change the base font? → `index.css`

### 6.2 RTL Support Strategy

Arabic is written right-to-left. The entire layout must flip:

```
LTR (English):                    RTL (Arabic):
┌────────┬─────────────┐          ┌─────────────┬────────┐
│Sidebar │  Content    │          │   Content   │Sidebar │
│        │             │          │             │        │
└────────┴─────────────┘          └─────────────┴────────┘
```

**How we handle it:**

1. **CSS Logical Properties** — Instead of `margin-left`, we use `margin-inline-start`. The browser automatically flips this in RTL mode:

```css
/* ❌ Physical — doesn't flip */
.sidebar { border-right: 1px solid gray; }

/* ✅ Logical — flips automatically */
.sidebar { border-inline-end: 1px solid gray; }
```

2. **`[dir="rtl"]` attribute** — Set on `<html>` by `LanguageContext.tsx` when the user switches to Arabic. CSS variables update automatically:

```css
:root {
    --direction: ltr;
    --start: left;
    --end: right;
}

[dir="rtl"] {
    --direction: rtl;
    --start: right;
    --end: left;
}
```

3. **Arabic Font Family** — The body gets a different font in RTL:
```css
body { font-family: var(--font-family-base); }         /* Inter for English */
[dir="rtl"] body { font-family: var(--font-family-arabic); }  /* Cairo for Arabic */
```

---

## 7. Layer 3 — Internationalization (i18n)

### How translation works

```
User clicks "EN" button
       │
       ▼
LanguageContext.changeLanguage('en')
       │
       ├──► i18n.changeLanguage('en')     → loads /locales/en/common.json
       ├──► document.documentElement.dir = 'ltr'  → CSS flips layout
       ├──► document.documentElement.lang = 'en'
       └──► localStorage.setItem('language', 'en') → remembered on reload
```

### Namespace System

Translations are split into **9 namespace files** per language:

```
/public/locales/en/common.json      ← shared strings (Save, Cancel, nav labels)
/public/locales/en/purchase.json    ← purchase-specific strings
/public/locales/en/vendor.json      ← vendor-specific strings
/public/locales/en/customer.json    ← customer-specific strings
/public/locales/en/payment.json     ← payment-specific strings
/public/locales/en/validation.json  ← form validation messages
...
```

**Why namespaces?**
- **Lazy loading** — i18next only downloads the JSON file when a page needs it. The purchase page loads `purchase.json` on demand, not upfront.
- **Team isolation** — two developers can work on `purchase.json` and `payment.json` without merge conflicts.
- **Smaller files** — easier to send to translators.

### Usage in Components

```tsx
import { useTranslation } from 'react-i18next'

function PurchaseListPage() {
    // 't' reads from the 'purchase' namespace
    const { t } = useTranslation('purchase')
    // 'tc' reads from the 'common' namespace
    const { t: tc } = useTranslation('common')

    return <h1>{t('title')}</h1>
    // English: "Purchases"
    // Arabic:  "المشتريات"
}
```

---

## 8. Layer 4 — HTTP Client & API Layer

### Why three files?

```
axios.config.ts ──► interceptors.ts ──► index.ts ──► purchase.api.ts
    (create)           (attach)          (wrap)        (use)
```

| File | Single Responsibility |
|---|---|
| `axios.config.ts` | Create the Axios instance (base URL, timeout, headers) |
| `interceptors.ts` | Attach auth token, language header, error handling |
| `index.ts` | Export typed `api.get<T>()` helpers that unwrap `AxiosResponse` |

### 8.1 Axios Instance (`axios.config.ts`)

```typescript
const apiClient = axios.create({
    baseURL: ENV.API_BASE_URL,    // "http://localhost:8080/api"
    timeout: 15_000,               // 15 seconds
    headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
    },
})
```

**Why 15 seconds?** Long enough for complex queries (reports with joins), short enough to fail fast when the backend is down. Without a timeout, the UI would hang indefinitely.

### 8.2 Interceptors (`interceptors.ts`)

**Request interceptor** — runs before every outgoing request:

```
Every API call
     │
     ▼
┌─ Request Interceptor ──────────────────────────────┐
│  1. Read JWT token from localStorage               │
│     → Set Authorization: Bearer <token>             │
│                                                     │
│  2. Read language from localStorage                 │
│     → Set Accept-Language: ar                       │
│     (backend returns Arabic validation messages)    │
│                                                     │
│  3. If dev mode → log method + URL to console       │
└─────────────────────────────────────────────────────┘
     │
     ▼
  Spring Boot
```

**Response interceptor** — runs on every response:

```
Spring Boot responds
     │
     ▼
┌─ Response Interceptor ─────────────────────────────┐
│                                                     │
│  200-299 → log in dev mode, pass through            │
│                                                     │
│  401 → Token expired/invalid                        │
│        • Remove token from localStorage             │
│        • Save current URL for redirect after login  │
│        • Navigate to /login                         │
│        • Show toast: "Session expired"              │
│                                                     │
│  403 → Forbidden                                    │
│        • Show toast: "No permission"                │
│        (user is logged in but lacks the role)       │
│                                                     │
│  400/422 → Validation error                         │
│        • Join errors[] into readable message        │
│        • Show toast with field-level errors          │
│                                                     │
│  500 → Server error                                 │
│        • Show toast: "Unexpected error"             │
│                                                     │
│  Network error → No response at all                 │
│        • Show toast: "Check your connection"        │
│                                                     │
│  ALL errors → Promise.reject(error)                 │
│  (so the calling code can still .catch())           │
└─────────────────────────────────────────────────────┘
```

**Why handle errors here instead of in each component?** Because 401/403/500 handling is **the same everywhere**. Without interceptors, every API call would need its own try/catch with the same toast logic — duplicated 50+ times across the app.

### 8.3 Typed API Helpers (`index.ts`)

```typescript
// ❌ Without helpers — verbose, AxiosResponse wrapper everywhere
const response = await apiClient.get<Purchase>('/purchases/1')
const purchase = response.data  // must unwrap .data every time

// ✅ With helpers — clean, returns T directly
const purchase = await api.get<Purchase>('/purchases/1')
```

The helpers (`get`, `post`, `put`, `patch`, `del`) do one thing: call Axios and return `response.data`. This means every call-site gets the typed payload directly.

**Why `del` not `delete`?** `delete` is a JavaScript reserved keyword. You can't write `function delete()`.

---

## 9. Layer 5 — TypeScript Types

### Common Types (`common.types.ts`)

These mirror the shapes your Spring Boot backend returns:

```typescript
// What the backend wraps every response in:
interface ApiResponse<T> {
    success: boolean
    message: string
    data: T
    errors?: Record<string, string>  // field → error message
}

// What Spring Data's Page<T> serializes to:
interface PaginatedResponse<T> {
    content: T[]           // the rows on this page
    totalElements: number  // total across all pages
    totalPages: number
    number: number         // current page (zero-based!)
    size: number           // page size
    first: boolean
    last: boolean
    empty: boolean
}

// What you send as query params to a paginated endpoint:
interface PaginationParams {
    page?: number    // zero-based (Spring convention)
    size?: number    // defaults to ENV.DEFAULT_PAGE_SIZE
    sort?: string    // "createdAt,desc"
}
```

**Why `number` (zero-based) for the page?** Because Spring Data uses zero-based pages. Page 0 is the first page. Using 1-based on the frontend would mean converting back and forth everywhere — error-prone.

### Module Types (`purchase.types.ts`)

```typescript
// Java BigDecimal → TypeScript number
// Java LocalDate  → TypeScript string ("YYYY-MM-DD")
// Java Long       → TypeScript number

interface Purchase {           // ← maps to PurchaseResponse DTO
    id: number
    productName: string
    buyPrice: number           // BigDecimal → number
    purchaseDate: string       // LocalDate → "2026-02-26"
    createdAt: string          // LocalDateTime → "2026-02-26T14:30:00"
    notes: string | null
    vendorName: string
}

interface PurchaseRequest {    // ← maps to PurchaseRequest DTO
    vendorId: number
    productName: string
    buyPrice: number
    purchaseDate: string
    notes?: string             // optional (Java: can be null)
}

interface PurchaseFilters extends PaginationParams {
    vendorId?: number
    startDate?: string
    endDate?: string
    searchTerm?: string
}
```

**Why `string` for dates, not `Date`?** Because JSON has no Date type. The backend sends `"2026-02-26"` as a string. Keeping it as `string` avoids constant `new Date()` / `.toISOString()` conversions. When you need to display or manipulate dates, use `date-fns`.

---

## 10. Layer 6 — Custom Hooks

### Why custom hooks?

Hooks extract **reusable stateful logic** out of components. Without them, every list page would duplicate pagination state management, every search input would duplicate debounce logic.

| Hook | What It Does | Used By |
|---|---|---|
| `usePagination()` | Manages `page` and `size` state with `nextPage`, `prevPage`, `goToPage`, `setSize` | Every list page |
| `useDebounce(value, delay)` | Returns a debounced value that only updates after the user stops typing for `delay` ms | Search inputs |
| `useLocalStorage(key, initial)` | Like `useState` but persisted to localStorage. Syncs across tabs. | Theme, preferences |
| `useToggle(initial?)` | Returns `[value, toggle, setTrue, setFalse]` | Modals, drawers, dropdowns |

### Module Hooks Pattern (5 hooks per module)

Every completed module has exactly 5 hooks:

| Hook | Purpose | Example |
|---|---|---|
| `use{Module}s` | Fetch paginated list, auto-refetch on filter change | `useCustomers(filters)` |
| `use{Module}` | Fetch single record by ID | `useCustomer(42)` |
| `use{Module}Create` | POST + success/error toast | `useCustomerCreate()` |
| `use{Module}Update` | PUT/PATCH + success/error toast | `useCustomerUpdate()` |
| `use{Module}Delete` | Confirm + DELETE + toast | `useCustomerDelete()` |

### usePagination — Deep Dive

```typescript
const { page, size, nextPage, prevPage, setSize } = usePagination()
// page starts at 0 (Spring Data convention)
// size defaults to ENV.DEFAULT_PAGE_SIZE (from .env → 10)

// When user changes page size from 10 to 25:
setSize(25)
// → size becomes 25
// → page resets to 0 (because page 5 of size-10 doesn't exist at size-25)
```

### useDebounce — Why It Exists

```typescript
const [search, setSearch] = useState('')
const debouncedSearch = useDebounce(search, 400)

useEffect(() => {
    // Without debounce: fires on EVERY keystroke → 10 API calls for "laptop"
    // With debounce:    fires once, 400ms after the user stops typing
    fetchResults(debouncedSearch)
}, [debouncedSearch])
```

---

## 11. Layer 7 — Router & Navigation

### Route Constants (`routes.config.ts`)

```typescript
export const APP_ROUTES = {
    DASHBOARD: '/',
    PURCHASES: {
        LIST:         '/purchases',
        CREATE:       '/purchases/create',
        EDIT_PATTERN: '/purchases/:id/edit',
        VIEW_PATTERN: '/purchases/:id',
    },
    VENDORS: {
        LIST:         '/vendors',
        CREATE:       '/vendors/create',
        EDIT_PATTERN: '/vendors/:id/edit',
        VIEW_PATTERN: '/vendors/:id',
    },
    CUSTOMERS: {
        LIST:         '/customers',
        CREATE:       '/customers/create',
        EDIT_PATTERN: '/customers/:id/edit',
        VIEW_PATTERN: '/customers/:id',
    },
    // ... same pattern for remaining modules
}

export const ROUTE_HELPERS = {
    PURCHASE_EDIT: (id: number) => `/purchases/${id}/edit`,
    PURCHASE_VIEW: (id: number) => `/purchases/${id}`,
    VENDOR_EDIT:   (id: number) => `/vendors/${id}/edit`,
    VENDOR_VIEW:   (id: number) => `/vendors/${id}`,
    CUSTOMER_EDIT: (id: number) => `/customers/${id}/edit`,
    CUSTOMER_VIEW: (id: number) => `/customers/${id}`,
    // ...
}
```

**Why two exports?**
- `APP_ROUTES.PURCHASES.EDIT_PATTERN` = `/purchases/:id/edit` — used in the router definition (React Router needs the `:id` placeholder)
- `ROUTE_HELPERS.PURCHASE_EDIT(42)` = `/purchases/42/edit` — used in `navigate()` calls and `<Link>` components (need the actual ID)

### Lazy Loading (`router/index.tsx`)

```typescript
const purchaseList = () => import('@pages/modules/installments/purchase/PurchaseListPage')

{
    path: APP_ROUTES.PURCHASES.LIST,
    element: lazyLoad(purchaseList),  // only downloaded when user navigates here
}
```

**Why lazy loading?** Without it, the browser downloads ALL page code upfront — even pages the user may never visit. With lazy loading:
- Initial load: only the layout shell + whatever page the user lands on
- Each new page: downloaded on demand (typically < 10KB per page)
- Result: faster initial page load

### Route Structure

```
/login          ← no layout wrapper (full-page form)
/register       ← no layout wrapper

/               ← MainLayout wraps everything below
├── /                     Dashboard
├── /purchases            PurchaseListPage
├── /purchases/create     PurchaseCreatePage
├── /purchases/:id/edit   PurchaseEditPage
├── /purchases/:id        PurchaseViewPage
├── /vendors              VendorListPage
├── /vendors/create       VendorCreatePage
├── /vendors/:id/edit     VendorEditPage
├── /vendors/:id          VendorViewPage
├── /customers            CustomerListPage
├── /customers/create     CustomerCreatePage
├── /customers/:id/edit   CustomerEditPage
├── /customers/:id        CustomerViewPage
├── /payments             ...
├── /partners             ...
├── /capital              ...
├── /contracts            ...
└── *                     NotFoundPage (404)
```

**Why are auth routes outside MainLayout?** Login and register pages shouldn't show the sidebar and header. By placing them outside the layout wrapper route, they render as full-page forms.

---

## 12. Layer 8 — Layout Components

### MainLayout — The Shell

```
┌──────────────────────────────────────────────┐
│                  Header                       │
│  [☰] [Logo MaalFlow]          [EN][AR] [👤]  │
├──────────┬───────────────────────────────────┤
│          │                                   │
│ Sidebar  │         <Outlet />                │
│          │                                   │
│ Dashboard│   (current page renders here)     │
│ Purchases│                                   │
│ Vendors  │                                   │
│ Payments │                                   │
│ Partners │                                   │
│ Capital  │                                   │
│ Customers│                                   │
│ Contracts│                                   │
│          │                                   │
└──────────┴───────────────────────────────────┘
```

**How it works:**
1. `MainLayout` renders `<Header />` + `<Sidebar />` + `<Outlet />`
2. React Router replaces `<Outlet />` with the matched page component
3. When you navigate from `/purchases` to `/customers`, only the `<Outlet />` content changes — the Header and Sidebar stay mounted (no re-render, no flicker)

### Sidebar — Mobile Behaviour

```
Desktop (> 768px):          Mobile (≤ 768px):

┌────────┬──────────┐       ┌──────────────────┐
│Sidebar │ Content  │       │    Content       │
│(always │          │       │                  │  ← sidebar hidden
│visible)│          │       │                  │
└────────┴──────────┘       └──────────────────┘

                            User taps ☰:

                            ┌────────┬─────────┐
                            │Sidebar │ overlay  │  ← slides in
                            │        │ (dark)   │  ← tap to close
                            └────────┴─────────┘
```

The sidebar uses CSS `transform: translateX(-100%)` to hide off-screen on mobile, and `translateX(0)` when `isOpen` is true. In RTL mode, the transform direction flips to `translateX(100%)`.

---

## 13. Layer 9 — Reusable UI Components

### Component Summary

| Component | Key Props | Special Features |
|---|---|---|
| **Button** | `variant` (primary/secondary/danger), `size` (sm/md/lg), `loading` | Shows spinner when loading, disables click |
| **Input** | `label`, `name`, `error`, `required` | RTL-safe text alignment, `forwardRef` for react-hook-form |
| **Modal** | `isOpen`, `onClose`, `title`, `footer` | Portal to `<body>`, ESC key close, backdrop click, scroll lock |
| **Table** | `columns`, `data`, `loading`, `emptyMessage` | Generic `<T>`, skeleton shimmer rows, empty state |
| **Card** | `title`, `footer`, `onClick` | Optional header/footer, clickable with keyboard support |
| **LoadingSpinner** | `size`, `inheritColor`, `fullPage` | CSS-only animation, `inheritColor` for use inside buttons |

### Table — Generic TypeScript

```typescript
// The Table component uses TypeScript generics:
interface TableColumn<T> {
    key: string
    label: string
    render?: (row: T, index: number) => ReactNode  // custom cell renderer
}

// Usage — T is inferred as Purchase:
const columns: TableColumn<Purchase>[] = [
    { key: 'id',          label: '#' },
    { key: 'productName', label: 'Product' },
    { key: 'buyPrice',    label: 'Price',
      render: (row) => `$${row.buyPrice.toFixed(2)}` },  // ← row is typed as Purchase
]

<Table columns={columns} data={purchases} loading={isLoading} />
```

**Why generics?** Without `<T>`, the `render` function's `row` parameter would be `any` — no autocomplete, no type checking. With generics, `row.buyPrice` is known to be a `number`.

### Modal — Why Portal?

```typescript
return createPortal(
    <div className="modal-backdrop">...</div>,
    document.body,  // ← renders outside the component tree
)
```

**Why?** If the modal renders inside a component that has `overflow: hidden` or `z-index` constraints, it could be clipped or hidden behind other elements. `createPortal` renders it directly into `<body>`, above everything else.

---

## 14. Layer 10 — Purchase Module

### The Full Stack

```
PurchaseListPage (component)
       │
       ▼
usePagination() → { page, size, nextPage, ... }
       │
       ▼
purchaseApi.getAll(filters)        ← src/services/api/modules/purchase.api.ts
       │
       ▼
api.get<PaginatedResponse<Purchase>>('/purchases', { params: filters })
       │
       ▼
Axios interceptors (add JWT, language)
       │
       ▼
GET http://localhost:8080/api/purchases?page=0&size=10&sort=purchaseDate,desc
       │
       ▼
Spring Boot → PurchaseController → PurchaseService → PurchaseRepository
       │
       ▼
Page<PurchaseResponse> returned as JSON
       │
       ▼
Interceptors (log, handle errors)
       │
       ▼
PaginatedResponse<Purchase> returned to component
       │
       ▼
<Table columns={columns} data={page.content} />
```

### purchaseApi Methods

| Method | HTTP | Endpoint | Request | Response |
|---|---|---|---|---|
| `getAll(filters?)` | GET | `/purchases?page=&size=&...` | query params | `PaginatedResponse<Purchase>` |
| `getById(id)` | GET | `/purchases/{id}` | — | `Purchase` |
| `create(data)` | POST | `/purchases` | `PurchaseRequest` body | `Purchase` |
| `update(id, data)` | PUT | `/purchases/{id}` | `PurchaseRequest` body | `Purchase` |
| `delete(id)` | DELETE | `/purchases/{id}` | — | `void` |
| `getStatistics()` | GET | `/purchases/statistics` | — | `PurchaseStatistics` |

### Cross-module connection: Vendor dropdown

The Purchase form needs a vendor dropdown. Instead of mock data, it fetches real vendors:

```typescript
const { vendors, loading: vendorsLoading } = useVendors({ size: 100 })
```

---

## 15. Layer 11 — Vendor Module

The Vendor module follows the **exact same pattern** as Purchase.

### Files Created

```
src/types/modules/vendor.types.ts           → Vendor, VendorDetails, VendorRequest, VendorFilters
src/services/api/modules/vendor.api.ts      → getAll, getById, create, update (PATCH), delete
src/hooks/modules/useVendors.ts             → paginated list
src/hooks/modules/useVendor.ts              → single by ID (returns VendorDetails)
src/hooks/modules/useVendorCreate.ts        → POST + toast
src/hooks/modules/useVendorUpdate.ts        → PATCH + toast
src/hooks/modules/useVendorDelete.ts        → confirm + DELETE + toast
src/utils/validators/vendor.validator.ts    → Zod schema (name, phone regex, address, notes)
src/pages/modules/installments/vendor/      → List, Form, Details + wrappers + CSS
public/locales/en/vendor.json               → English translations
public/locales/ar/vendor.json               → Arabic translations
```

### Key Differences from Purchase

| Aspect | Purchase | Vendor |
|--------|----------|--------|
| **HTTP update method** | `PUT` (full replace) | `PATCH` (partial update) |
| **Response types** | Single `Purchase` | `Vendor` (list) + `VendorDetails` (single with purchases) |
| **Detail page content** | Purchase info + payment placeholder | Vendor info + purchases table |

---

## 16. Layer 12 — Customer Module

The Customer module is the most feature-rich module so far.

### Backend Endpoints

```
GET    /api/v1/customers                           → Page<CustomerSummary>
GET    /api/v1/customers/{id}                      → CustomerResponse
POST   /api/v1/customers                           → CustomerResponse
PUT    /api/v1/customers/{id}                      → CustomerResponse
DELETE /api/v1/customers/{id}                      → String (soft delete)
GET    /api/v1/customers/{id}/with-contracts       → CustomerWithContractsResponse
GET    /api/v1/customers/{id}/contracts            → Page<ContractResponse>
POST   /api/v1/customers/{cId}/link/{linkedId}     → void (link accounts)
GET    /api/v1/customers/{id}/linked-accounts      → List<CustomerAccountLinkResponse>
GET    /api/v1/customers/linked-accounts/by-relation-type → List<CustomerAccountLinkResponse>
GET    /api/v1/customers/stats/count               → Map<String, Long>
```

### Files Created

```
src/types/modules/customer.types.ts         → Customer, CustomerResponse, CustomerDetails,
                                               CustomerAccountLink, CustomerRelationshipType enum,
                                               CustomerRequest, CustomerFilters, LinkCustomerRequest,
                                               CustomerStats
src/services/api/modules/customer.api.ts    → getAll, getById, create, update (PUT), delete,
                                               getWithContracts, linkAccounts, getLinkedAccounts,
                                               getLinkedAccountsByType, getStats
src/hooks/modules/useCustomers.ts           → paginated list
src/hooks/modules/useCustomer.ts            → single by ID + linked accounts (parallel fetch)
src/hooks/modules/useCustomerCreate.ts      → POST + toast
src/hooks/modules/useCustomerUpdate.ts      → PUT + toast
src/hooks/modules/useCustomerDelete.ts      → confirm + DELETE + toast
src/utils/validators/customer.validator.ts  → Zod schema (name, phone, address, nationalId, notes)
src/pages/modules/installments/customer/    → List, Form, Details + wrappers + CSS
public/locales/en/customer.json             → English translations (99 keys)
public/locales/ar/customer.json             → Arabic translations (99 keys)
```

### Key Differences from Purchase & Vendor

| Aspect | Purchase | Vendor | Customer |
|--------|----------|--------|----------|
| **HTTP update** | `PUT` | `PATCH` | `PUT` |
| **List type** | `Purchase` | `Vendor` | `Customer` |
| **Detail type** | Same | `VendorDetails` (+ purchases) | `CustomerDetails` (+ contracts) |
| **Extra data on detail** | — | Vendor's purchases | Linked accounts (parallel fetch) |
| **Enum type** | — | — | `CustomerRelationshipType` (5 values) |
| **Extra API methods** | `getStatistics()` | — | `linkAccounts()`, `getLinkedAccounts()`, `getStats()` |
| **List page special UI** | Search | Search | Search + Active/Inactive toggle filter |
| **Validator unique fields** | vendorId, buyPrice, date | name, phone, address | nationalId (6-14 digits) |

### CustomerRelationshipType Enum

```typescript
enum CustomerRelationshipType {
    SAME_PERSON      = 'SAME_PERSON',
    FAMILY_MEMBER    = 'FAMILY_MEMBER',
    BUSINESS_PARTNER = 'BUSINESS_PARTNER',
    GUARANTOR        = 'GUARANTOR',
    OTHER            = 'OTHER',
}
```

### Customer Details Page Sections

```
┌──────────────────────────────────────────────┐
│ ← Back to Customers   Customer #42           │
│                              [Edit] [Delete]  │
├──────────────────────────────────────────────┤
│ Customer Information                          │
│ ┌─────────────┬──────────────┐               │
│ │ Name        │ Phone        │               │
│ │ Ahmed Ali   │ +964780...   │               │
│ ├─────────────┼──────────────┤               │
│ │ National ID │ Address      │               │
│ │ 12345678    │ Baghdad St   │               │
│ ├─────────────┼──────────────┤               │
│ │ Status      │ Created At   │               │
│ │ 🟢 Active   │ 28/02/2026   │               │
│ └─────────────┴──────────────┘               │
├──────────────────────────────────────────────┤
│ Notes                                         │
│ "Loyal customer since 2024"                   │
├──────────────────────────────────────────────┤
│ 🔗 Linked Accounts                           │
│ ┌──────────┬──────────┬──────┬──────┐        │
│ │ Customer │ Relation │ Desc │Status│        │
│ │ Fatima   │ Family   │ Wife │ 🟢   │        │
│ │ Omar     │Guarantor │      │ 🟢   │        │
│ └──────────┴──────────┴──────┴──────┘        │
├──────────────────────────────────────────────┤
│ 📄 Contracts                                  │
│ (No contracts recorded for this customer)     │
└──────────────────────────────────────────────┘
```

### Customer List — Active/Inactive Toggle

```
┌─────────────────────────────────────────────┐
│ Customers                    [+ Create]      │
│                                              │
│ [🔍 Search...]  [All|Active|Inactive] [Clear]│
│                                              │
│ ┌──────┬───────┬──────┬─────────┬──────────┐│
│ │ Name │ Phone │ ID   │ Address │ Actions  ││
│ ├──────┼───────┼──────┼─────────┼──────────┤│
│ │Ahmed │ +964..│ 1234 │ Baghdad │ 👁 ✏ 🗑  ││
│ └──────┴───────┴──────┴─────────┴──────────┘│
│                                              │
│ Showing 1–10 of 42   Rows: [10▼]  [◀] [▶]  │
└─────────────────────────────────────────────┘
```

The toggle filter is a 3-button group (All / Active / Inactive) that filters the displayed list.

---

## 17. Layer 13 — Contract Module (Fourth Domain Module)

The Contract module is the **most complex module** in the system — it's the core of the installments business. Unlike previous modules that have a single entity, Contract has **3 backend entities**: Contract, InstallmentSchedule, and ContractExpense.

### Why Contract is Different

| Aspect | Purchase / Vendor / Customer | Contract |
|--------|------------------------------|----------|
| **Backend entities** | 1 | 3 (Contract + InstallmentSchedule + ContractExpense) |
| **API services** | 1 file | 3 files (`contract.api.ts`, `installmentSchedule.api.ts`, `contractExpense.api.ts`) |
| **Hooks** | 5 (standard CRUD) | 10 (5 contract + 2 schedule + 2 expense + 1 complete) |
| **Enums** | 0-1 | 5 (`ContractStatus`, `PaymentStatus`, `ExpenseType`, `PaidBy`, `DeductionType`) |
| **List page filter** | Search only | Status tabs (ACTIVE/COMPLETED/LATE/CANCELLED) + search |
| **Detail page** | Info card + notes | 6 sections: info, financial summary, installment schedule, expenses, notes |
| **Delete** | Standard DELETE | Disabled — uses `markAsCompleted` instead |
| **Form dropdowns** | Vendor (1 dropdown) | Customer + Purchase (2 dropdowns, both fetched from API) |

### Backend Endpoints (3 Controllers)

**ContractController** (`/api/v1/contracts`):
```
POST   /                              → Create contract
PUT    /{id}                          → Update contract
GET    /{id}                          → Get by ID
GET    /contract-number/{number}      → Get by contract number
GET    /by-status/{status}?page&size  → Paginated by status (main list endpoint)
GET    /customer/{id}?page&size       → Customer's contracts
GET    /customer/{id}/all-linked      → All linked customer contracts
GET    /by-payment-day/{day}          → Contracts by payment day
GET    /by-address?address=           → Contracts by address
GET    /total-monthly-expected        → Sum of all monthly amounts
GET    /total-net-profit              → Sum of net profits
GET    /{id}/early-payment-discount   → Calculate early payment discount
GET    /{id}/cash-discount            → Calculate cash discount
PUT    /{id}/complete                 → Mark as completed
```

**InstallmentScheduleController** (`/api/v1/installment-schedules`):
```
POST   /generate/{contractId}         → Auto-generate schedules
POST   /generate/{contractId}/custom  → Custom generation (months/amount/remainder)
PUT    /swap-remainder/{contractId}   → Swap remainder position (first ↔ last)
DELETE /unpaid/{contractId}           → Delete all unpaid schedules
PUT    /reschedule/{contractId}       → Reschedule unpaid installments
PUT    /skip-month/{contractId}       → Skip a month (e.g., Ramadan)
POST   /                              → Create single schedule
PUT    /{id}                          → Update single schedule
GET    /contract/{contractId}         → All schedules for a contract
GET    /overdue                       → All overdue schedules
GET    /due-soon?daysAhead=5          → Schedules due within N days
GET    /by-payment-day/{day}          → By collection day
GET    /by-status/{status}?page&size  → By payment status (paginated)
GET    /monthly-summary?month=        → Monthly collection summary
```

**ContractExpenseController** (`/api/v1/contract-expenses`):
```
POST   /                              → Create expense
PUT    /{id}                          → Update expense
GET    /{id}                          → Get by ID
GET    /contract/{contractId}         → Expenses for a contract
GET    /by-type/{type}                → By expense type
GET    /date-range?start&end          → By date range
GET    /contract/{contractId}/total   → Total expenses for a contract
GET    /partner/{partnerId}/total     → Total expenses for a partner
DELETE /{id}                          → Delete expense
```

### Hook Architecture — Why 10 Hooks?

The standard module pattern uses 5 CRUD hooks. Contract breaks this because:

1. **Sub-entities are action-oriented**: InstallmentSchedule isn't simple CRUD — it has `generate`, `reschedule`, `swapRemainder`, `skipMonth`. These are grouped into `useInstallmentActions` rather than 5 separate hooks.

2. **Detail page fetches 3 things in parallel**: `useContract(id)` fetches the contract + schedules + expenses via `Promise.all`.

3. **No delete**: The backend has `deleteContract` commented out. Instead, `useContractComplete` calls `PUT /{id}/complete`.

```
Contract Hooks (5):
├── useContracts(status, page, size)  → list by status
├── useContract(id)                   → single + schedules + expenses
├── useContractCreate()               → POST
├── useContractUpdate()               → PUT
└── useContractComplete()             → PUT /{id}/complete

InstallmentSchedule Hooks (2):
├── useInstallmentSchedules(contractId) → GET /contract/{id}
└── useInstallmentActions()             → generate, generateCustom, swapRemainder,
                                          deleteUnpaid, reschedule, skipMonth

ContractExpense Hooks (2):
├── useContractExpenses(contractId)     → GET /contract/{id}
└── useContractExpenseActions()         → create, update, delete
```

### Contract Details Page — 6 Sections

```
┌──────────────────────────────────────────────────────────────┐
│ ← Back to Contracts   Contract CNT-2026-001                  │
│                                        [Edit] [Mark Complete]│
├──────────────────────────────────────────────────────────────┤
│ Contract Information                                          │
│ ┌───────────────┬───────────────┬──────────────┐             │
│ │ Contract #    │ Customer      │ Product      │             │
│ │ CNT-2026-001  │ Ahmed Ali     │ Samsung TV   │             │
│ ├───────────────┼───────────────┼──────────────┤             │
│ │ Vendor        │ Partner       │ Status       │             │
│ │ TechVendor    │ —             │ 🟢 Active    │             │
│ ├───────────────┼───────────────┼──────────────┤             │
│ │ Start Date    │ Months        │ Payment Day  │             │
│ │ 01/01/2026    │ 12            │ 15th         │             │
│ └───────────────┴───────────────┴──────────────┘             │
├──────────────────────────────────────────────────────────────┤
│ Financial Summary                                             │
│ ┌──────────┬──────────┬──────────┬──────────┐                │
│ │ Original │  Final   │  Down    │Remaining │                │
│ │ $3,000   │ $3,500   │ $500     │ $3,000   │                │
│ ├──────────┼──────────┼──────────┼──────────┤                │
│ │ Monthly  │ Profit   │ Expenses │Net Profit│                │
│ │ $250     │ 🟢 $500  │ 🔴 $50   │ 🟢 $450  │                │
│ └──────────┴──────────┴──────────┴──────────┘                │
├──────────────────────────────────────────────────────────────┤
│ 📅 Installment Schedule        [Generate] [Swap] [Del Unpaid]│
│ ┌───┬────────────┬────────┬──────┬──────────┬───────────┐    │
│ │ # │ Due Date   │ Amount │ Paid │ Status   │ Profit Mo │    │
│ ├───┼────────────┼────────┼──────┼──────────┼───────────┤    │
│ │ 1 │ 15/01/2026 │ $300   │ $300 │ 🟢 Paid  │ 2026-01   │    │
│ │ 2 │ 15/02/2026 │ $250   │ $250 │ 🟢 Paid  │ 2026-02   │    │
│ │ 3 │ 15/03/2026 │ $250   │  —   │ 🟡 Pend  │ 2026-03   │    │
│ │...│ ...        │ ...    │ ...  │ ...      │ ...       │    │
│ │12★│ 15/12/2026 │ $250   │  —   │ 🟡 Pend  │ 2026-12   │    │
│ └───┴────────────┴────────┴──────┴──────────┴───────────┘    │
├──────────────────────────────────────────────────────────────┤
│ 💰 Expenses                                                   │
│ ┌───────────┬────────┬────────┬────────┬─────────┐           │
│ │ Type      │ Amount │ Date   │Paid By │ Receipt │           │
│ │ Shipping  │ $30    │ 01/01  │ Owner  │ RC-001  │           │
│ │ Insurance │ $20    │ 01/01  │ Customer│ RC-002 │           │
│ └───────────┴────────┴────────┴────────┴─────────┘           │
├──────────────────────────────────────────────────────────────┤
│ Notes                                                         │
│ "Customer agreed to pay on the 15th of each month"           │
└──────────────────────────────────────────────────────────────┘
```

### Contract List — Status Tabs

```
┌──────────────────────────────────────────────────────────────┐
│ Contracts                                    [+ Create]       │
│                                                               │
│ [  All  | Active | Late | Completed | Cancelled ]             │
│  ^^^^^^^^                                                     │
│  (status tabs — clicking one re-fetches by that status)       │
│                                                               │
│ [🔍 Search by contract # or customer…]                [Clear] │
│                                                               │
│ ┌────────┬──────────┬─────────┬─────────┬───────┬──────────┐ │
│ │ Cont # │ Customer │ Final $ │ Monthly │ Mths  │ Status   │ │
│ ├────────┼──────────┼─────────┼─────────┼───────┼──────────┤ │
│ │ CNT-01 │ Ahmed    │ $3,500  │ $250    │ 12    │ 🟢Active │ │
│ │ CNT-02 │ Fatima   │ $5,000  │ $400    │ 12    │ 🔴Late   │ │
│ └────────┴──────────┴─────────┴─────────┴───────┴──────────┘ │
│                                                               │
│ Showing 1–10 of 25   Rows: [10▼]  [◀] [▶]                   │
└──────────────────────────────────────────────────────────────┘
```

### Zod Validation — Contract vs Expense

Two separate schemas:

**`contractCreateSchema`** — matches `ContractRequest`:
- `customerId` — required, positive
- `purchaseId` — required, positive
- `finalPrice` — required, min 100
- `downPayment` — required, min 1
- `months` — optional, 1–60
- `monthlyAmount` — optional, min 1
- `startDate` — required
- `additionalCosts` — optional, min 0
- `earlyPaymentDiscountRate` — optional, 0–100
- `agreedPaymentDay` — optional, 1–31
- `contractNumber` — optional, max 50
- `notes` — optional, max 500

**`contractExpenseSchema`** — matches `ContractExpenseRequest`:
- `expenseType` — required (enum)
- `amount` — required, min 0.01
- `description` — optional, max 255
- `expenseDate` — required
- `paidBy` — optional (enum)
- `receiptNumber` — optional, max 100
- `notes` — optional, max 500

---

## 18. Layer 14 — Partner Module (Fifth Domain Module)

The Partner module manages business partners — investors, affiliates, and distributors who participate in the installment business. Like Contract, it has **multiple backend controllers** but with a simpler detail page structure.

### Why Partner is Unique

| Aspect | Standard Module | Partner |
|--------|----------------|---------|
| **Backend controllers** | 1 | 5 (Partner + Investment + Withdrawal + Commission + CustomerAcquisition) |
| **API services** | 1 file | 4 files |
| **Enums** | 0-1 | 10 const enums |
| **List endpoint** | Paginated | Returns all (with optional status filter) |
| **Detail page** | Info + notes | Info + financial summary + investments table + withdrawals table + notes |

### Backend Endpoints (5 Controllers)

**PartnerController** (`/api/v1/partners`):
```
POST   /              → Create partner
GET    /{id}           → Get by ID
GET    /?status=       → Get all (optional status filter, returns List not Page)
PUT    /{id}           → Update partner
DELETE /{id}           → Delete partner
```

**PartnerInvestmentController** (`/api/v1/partner-investments`):
```
POST   /                      → Create investment
GET    /{partnerId}/by-partner → Get investments by partner
GET    /{id}                   → Get investment by ID
POST   /{id}/confirm           → Confirm investment
```

**PartnerWithdrawalController** (`/api/v1/partner-withdrawals`):
```
POST   /                       → Create withdrawal request
GET    /partner/{partnerId}    → Get withdrawals by partner
GET    /pending                → Get all pending withdrawals
POST   /{id}/approve           → Approve withdrawal
POST   /{id}/process           → Process withdrawal
GET    /{id}                   → Get by ID
```

**PartnerCommissionController** (`/api/v1/partner/commissions`):
```
POST   /                       → Create commission
GET    /partner/{id}?page&size → Commissions by partner (paginated)
GET    /contract/{id}          → Commissions by contract
PUT    /{id}/approve            → Approve commission
PUT    /{id}/pay                → Pay commission
```

**PartnerCustomerAcquisitionController** (`/api/v1/partner/customer-acquisitions`):
```
POST   /                       → Assign customer to partner
GET    /partner/{id}           → Get partner's customers
PUT    /transfer                → Transfer customer between partners
GET    /partner/{id}/performance → Performance metrics
PUT    /commission              → Update commission earned
```

### Key Differences from Contract Pattern

1. **No pagination**: The `GET /partners` endpoint returns a `List<PartnerResponse>` not a `Page`. So `usePartners` doesn't use pagination — it filters client-side.

2. **Status filter is server-side**: The backend accepts `?status=ACTIVE` parameter, so switching tabs triggers a new API call.

3. **Detail page fetches in parallel**: `usePartner(id)` fetches the partner + investments + withdrawals via `Promise.all` (same pattern as `useContract`).

4. **Standard delete**: Unlike Contract (which disabled delete), Partner supports full `DELETE /{id}`.

### Hook Architecture

```
Partner Hooks (5 — standard CRUD):
├── usePartners(status?)           → GET / (optional status filter)
├── usePartner(id)                 → GET /{id} + investments + withdrawals
├── usePartnerCreate()             → POST /
├── usePartnerUpdate()             → PUT /{id}
└── usePartnerDelete()             → DELETE /{id}
```

### Partner Detail Page — 5 Sections

```
┌──────────────────────────────────────────────────────────┐
│ ← Back to Partners   Partner: Ahmed Ibrahim              │
│                                       [Edit] [Delete]    │
├──────────────────────────────────────────────────────────┤
│ Partner Information                                       │
│ ┌──────────────┬──────────────┬─────────────┐            │
│ │ Name         │ Phone        │ Address     │            │
│ │ Ahmed Ibrahim│ +9647701234  │ Baghdad     │            │
│ ├──────────────┼──────────────┼─────────────┤            │
│ │ Type         │ Share %      │ Status      │            │
│ │ Investor     │ 25%          │ 🟢 Active   │            │
│ ├──────────────┼──────────────┼─────────────┤            │
│ │ Start Date   │ Profit Start │ Sharing     │            │
│ │ 01/01/2026   │ 01/2026      │ ✅ Yes      │            │
│ └──────────────┴──────────────┴─────────────┘            │
├──────────────────────────────────────────────────────────┤
│ Financial Summary                                         │
│ ┌─────────────────┬─────────────────┬──────────────────┐ │
│ │ Total Investment│ Total Withdrawals│ Current Balance  │ │
│ │ 🟢 $50,000      │ 🔴 $10,000       │ 🟢 $40,000      │ │
│ └─────────────────┴─────────────────┴──────────────────┘ │
├──────────────────────────────────────────────────────────┤
│ 💰 Investments                                            │
│ ┌────────┬──────────┬──────────┬────────────┐            │
│ │ Amount │ Type     │ Status   │ Date       │            │
│ │ $40,000│ INITIAL  │ ✅ Conf  │ 01/01/2026 │            │
│ │ $10,000│ ADDITION │ 🟡 Pend  │ 15/02/2026 │            │
│ └────────┴──────────┴──────────┴────────────┘            │
├──────────────────────────────────────────────────────────┤
│ 📤 Withdrawals                                            │
│ ┌────────┬──────────────┬──────────┬────────┐            │
│ │ Amount │ Type         │ Status   │ Reason │            │
│ │ $5,000 │ From Profit  │ ✅ Comp  │ —      │            │
│ │ $5,000 │ From Both    │ 🟡 Pend  │ Urgent │            │
│ └────────┴──────────────┴──────────┴────────┘            │
├──────────────────────────────────────────────────────────┤
│ Notes                                                     │
│ "Key investor, handles northern region customers"        │
└──────────────────────────────────────────────────────────┘
```

---

## 19. Layer 15 — Payment Module (Sixth Domain Module)

The Payment module handles **payment processing** — recording payments, tracking discounts, cancellations, refunds, and providing monthly statistics. Unlike other modules, payments are **not editable** after creation — they can only be cancelled or refunded.

### Why Payment is Unique

| Aspect | Standard Module | Payment |
|--------|----------------|---------|
| **CRUD** | Create, Read, Update, Delete | Create, Read, Cancel, Refund (no edit/delete) |
| **List endpoint** | Simple pagination | Paginated + month filter + date range + early-only filter |
| **Statistics** | None | Monthly stats, YTD, overdue summary, daily summaries |
| **Discounts** | None | Early payment, final installment, bulk, loyalty, manual |
| **Idempotency** | None | Idempotency key prevents duplicate payments |
| **Edit page** | Form | Redirects to Details page (payments can't be edited) |

### Backend Endpoints

**PaymentController** (`/api/v1/payments`):
```
POST   /                                        → Process payment (with idempotency key)
GET    /{id}                                     → Get by ID
GET    /by-key/{key}                             → Get by idempotency key
GET    /?page&size                               → List all (paginated)
GET    /month/{YYYY-MM}?page&size                → List by month
GET    /date-range?startDate&endDate&page&size   → List by date range
GET    /early-payments?page&size                 → Early payments only
PUT    /{id}/cancel?reason                       → Cancel payment
PUT    /{id}/refund?reason                       → Refund payment

GET    /calculate-discount/early?amount&paymentDate&dueDate  → Calculate early discount
GET    /calculate-discount/final?amount&isFinalInstallment   → Calculate final discount

GET    /statistics/monthly/{year}/{month}        → Monthly statistics
GET    /statistics/ytd/{year}                    → Year-to-date statistics
GET    /statistics/overdue                       → Overdue payment summary
GET    /statistics/daily/{year}/{month}          → Daily summaries for a month

GET    /discount-config/{type}                   → Get discount config by type
GET    /discount-config                          → All active discount configs
PUT    /discount-config/{id}/activate            → Activate discount config
DELETE /discount-config/{type}                   → Deactivate discount config
```

### Hook Architecture — Action-Based (not CRUD)

```
Payment Hooks (5):
├── usePayments(page, size, month?)   → Paginated list, optional month filter
├── usePaymentDetail(id)              → Single payment by ID
├── usePaymentCreate()                → POST (process payment)
├── usePaymentActions()               → cancel + refund (both with confirmation)
└── usePaymentStatistics(year, month) → Monthly statistics
```

Key differences from standard CRUD hooks:
- **No `usePaymentUpdate`** — payments aren't editable
- **No `usePaymentDelete`** — use cancel/refund instead
- **`usePaymentActions`** groups cancel + refund into one hook (action-oriented)
- **`usePaymentStatistics`** is a read-only analytics hook

### Payment List Page — Month Filter + Pagination

```
┌──────────────────────────────────────────────────────────┐
│ Payments                                  [+ Record]      │
│                                                           │
│ Filter by Month: [2026-02 ▼]                     [Clear]  │
│                                                           │
│ ┌───┬────────┬────────┬──────┬──────────┬──────┬───────┐ │
│ │ # │ Amount │  Net   │Method│ Status   │ Date │ Early │ │
│ ├───┼────────┼────────┼──────┼──────────┼──────┼───────┤ │
│ │ 1 │ $250   │ $240   │ Cash │ ✅ Done  │02/15 │  ✓   │ │
│ │ 2 │ $300   │ $300   │ Bank │ ✅ Done  │02/16 │  —   │ │
│ │ 3 │ $200   │ $200   │ Cash │ 🟡 Pend  │02/17 │  —   │ │
│ │ 4 │ $150   │ $150   │ Voda │ 🔴 Canc  │02/18 │  —   │ │
│ └───┴────────┴────────┴──────┴──────────┴──────┴───────┘ │
│                                                           │
│ Showing 1–20 of 45   Rows: [20▼]  [◀] [▶]               │
└──────────────────────────────────────────────────────────┘
```

### Payment Detail Page — View + Actions

```
┌──────────────────────────────────────────────────────────┐
│ ← Back to Payments   Payment #42                         │
│                                    [Refund] [Cancel]      │
├──────────────────────────────────────────────────────────┤
│ Payment Information                                       │
│ ┌──────────────┬──────────────┬─────────────┐            │
│ │ ID           │ Tx Key       │ Status      │            │
│ │ #42          │ PAY-A1B2C3D4 │ ✅ Completed │            │
│ ├──────────────┼──────────────┼─────────────┤            │
│ │ Method       │ Payment Date │ Agreed Mo.  │            │
│ │ Cash         │ 15/02/2026   │ 2026-02     │            │
│ ├──────────────┼──────────────┼─────────────┤            │
│ │ Early?       │ Received By  │ Collector   │            │
│ │ Yes          │ Admin        │ Ahmed       │            │
│ └──────────────┴──────────────┴─────────────┘            │
├──────────────────────────────────────────────────────────┤
│ ┌─────────────────┬─────────────────┬──────────────────┐ │
│ │   Amount        │    Discount     │   Net Amount     │ │
│ │   $250.00       │   🔴 -$10.00    │   🟢 $240.00     │ │
│ └─────────────────┴─────────────────┴──────────────────┘ │
├──────────────────────────────────────────────────────────┤
│ Notes                                                     │
│ "Early payment — 5 days before due date"                 │
└──────────────────────────────────────────────────────────┘
```

### Zod Validation

Payment validation is lighter than other modules because payments have fewer required fields:

- `idempotencyKey` — required, max 100 (unique transaction key)
- `amount` — required, min 0.01
- `paymentMethod` — required (enum)
- `actualPaymentDate` — required
- `agreedPaymentMonth` — optional, format YYYY-MM
- `discountAmount` — optional, min 0
- `notes` — optional, max 500

---

## 20. Layer 16 — Capital Module (Seventh Domain Module)

The Capital module manages the **shared capital pool** — the money that owners and partners invest and that gets allocated to contracts. It has a unique 2-controller architecture with a **dashboard + transaction** pattern.

### Why Capital is Unique

| Aspect | Standard Module | Capital |
|--------|----------------|---------|
| **Backend controllers** | 1 | 2 (CapitalPool + CapitalTransaction) |
| **API services** | 1 file | 1 file with 2 exported objects |
| **List page** | Table only | Dashboard (pool summary cards) + transaction table |
| **CRUD** | Standard CRUD | Pool: create/update/recalculate; Transactions: create + list (no edit/delete) |
| **Form page** | Single form | 2 forms switched via URL param (`?tab=transaction`) |
| **Edit page** | Separate page | Reuses same FormPage (auto-detects existing pool) |

### Backend Endpoints (2 Controllers)

**CapitalPoolController** (`/api/v1/partner/capital-pool`):
```
GET    /current      → Get current pool status (single record)
POST   /             → Create pool
PUT    /             → Update pool
POST   /recalculate  → Recalculate from transactions
GET    /history       → Pool history (paginated)
```

**CapitalTransactionController** (`/api/v1/partner/capital-transactions`):
```
POST   /                              → Create transaction
GET    /?page&size                     → List all (paginated)
GET    /type/{transactionType}         → List by type (returns List, not Page)
GET    /partner/{partnerId}            → List by partner
GET    /date-range?startDate&endDate   → List by date range
GET    /summary/monthly/{year}/{month} → Monthly summary (Map<String, BigDecimal>)
GET    /summary/partner/{id}?start&end → Partner summary
```

### Key Architectural Decisions

1. **Single API file with 2 exports**: `capitalPoolApi` and `capitalTransactionApi` — both live in `capital.api.ts` since they're part of the same domain.

2. **Dashboard + Table pattern**: The list page shows BOTH the pool summary (4 financial cards + 3 share percentages) and the transaction table below it. This is different from all other modules which show just a table.

3. **Type filter uses server endpoint**: When filtering by transaction type, the hook calls `getByType()` (returns `List`) instead of `getAll()` (returns `Page`). So pagination is disabled when a type filter is active.

4. **Form page with tab switching**: `?tab=transaction` shows the transaction form; without it, shows the pool form. The pool form auto-detects whether a pool exists (edit mode) or not (create mode).

5. **No delete/edit for transactions**: Capital transactions are immutable audit records. Once recorded, they cannot be modified — only new transactions can be created.

### Hook Architecture

```
Capital Hooks (4):
├── useCapitalPool()                         → GET /current
├── useCapitalPoolActions()                  → create + update + recalculate
├── useCapitalTransactions(page, size, type?) → Paginated list + optional type filter
└── useCapitalTransactionCreate()            → POST transaction
```

### Capital List Page Layout

```
┌──────────────────────────────────────────────────────────┐
│ Capital Management              [Recalculate] [Edit Pool]│
│                                         [New Transaction]│
├──────────────────────────────────────────────────────────┤
│ Current Capital Pool Status                               │
│ ┌────────────┬────────────┬────────────┬────────────┐    │
│ │   Total    │  Available │   Locked   │  Returned  │    │
│ │  $100,000  │ 🟢 $60,000│ 🟡 $35,000 │ 🔵 $5,000  │    │
│ └────────────┴────────────┴────────────┴────────────┘    │
│ ┌──────────────┬──────────────┬──────────────┐           │
│ │ Owner Share  │Partner Share │ Utilization  │           │
│ │   60.0%      │   40.0%      │   35.0%      │           │
│ └──────────────┴──────────────┴──────────────┘           │
├──────────────────────────────────────────────────────────┤
│ Capital Transactions                                      │
│ [All][Investment][Withdrawal][Allocation][Return][Manual] │
│                                                           │
│ ┌───┬────────────┬────────┬──────────┬──────────┬──────┐ │
│ │ # │    Type    │ Amount │Avail.Bef.│Avail.Aft.│ Date │ │
│ ├───┼────────────┼────────┼──────────┼──────────┼──────┤ │
│ │ 1 │🟢 Invest  │$40,000 │ $20,000  │ $60,000  │02/01 │ │
│ │ 2 │🟡 Allocat │$15,000 │ $60,000  │ $45,000  │02/05 │ │
│ │ 3 │🔵 Return  │ $5,000 │ $45,000  │ $50,000  │02/15 │ │
│ └───┴────────────┴────────┴──────────┴──────────┴──────┘ │
│                                                           │
│ Showing 1–20 of 35   Rows: [20▼]  [◀] [▶]               │
└──────────────────────────────────────────────────────────┘
```

---

## 21. Data Flow: Full Request Lifecycle

Here's what happens when a user opens the Customers page:

```
 1. User clicks "Customers" in Sidebar
                │
 2. React Router matches /customers
    → lazy-loads CustomerListPage chunk
                │
 3. CustomerListPage mounts
    → calls usePagination() → { page: 0, size: 10 }
    → calls useDebounce(searchTerm, 400) → ""
    → calls useCustomers({ page: 0, size: 10 })
                │
 4. useCustomers calls customerApi.getAll({ page: 0, size: 10 })
                │
 5. customerApi.getAll calls api.get<PaginatedResponse<Customer>>()
                │
 6. REQUEST INTERCEPTOR fires:
    ├── Reads "token" from localStorage → Authorization: Bearer eyJhb...
    ├── Reads "language" from localStorage → Accept-Language: ar
    └── Logs: [API →] GET /v1/customers?page=0&size=10
                │
 7. Vite proxy forwards /api/v1/customers → http://localhost:8080/api/v1/customers
                │
 8. Spring Boot processes the request:
    CustomerController → CustomerService → CustomerRepository.findAll(Pageable)
                │
 9. Spring returns JSON:
    { "content": [...], "totalElements": 42, "number": 0, ... }
                │
10. RESPONSE INTERCEPTOR fires:
    ├── Status 200 → logs: [API ←] 200 /v1/customers
    └── Passes response through
                │
11. api.get unwraps response.data → PaginatedResponse<Customer>
                │
12. useCustomers sets state, component re-renders
                │
13. <Table columns={columns} data={customers} /> renders the table
                │
14. User sees the customer list with active/inactive toggle
```

---

## 22. What's Next

| Priority | Task | Status |
|---|---|---|
| ✅ Done | **Purchase module** — full CRUD with validation, vendor dropdown | Complete |
| ✅ Done | **Vendor module** — full CRUD, connected to Purchase form | Complete |
| ✅ Done | **Customer module** — CRUD + linked accounts + contracts + status toggle | Complete |
| ✅ Done | **Contract module** — CRUD + installment schedules + expenses + status tabs + financial tracking | Complete |
| ✅ Done | **Partner module** — CRUD + investments + withdrawals + commissions + status filter | Complete |
| ✅ Done | **Payment module** — Payment processing + statistics + cancel/refund + month filter | Complete |
| ✅ Done | **Capital module** — Pool dashboard + transactions + type filters + recalculate | Complete |
| 🔴 High | **ProtectedRoute** — wrap app routes with auth check | Stub exists |
| 🔴 High | **AuthContext** → use Axios instead of raw `fetch` | Partially done |
| 🟡 Medium | **Zustand stores** for client-side state (filters, selected items) | Library installed |
| 🟢 Low | **Dark mode** — `variables.css` is ready for a `[data-theme="dark"]` override | Design tokens ready |
| 🟢 Low | **Testing** — unit tests with Vitest, component tests with Testing Library | Not started |

> 📖 See `DEVELOPMENT_GUIDE.md` for a comprehensive guide on how the module pattern works,
> how to add new modules, handle backend changes, and troubleshooting tips.

---

*Generated on 2026-02-28 — MaalFlow Frontend v0.0.0*
