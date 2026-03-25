# MaalFlow — Complete Development Guide

> **Everything we built, why we built it, how it works, and how to add more.**  
> React 19 · TypeScript 5.9 · Vite 7 · Spring Boot Backend  
> Last updated: 2026-02-28

---

## Table of Contents

1. [Project Overview — What We Built](#1-project-overview)
2. [Architecture Layers (The Big Picture)](#2-architecture-layers)
3. [The Module Pattern — Our Core Strategy](#3-the-module-pattern)
4. [Complete File Map — Every File We Created](#4-complete-file-map)
5. [Deep Dive: Purchase Module (The First Module)](#5-deep-dive-purchase-module)
6. [Deep Dive: Vendor Module (The Second Module)](#6-deep-dive-vendor-module)
7. [How Data Flows Through the App](#7-how-data-flows)
8. [Key Concepts Explained with Examples](#8-key-concepts)
9. [Strategy: How to Add a New Module (Step-by-Step)](#9-how-to-add-a-new-module)
10. [Strategy: How to Handle Backend Changes](#10-handling-backend-changes)
11. [Strategy: How to Update an Existing Module](#11-updating-existing-module)
12. [Common Patterns You'll See Everywhere](#12-common-patterns)
13. [Configuration & Infrastructure Files](#13-configuration-files)
14. [Internationalization (EN/AR) Strategy](#14-i18n-strategy)
15. [Form Validation Strategy (Zod + react-hook-form)](#15-form-validation)
16. [RTL Support Strategy](#16-rtl-support)
17. [Tips & Best Practices](#17-tips-and-best-practices)
18. [Troubleshooting Common Issues](#18-troubleshooting)

---

## 1. Project Overview

### What is MaalFlow?

MaalFlow is a **bilingual installments management system** (Arabic + English). It's a React SPA that talks to a Spring Boot REST API.

### What modules exist?

| Module | Status | Description |
|--------|--------|-------------|
| **Purchase** | ✅ Complete | Record items bought from vendors |
| **Vendor** | ✅ Complete | Manage vendor contacts & info |
| **Payment** | ✅ Complete | Payment processing, statistics, cancel/refund, discounts |
| **Partner** | ✅ Complete | Partner management, investments, withdrawals, commissions |
| **Capital** | ✅ Complete | Capital pool dashboard, transactions, type filters |
| **Customer** | ✅ Complete | Customer profiles, linked accounts, contracts |
| **Contract** | ✅ Complete | Installment contracts, schedules, expenses, financial tracking |

### What does "Complete" mean?

A complete module has ALL of these files:

```
✅ Types           → src/types/modules/{module}.types.ts
✅ API Service     → src/services/api/modules/{module}.api.ts
✅ Hooks (5)       → src/hooks/modules/use{Module}s.ts (list)
                     src/hooks/modules/use{Module}.ts  (single)
                     src/hooks/modules/use{Module}Create.ts
                     src/hooks/modules/use{Module}Update.ts
                     src/hooks/modules/use{Module}Delete.ts
✅ Validator       → src/utils/validators/{module}.validator.ts
✅ Pages (5-6)     → src/pages/modules/installments/{module}/
                     {Module}ListPage.tsx + .css
                     {Module}FormPage.tsx + .css
                     {Module}DetailsPage.tsx + .css
                     {Module}CreatePage.tsx (wrapper)
                     {Module}EditPage.tsx   (wrapper)
                     {Module}ViewPage.tsx   (wrapper)
✅ Translations    → public/locales/en/{module}.json
                     public/locales/ar/{module}.json
✅ Routes          → Updated in routes.config.ts + router/index.tsx
✅ Navigation      → Updated in Sidebar.tsx + common.json
```

---

## 2. Architecture Layers

### The Layer Cake

Think of the app as a cake with 7 layers. Each layer only talks to the layer directly below it:

```
┌─────────────────────────────────────────────────────┐
│  Layer 7: PAGES                                      │
│  (PurchaseListPage, VendorFormPage, etc.)            │
│  → Combines hooks + components into a full screen    │
├─────────────────────────────────────────────────────┤
│  Layer 6: UI COMPONENTS                              │
│  (Table, Button, Input, Card, Modal)                 │
│  → Reusable building blocks, no business logic       │
├─────────────────────────────────────────────────────┤
│  Layer 5: CUSTOM HOOKS                               │
│  (usePurchases, useVendorCreate, usePagination)      │
│  → State management + API calls + toast messages     │
├─────────────────────────────────────────────────────┤
│  Layer 4: API SERVICES                               │
│  (purchaseApi, vendorApi)                            │
│  → Maps each backend endpoint to a typed function    │
├─────────────────────────────────────────────────────┤
│  Layer 3: HTTP CLIENT                                │
│  (api.get, api.post, interceptors)                   │
│  → Handles auth, errors, language, logging          │
├─────────────────────────────────────────────────────┤
│  Layer 2: TYPES & VALIDATION                         │
│  (Purchase, VendorRequest, Zod schemas)              │
│  → TypeScript interfaces + form validation rules     │
├─────────────────────────────────────────────────────┤
│  Layer 1: CONFIGURATION                              │
│  (.env, vite.config, i18n, routes)                   │
│  → Environment, aliases, proxy, translations         │
└─────────────────────────────────────────────────────┘
```

### Why layers?

**Separation of concerns.** Each layer has ONE job:

| Layer | Knows About | DOESN'T Know About |
|-------|------------|---------------------|
| Page | Which hooks to call, which components to render | How HTTP works |
| Hook | Which API function to call, how to manage state | Which page uses it |
| API Service | Which endpoint to hit, what types to expect | Auth tokens, error toasts |
| HTTP Client | How to attach headers, handle errors | Business logic |
| Types | Data shapes only | Everything else |

**Real benefit:** When you change authentication from localStorage to httpOnly cookies, you only edit `interceptors.ts`. Zero changes in hooks, pages, or API services.

---

## 3. The Module Pattern — Our Core Strategy

### What is it?

Every business module (Purchase, Vendor, Payment, etc.) follows the **exact same file structure** with the **exact same naming conventions**. This is the most important strategy in the project.

### The Pattern (7 file groups, always the same)

```
Module: "Purchase"  →  Replace with your module name

GROUP 1: TYPES
  src/types/modules/purchase.types.ts
  ├── Purchase           (response DTO → what the backend returns)
  ├── PurchaseRequest    (request DTO → what we send to the backend)
  └── PurchaseFilters    (extends PaginationParams → query params for list)

GROUP 2: API SERVICE
  src/services/api/modules/purchase.api.ts
  └── purchaseApi = {
        getAll(filters?) → PaginatedResponse<Purchase>
        getById(id)      → Purchase
        create(data)     → Purchase
        update(id, data) → Purchase
        delete(id)       → void
      }

GROUP 3: HOOKS (5 hooks, always the same pattern)
  src/hooks/modules/
  ├── usePurchases.ts      → fetch list, auto-refetch on filter change
  ├── usePurchase.ts       → fetch single by ID
  ├── usePurchaseCreate.ts → POST + toast
  ├── usePurchaseUpdate.ts → PUT/PATCH + toast
  └── usePurchaseDelete.ts → confirm + DELETE + toast

GROUP 4: VALIDATOR
  src/utils/validators/purchase.validator.ts
  ├── purchaseCreateSchema   (Zod schema matching backend @Annotations)
  ├── purchaseUpdateSchema   (same as create, or partial)
  ├── PurchaseFormData       (z.infer<typeof schema> → type for useForm)
  └── validatePurchase()     (standalone helper)

GROUP 5: PAGES (3 real + 3 wrappers)
  src/pages/modules/installments/purchase/
  ├── PurchaseListPage.tsx     → table + search + pagination + delete
  ├── PurchaseFormPage.tsx     → create/edit form with validation
  ├── PurchaseDetailsPage.tsx  → read-only view with actions
  ├── PurchaseCreatePage.tsx   → wrapper: <PurchaseFormPage />
  ├── PurchaseEditPage.tsx     → wrapper: <PurchaseFormPage purchaseId={id} />
  └── PurchaseViewPage.tsx     → wrapper: <PurchaseDetailsPage />

GROUP 6: TRANSLATIONS (per language)
  public/locales/en/purchase.json
  public/locales/ar/purchase.json

GROUP 7: WIRING (update existing files)
  routes.config.ts   → add PURCHASES routes + helpers
  router/index.tsx   → add lazy imports + route entries
  Sidebar.tsx        → add nav item
  common.json        → add nav label
  i18n.config.ts     → add namespace
  hooks/modules/index.ts → add barrel exports
```

### Why this pattern?

1. **Predictability** — When you need to add a Payment module, you KNOW exactly what files to create. No guessing.
2. **Consistency** — Every developer writes the same structure. Code reviews are faster.
3. **Copy-paste friendly** — You can literally copy the Purchase folder, find/replace "Purchase" → "Payment", and you're 80% done.
4. **Easy onboarding** — New team member? "Just look at how Purchase works, every module is the same."

---

## 4. Complete File Map — Every File We Created

### Infrastructure (created once, shared by all modules)

```
Configuration:
  .env.development              → API URL, debug flags, default language
  .env.production               → Production settings
  vite.config.ts                → Aliases, proxy, build optimization
  tsconfig.json                 → Strict TS + path mappings
  tsconfig.node.json            → Vite config TypeScript

Styling:
  src/styles/variables.css      → Design tokens (colors, spacing, RTL vars)
  src/styles/globals.css        → Utility classes
  src/styles/index.css          → Reset + imports

i18n:
  src/config/i18n.config.ts     → i18next setup (languages, namespaces, backend)
  src/config/env.config.ts      → Type-safe ENV object

HTTP Client:
  src/services/api/client/axios.config.ts  → Axios instance
  src/services/api/client/interceptors.ts  → Auth, errors, logging
  src/services/api/index.ts                → Typed get/post/put/patch/del

Common Types:
  src/types/common.types.ts     → ApiResponse<T>, PaginatedResponse<T>, etc.

Common Hooks:
  src/hooks/common/usePagination.ts
  src/hooks/common/useDebounce.ts
  src/hooks/common/useLocalStorage.ts
  src/hooks/common/useToggle.ts

Router:
  src/router/routes.config.ts   → APP_ROUTES + ROUTE_HELPERS
  src/router/index.tsx           → createBrowserRouter with lazy loading

Layout:
  src/components/layout/Header/Header.tsx
  src/components/layout/Sidebar/Sidebar.tsx
  src/components/layout/MainLayout/MainLayout.tsx

UI Components:
  src/components/common/Button/Button.tsx
  src/components/common/Input/Input.tsx
  src/components/common/Modal/Modal.tsx
  src/components/common/Table/Table.tsx
  src/components/ui/Card/Card.tsx
  src/components/ui/LoadingSpinner/LoadingSpinner.tsx

Translations (shared):
  public/locales/en/common.json
  public/locales/ar/common.json
  public/locales/en/validation.json
  public/locales/ar/validation.json

App Entry:
  src/App.tsx                   → Providers + router + toast
  src/main.tsx                  → ReactDOM.createRoot
```

### Purchase Module Files

```
src/types/modules/purchase.types.ts
src/services/api/modules/purchase.api.ts
src/hooks/modules/usePurchases.ts
src/hooks/modules/usePurchase.ts
src/hooks/modules/usePurchaseCreate.ts
src/hooks/modules/usePurchaseUpdate.ts
src/hooks/modules/usePurchaseDelete.ts
src/utils/validators/purchase.validator.ts
src/pages/modules/installments/purchase/PurchaseListPage.tsx + .css
src/pages/modules/installments/purchase/PurchaseFormPage.tsx + .css
src/pages/modules/installments/purchase/PurchaseDetailsPage.tsx + .css
src/pages/modules/installments/purchase/PurchaseCreatePage.tsx
src/pages/modules/installments/purchase/PurchaseEditPage.tsx
src/pages/modules/installments/purchase/PurchaseViewPage.tsx
public/locales/en/purchase.json
public/locales/ar/purchase.json
```

### Vendor Module Files

```
src/types/modules/vendor.types.ts
src/services/api/modules/vendor.api.ts
src/hooks/modules/useVendors.ts
src/hooks/modules/useVendor.ts
src/hooks/modules/useVendorCreate.ts
src/hooks/modules/useVendorUpdate.ts
src/hooks/modules/useVendorDelete.ts
src/utils/validators/vendor.validator.ts
src/pages/modules/installments/vendor/VendorListPage.tsx + .css
src/pages/modules/installments/vendor/VendorFormPage.tsx + .css
src/pages/modules/installments/vendor/VendorDetailsPage.tsx + .css
src/pages/modules/installments/vendor/VendorCreatePage.tsx
src/pages/modules/installments/vendor/VendorEditPage.tsx
src/pages/modules/installments/vendor/VendorViewPage.tsx
public/locales/en/vendor.json
public/locales/ar/vendor.json
```

### Customer Module Files

```
src/types/modules/customer.types.ts
src/services/api/modules/customer.api.ts
src/hooks/modules/useCustomers.ts
src/hooks/modules/useCustomer.ts
src/hooks/modules/useCustomerCreate.ts
src/hooks/modules/useCustomerUpdate.ts
src/hooks/modules/useCustomerDelete.ts
src/utils/validators/customer.validator.ts
src/pages/modules/installments/customer/CustomerListPage.tsx + .css
src/pages/modules/installments/customer/CustomerFormPage.tsx + .css
src/pages/modules/installments/customer/CustomerDetailsPage.tsx + .css
src/pages/modules/installments/customer/CustomerCreatePage.tsx
src/pages/modules/installments/customer/CustomerEditPage.tsx
src/pages/modules/installments/customer/CustomerViewPage.tsx
public/locales/en/customer.json
public/locales/ar/customer.json
```

### Contract Module Files

```
src/types/modules/contract.types.ts
src/services/api/modules/contract.api.ts
src/services/api/modules/contractExpense.api.ts
src/services/api/modules/installmentSchedule.api.ts
src/hooks/modules/useContracts.ts
src/hooks/modules/useContract.ts
src/hooks/modules/useContractCreate.ts
src/hooks/modules/useContractUpdate.ts
src/hooks/modules/useContractComplete.ts
src/hooks/modules/useInstallmentSchedules.ts
src/hooks/modules/useInstallmentActions.ts
src/hooks/modules/useContractExpenses.ts
src/hooks/modules/useContractExpenseActions.ts
src/utils/validators/contract.validator.ts
src/pages/modules/installments/contract/ContractListPage.tsx + .css
src/pages/modules/installments/contract/ContractFormPage.tsx + .css
src/pages/modules/installments/contract/ContractDetailsPage.tsx + .css
src/pages/modules/installments/contract/ContractCreatePage.tsx
src/pages/modules/installments/contract/ContractEditPage.tsx
src/pages/modules/installments/contract/ContractViewPage.tsx
public/locales/en/contract.json
public/locales/ar/contract.json
```

### Partner Module Files

```
src/types/modules/partner.types.ts
src/services/api/modules/partner.api.ts
src/services/api/modules/partnerInvestment.api.ts
src/services/api/modules/partnerWithdrawal.api.ts
src/services/api/modules/partnerCommission.api.ts
src/hooks/modules/usePartners.ts
src/hooks/modules/usePartner.ts
src/hooks/modules/usePartnerCreate.ts
src/hooks/modules/usePartnerUpdate.ts
src/hooks/modules/usePartnerDelete.ts
src/utils/validators/partner.validator.ts
src/pages/modules/installments/partner/PartnerListPage.tsx + .css
src/pages/modules/installments/partner/PartnerFormPage.tsx + .css
src/pages/modules/installments/partner/PartnerDetailsPage.tsx + .css
src/pages/modules/installments/partner/PartnerCreatePage.tsx
src/pages/modules/installments/partner/PartnerEditPage.tsx
src/pages/modules/installments/partner/PartnerViewPage.tsx
public/locales/en/partner.json
public/locales/ar/partner.json
```

### Payment Module Files

```
src/types/modules/payment.types.ts
src/services/api/modules/payment.api.ts
src/hooks/modules/usePayment.ts          (usePayments — paginated list)
src/hooks/modules/usePaymentDetail.ts
src/hooks/modules/usePaymentCreate.ts
src/hooks/modules/usePaymentActions.ts   (cancel + refund)
src/hooks/modules/usePaymentStatistics.ts
src/utils/validators/payment.validator.ts
src/pages/modules/installments/payment/PaymentListPage.tsx + .css
src/pages/modules/installments/payment/PaymentFormPage.tsx + .css
src/pages/modules/installments/payment/PaymentDetailsPage.tsx + .css
src/pages/modules/installments/payment/PaymentCreatePage.tsx
src/pages/modules/installments/payment/PaymentEditPage.tsx   (→ redirects to Details)
src/pages/modules/installments/payment/PaymentViewPage.tsx
public/locales/en/payment.json
public/locales/ar/payment.json
```

### Capital Module Files

```
src/types/modules/capital.types.ts
src/services/api/modules/capital.api.ts         (capitalPoolApi + capitalTransactionApi)
src/hooks/modules/useCapitalPool.ts
src/hooks/modules/useCapitalPoolActions.ts      (create, update, recalculate)
src/hooks/modules/useCapitalTransactions.ts     (paginated + type filter)
src/hooks/modules/useCapitalTransactionCreate.ts
src/utils/validators/capital.validator.ts       (pool + transaction schemas)
src/pages/modules/installments/capital/CapitalListPage.tsx + .css
src/pages/modules/installments/capital/CapitalFormPage.tsx + .css  (pool form + transaction form)
src/pages/modules/installments/capital/CapitalCreatePage.tsx
src/pages/modules/installments/capital/CapitalEditPage.tsx
public/locales/en/capital.json
public/locales/ar/capital.json
```

---

## 5. Deep Dive: Purchase Module

### 5.1 Types — The Foundation

**File:** `src/types/modules/purchase.types.ts`

**Purpose:** Define the exact shape of data coming from and going to the backend.

**How they map to Spring Boot DTOs:**

```
Java (Backend)                    TypeScript (Frontend)
─────────────────────────         ────────────────────────────
PurchaseResponse DTO         →    Purchase interface
  Long id                    →      id: number
  String productName         →      productName: string
  BigDecimal buyPrice        →      buyPrice: number
  LocalDate purchaseDate     →      purchaseDate: string   ← "2026-02-28"
  LocalDateTime createdAt    →      createdAt: string      ← "2026-02-28T14:30:00"
  String notes               →      notes: string | null
  String vendorName          →      vendorName: string

PurchaseRequest DTO          →    PurchaseRequest interface
  Long vendorId              →      vendorId: number
  String productName         →      productName: string
  BigDecimal buyPrice        →      buyPrice: number
  LocalDate purchaseDate     →      purchaseDate: string
  String notes (optional)    →      notes?: string
```

**Key decisions:**
- `BigDecimal` → `number`: JavaScript doesn't have BigDecimal. For financial apps with extreme precision needs, use a library like `decimal.js`. For our case, `number` is fine.
- `LocalDate` → `string`: JSON has no Date type. We keep dates as ISO strings (`"2026-02-28"`) and format them only for display using `formatDate()`.
- `notes: string | null` vs `notes?: string`: The response ALWAYS includes `notes` (it might be `null`). The request makes notes truly optional with `?`.

### 5.2 API Service — Mapping Endpoints

**File:** `src/services/api/modules/purchase.api.ts`

```typescript
const BASE = '/purchases'

export const purchaseApi = {
    // GET /api/purchases?page=0&size=10&searchTerm=Laptop
    async getAll(filters?: PurchaseFilters): Promise<PaginatedResponse<Purchase>> {
        return api.get<PaginatedResponse<Purchase>>(BASE, { params: filters })
    },

    // GET /api/purchases/42
    async getById(id: number): Promise<Purchase> {
        const res = await api.get<ApiResponse<Purchase>>(`${BASE}/${id}`)
        return res.data  // unwrap ApiResponse envelope
    },

    // POST /api/purchases (body: PurchaseRequest)
    async create(data: PurchaseRequest): Promise<Purchase> {
        const res = await api.post<ApiResponse<Purchase>>(BASE, data)
        return res.data
    },

    // PUT /api/purchases/42 (body: PurchaseRequest)
    async update(id: number, data: PurchaseRequest): Promise<Purchase> {
        const res = await api.put<ApiResponse<Purchase>>(`${BASE}/${id}`, data)
        return res.data
    },

    // DELETE /api/purchases/42
    async delete(id: number): Promise<void> {
        await api.del(`${BASE}/${id}`)
    },
}
```

**Why does `getAll` return directly but `getById` unwraps `.data`?**

The backend returns different shapes:
- **List endpoints** → Spring Data's `Page<T>` (no wrapper, direct JSON)
- **Single/mutation endpoints** → Your custom `ApiResponse<T>` wrapper with `{ success, message, data }`

So `getAll` passes the response through as-is, while `getById` extracts `.data` from the `ApiResponse` envelope.

### 5.3 Hooks — The State Bridge

**5 hooks, each with ONE job:**

```
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│  usePurchases    │     │  usePurchase     │     │ usePurchaseCreate│
│                  │     │                  │     │                  │
│  "Fetch the      │     │  "Fetch ONE      │     │  "Send a POST    │
│   list. Auto-    │     │   purchase       │     │   and handle     │
│   refetch when   │     │   by ID"         │     │   success/error" │
│   filters change"│     │                  │     │                  │
│                  │     │  Returns:        │     │  Returns:        │
│  Returns:        │     │  { purchase,     │     │  { createPurchase│
│  { purchases,    │     │    loading,      │     │    loading,      │
│    loading,      │     │    error,        │     │    error }       │
│    error,        │     │    refetch }     │     │                  │
│    totalPages,   │     │                  │     │                  │
│    totalElements,│     │                  │     │                  │
│    refetch }     │     │                  │     │                  │
└──────────────────┘     └──────────────────┘     └──────────────────┘
```

**Why separate hooks instead of one big `usePurchase` hook?**

1. **Single Responsibility** — Each hook does one thing. Easy to test, easy to debug.
2. **Selective imports** — The list page only needs `usePurchases` + `usePurchaseDelete`. The form page only needs `usePurchaseCreate` + `usePurchaseUpdate`. No wasted code.
3. **Independent loading states** — Deleting shows its own spinner without affecting the list's loading state.

**How `usePurchases` auto-refetches:**

```typescript
export function usePurchases(filters?: PurchaseFilters) {
    const [purchases, setPurchases] = useState<Purchase[]>([])
    const [loading, setLoading] = useState(true)

    // fetchPurchases is recreated ONLY when filters change
    const fetchPurchases = useCallback(async () => {
        setLoading(true)
        try {
            const page = await purchaseApi.getAll(filters)
            setPurchases(page.content)
        } finally {
            setLoading(false)
        }
    }, [filters])  // ← dependency: when filters change, new function is created

    // useEffect runs whenever fetchPurchases changes
    useEffect(() => {
        fetchPurchases()
    }, [fetchPurchases])  // ← triggers re-fetch

    return { purchases, loading, refetch: fetchPurchases }
}
```

**The chain:** User changes page → `filters` object changes → `useCallback` creates new `fetchPurchases` → `useEffect` detects change → calls `fetchPurchases()` → table updates.

### 5.4 Validator — Mirroring Backend Rules

**File:** `src/utils/validators/purchase.validator.ts`

**The strategy:** Every `@Annotation` on the Java DTO becomes a `.method()` call in the Zod schema:

```
Java @Annotation                    Zod Equivalent
──────────────────────────          ─────────────────────────
@NotNull                        →   z.number({ error: ... })
@Positive                       →   .positive({ error: ... })
@NotBlank                       →   z.string().min(1, ...)
@DecimalMin("1")                →   .min(100, ...)
@Digits(integer=10, fraction=2) →   .refine(regex, ...)
@PastOrPresent                  →   .refine(date <= today, ...)
@Size(max=500)                  →   .max(500, ...)
```

**Why validate on the frontend too?** The backend validates everything. But frontend validation gives **instant feedback** — the user sees "Price is required" before they even submit the form. Without it, every validation error requires a network round-trip.

**Why use the same error message keys?**

```typescript
const t = (key: string): string => i18n.t(key, { ns: 'validation' })

vendorId: z.number({ error: () => t('vendorId.required') })
```

This reads from `validation.json`:
```json
{ "vendorId": { "required": "التاجر مطلوب" } }  // Arabic
{ "vendorId": { "required": "Vendor is required" } }  // English
```

The validation messages are bilingual! When the user switches language, error messages update too.

### 5.5 Pages — The User Interface

**Three real pages, three wrappers:**

```
PurchaseCreatePage.tsx → just renders <PurchaseFormPage />
PurchaseEditPage.tsx   → reads :id, renders <PurchaseFormPage purchaseId={id} />
PurchaseViewPage.tsx   → just renders <PurchaseDetailsPage />
```

**Why wrappers?** The router needs separate components for each route. But the form logic is identical for create and edit — the only difference is whether we have a `purchaseId`. Instead of duplicating the form, we have ONE `PurchaseFormPage` that accepts an optional `purchaseId` prop:

```typescript
// No purchaseId → CREATE MODE (empty form, uses usePurchaseCreate)
<PurchaseFormPage />

// With purchaseId → EDIT MODE (prefills form, uses usePurchaseUpdate)
<PurchaseFormPage purchaseId={42} />
```

**PurchaseListPage anatomy:**

```
┌─────────────────────────────────────────────────┐
│ ┌─────────────┐                  ┌────────────┐ │
│ │ Purchases   │                  │ + Create   │ │ ← Header
│ └─────────────┘                  └────────────┘ │
│                                                  │
│ ┌──────────────────────────┐  ┌───────────────┐ │
│ │ 🔍 Search by vendor...   │  │ Clear Filters │ │ ← Filters
│ └──────────────────────────┘  └───────────────┘ │
│                                                  │
│ ┌────────────────────────────────────────────── │
│ │ Product  │ Price   │ Vendor │ Date  │Actions │ │ ← Table
│ ├──────────┼─────────┼────────┼───────┼────────│ │
│ │ Laptop   │ $1,200  │ Tech   │ 02/28 │ 👁✏🗑  │ │
│ │ Phone    │ $800    │ Smart  │ 02/25 │ 👁✏🗑  │ │
│ └──────────┴─────────┴────────┴───────┴────────│ │
│                                                  │
│ Showing 1-10 of 42    Rows: [10▼]   [◀] [▶]    │ ← Pagination
└─────────────────────────────────────────────────┘
```

---

## 6. Deep Dive: Vendor Module

### 6.1 What's Different from Purchase?

The Vendor module follows the **exact same pattern** as Purchase, with these differences:

| Aspect | Purchase | Vendor |
|--------|----------|--------|
| **Backend update method** | `PUT` (full replace) | `PATCH` (partial update) |
| **API helper** | `api.put()` | `api.patch()` |
| **Response type** | `Purchase` (list) | `Vendor` (list) + `VendorDetails` (single) |
| **Detail page content** | Purchase info + payment placeholder | Vendor info + purchases table |
| **Form fields** | vendorId, productName, buyPrice, date, notes | name, phone, address, notes |
| **Dropdown dependency** | Needs vendor list (fetches from API) | No external dependency |

### 6.2 Vendor Types

```typescript
// List view uses the summary (lightweight)
interface Vendor {
    id: number
    name: string
    phone: string
    address: string
    notes: string | null
}

// Detail view includes related purchases
interface VendorDetails extends Vendor {
    purchases?: VendorPurchase[]  // what this vendor sold us
}

// Embedded purchase info (from VendorResponse DTO)
interface VendorPurchase {
    productName: string
    buyPrice: number
}
```

**Why two interfaces (`Vendor` vs `VendorDetails`)?** The list endpoint returns `Page<VendorSummary>` (lightweight, no purchases). The detail endpoint returns `VendorResponse` with embedded purchases. Using a single type would force the list to carry empty `purchases[]` arrays — wasteful.

### 6.3 Vendor API — PATCH vs PUT

```typescript
// Purchase uses PUT (full replacement)
async update(id: number, data: PurchaseRequest): Promise<Purchase> {
    const res = await api.put<ApiResponse<Purchase>>(`${BASE}/${id}`, data)
    return res.data
}

// Vendor uses PATCH (partial update) — matches backend @PatchMapping
async update(id: number, data: VendorRequest): Promise<Vendor> {
    return api.patch<Vendor>(`${BASE}/${id}`, data)
}
```

**Why does this matter?** The backend decides:
- `@PutMapping` → expects ALL fields, overwrites the entire entity
- `@PatchMapping` → expects SOME fields, only updates provided ones

Our frontend matches the backend's choice. The Purchase backend uses PUT, the Vendor backend uses PATCH.

### 6.4 Connecting Purchase to Vendor

Before the Vendor module existed, `PurchaseFormPage` used mock vendor data:

```typescript
// ❌ BEFORE — hardcoded mock data
const MOCK_VENDORS = [
    { id: 1, name: 'TechVendor Inc.' },
    { id: 2, name: 'Office Supplies Co.' },
]
```

After creating the Vendor module, we replaced this with a real API call:

```typescript
// ✅ AFTER — real data from backend
const { vendors, loading: vendorsLoading } = useVendors({ size: 100 })
```

**What changed in PurchaseFormPage:**

1. **Import:** Added `useVendors` from `@hooks/modules` and `Vendor` type
2. **Data source:** `MOCK_VENDORS` → `useVendors({ size: 100 })`
3. **Dropdown:** `MOCK_VENDORS.map(v => ...)` → `vendors.map(v => ...)`
4. **Loading state:** Shows "..." in dropdown while vendors load
5. **Edit mode prefill:** `MOCK_VENDORS.find(v => v.name === ...)` → `vendors.find((v: Vendor) => v.name === ...)`

This is a great example of **progressive enhancement** — the system worked with mocks, and we upgraded to real data with minimal changes.

---

## 7. How Data Flows Through the App

### Flow 1: Loading the Vendor List

```
User clicks "Vendors" in sidebar
         │
         ▼
React Router matches /vendors
         │
         ▼
Lazy-loads VendorListPage.tsx chunk (first visit only)
         │
         ▼
VendorListPage mounts, calls:
  ├── usePagination() → { page: 0, size: 10 }
  ├── useDebounce(searchTerm, 400) → ""
  └── useVendors({ page: 0, size: 10 })
         │
         ▼
useVendors calls vendorApi.getAll({ page: 0, size: 10 })
         │
         ▼
vendorApi.getAll calls api.get<PaginatedResponse<Vendor>>('/vendors', { params })
         │
         ▼
api.get calls axios.get() → Request interceptor fires:
  ├── Adds Authorization: Bearer <token>
  ├── Adds Accept-Language: ar
  └── Logs: [API →] GET /vendors?page=0&size=10
         │
         ▼
Vite proxy: localhost:3000/api/vendors → localhost:8080/api/vendors
         │
         ▼
Spring Boot: VendorController → VendorService → VendorRepository.findAll(Pageable)
         │
         ▼
Returns JSON: { content: [...], totalElements: 25, totalPages: 3, ... }
         │
         ▼
Response interceptor: logs [API ←] 200, passes through
         │
         ▼
api.get unwraps response.data → PaginatedResponse<Vendor>
         │
         ▼
useVendors sets state: vendors=page.content, totalPages=3, loading=false
         │
         ▼
VendorListPage re-renders with data
         │
         ▼
<Table columns={columns} data={vendors} /> renders the table
```

### Flow 2: Creating a New Vendor

```
User fills form: name="TechCorp", phone="+1234567890", address="123 Main St"
         │
         ▼
User clicks "Save Vendor"
         │
         ▼
react-hook-form validates with Zod schema:
  ├── name: "TechCorp" → ✅ min(4), max(50)
  ├── phone: "+1234567890" → ✅ regex /^\+?[0-9]{10,15}$/
  └── address: "123 Main St" → ✅ min(1), max(100)
         │
         ▼
onSubmit(data) called → useVendorCreate.createVendor(payload)
         │
         ▼
vendorApi.create(payload) → api.post('/vendors', payload)
         │
         ▼
POST http://localhost:8080/api/vendors
Body: { "name": "TechCorp", "phone": "+1234567890", "address": "123 Main St" }
         │
         ▼
Spring Boot validates with @Annotations:
  ├── @NotBlank ✅
  ├── @Size(min=4, max=50) ✅
  └── @Pattern(regexp="^\+?[0-9]{10,15}$") ✅
         │
         ▼
VendorService.create() → saves to database → returns VendorSummary
         │
         ▼
Response: 201 Created, body: { id: 6, name: "TechCorp", ... }
         │
         ▼
useVendorCreate receives vendor → toast.success("Vendor created successfully")
         │
         ▼
Returns created vendor → onSubmit navigates to APP_ROUTES.VENDORS.LIST
         │
         ▼
VendorListPage loads → shows TechCorp in the table
```

### Flow 3: Search with Debounce

```
User types "T" in search box
         │
         ▼
setSearchTerm("T") → immediate state update
         │
         ▼
useDebounce("T", 400) → returns "" (still debouncing, 400ms timer starts)
         │
         ▼
User types "e" (50ms later) → searchTerm = "Te"
         │
         ▼
useDebounce("Te", 400) → returns "" (resets 400ms timer)
         │
         ▼
User types "c" (80ms later) → searchTerm = "Tec"
         │
         ▼
useDebounce("Tec", 400) → returns "" (resets timer again)
         │
         ▼
... 400ms of no typing ...
         │
         ▼
useDebounce returns "Tec" → debouncedSearch = "Tec"
         │
         ▼
filters useMemo recomputes: { page: 0, size: 10, search: "Tec" }
         │
         ▼
useVendors detects filter change → calls vendorApi.getAll({ search: "Tec" })
         │
         ▼
GET /api/vendors?page=0&size=10&search=Tec
         │
         ▼
Only ONE API call made (not 3)! Debounce saved 2 unnecessary requests.
```

---

## 8. Key Concepts Explained with Examples

### 8.1 TypeScript Generics

**What:** A way to make a function/component work with ANY type while keeping type safety.

**Why:** Without generics, our `Table` component would have to use `any` for row data — losing all type checking.

```typescript
// WITHOUT generics — row is 'any', no autocomplete
function Table({ data }: { data: any[] }) {}

// WITH generics — row is fully typed
function Table<T>({ data, columns }: { data: T[], columns: TableColumn<T>[] }) {}

// Usage — T is inferred as Vendor
<Table<Vendor>
    columns={[
        { key: 'name', label: 'Name' },
        { key: 'phone', label: 'Phone' },
        { key: 'address', label: 'Address', render: (row) => row.address.toUpperCase() }
        //                                                    ^^^ TypeScript knows row is Vendor
    ]}
    data={vendors}  // Vendor[]
/>
```

### 8.2 Barrel Exports

**What:** An `index.ts` file that re-exports everything from a folder.

```typescript
// src/hooks/modules/index.ts
export { usePurchases } from './usePurchases'
export { usePurchase } from './usePurchase'
export { usePurchaseCreate } from './usePurchaseCreate'
export { useVendors } from './useVendors'
export { useVendor } from './useVendor'
// ...
```

**Why:**
```typescript
// ❌ Without barrel — long, fragile paths
import { usePurchases } from '@hooks/modules/usePurchases'
import { usePurchaseDelete } from '@hooks/modules/usePurchaseDelete'
import { useVendors } from '@hooks/modules/useVendors'

// ✅ With barrel — clean, one import
import { usePurchases, usePurchaseDelete, useVendors } from '@hooks/modules'
```

### 8.3 useCallback + useEffect (Auto-Refetch Pattern)

**The problem:** You want to fetch data when filters change, but `useEffect` with an async function inside creates a new function every render.

**The solution:**

```typescript
// 1. Wrap the fetch function in useCallback with filters as dependency
const fetchVendors = useCallback(async () => {
    const page = await vendorApi.getAll(filters)
    setVendors(page.content)
}, [filters])  // ← only creates a new function when filters actually change

// 2. Run the fetch whenever the function changes
useEffect(() => {
    fetchVendors()
}, [fetchVendors])

// 3. Expose refetch for manual refresh (e.g., after delete)
return { vendors, refetch: fetchVendors }
```

**Why `useCallback`?** Without it, `fetchVendors` would be a new function on every render, making `useEffect` fire on every render — infinite loop!

### 8.4 Controlled vs Uncontrolled Forms

We use **react-hook-form** which is a hybrid approach:

```typescript
const { register, handleSubmit, formState: { errors } } = useForm<VendorFormData>({
    resolver: zodResolver(vendorCreateSchema),  // Zod validates
    defaultValues: { name: '', phone: '', address: '', notes: '' },
})

// register() connects an input to react-hook-form
<input {...register('name')} />
// This is like writing:
// <input ref={nameRef} name="name" onChange={handleChange} />
// But react-hook-form does it internally, avoiding re-renders on every keystroke
```

**Why react-hook-form over controlled inputs?**
```typescript
// ❌ Controlled — re-renders entire form on EVERY keystroke
const [name, setName] = useState('')
<input value={name} onChange={e => setName(e.target.value)} />

// ✅ react-hook-form — only re-renders when errors change
<input {...register('name')} />
```

---

## 9. How to Add a New Module (Step-by-Step)

Let's say you need to add the **Payment** module. Here's the exact recipe:

### Step 1: Study the Backend DTOs

```java
// What does the backend return? (Response DTO)
public class PaymentResponse {
    private Long id;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod; // CASH, BANK_TRANSFER, etc.
    private String customerName;
    private String notes;
}

// What does the backend expect? (Request DTO)
public class PaymentRequest {
    @NotNull private Long customerId;
    @NotNull @Positive private BigDecimal amount;
    @NotNull @PastOrPresent private LocalDate paymentDate;
    @NotBlank private String paymentMethod;
    @Size(max = 500) private String notes;
}
```

### Step 2: Create Types

```
📄 src/types/modules/payment.types.ts
```

Map every Java field to TypeScript. Copy `purchase.types.ts` and adapt.

### Step 3: Create API Service

```
📄 src/services/api/modules/payment.api.ts
```

Map every endpoint. Copy `purchase.api.ts`, change:
- `BASE = '/payments'`
- Types: `Purchase` → `Payment`, `PurchaseRequest` → `PaymentRequest`
- Check if backend uses PUT or PATCH for updates

### Step 4: Create Hooks

```
📄 src/hooks/modules/usePayments.ts
📄 src/hooks/modules/usePayment.ts
📄 src/hooks/modules/usePaymentCreate.ts
📄 src/hooks/modules/usePaymentUpdate.ts
📄 src/hooks/modules/usePaymentDelete.ts
```

Copy the Purchase hooks, find/replace:
- `Purchase` → `Payment`
- `purchase` → `payment`
- `purchaseApi` → `paymentApi`
- Translation namespace: `'purchase'` → `'payment'`

### Step 5: Create Validator

```
📄 src/utils/validators/payment.validator.ts
```

Map each `@Annotation` to a Zod method. Copy `purchase.validator.ts` and adapt fields.

### Step 6: Create Translations

```
📄 public/locales/en/payment.json   (copy purchase.json, change values)
📄 public/locales/ar/payment.json   (copy purchase.json, change values)
```

### Step 7: Create Pages

```
📄 src/pages/modules/installments/payment/PaymentListPage.tsx + .css
📄 src/pages/modules/installments/payment/PaymentFormPage.tsx + .css
📄 src/pages/modules/installments/payment/PaymentDetailsPage.tsx + .css
📄 src/pages/modules/installments/payment/PaymentCreatePage.tsx
📄 src/pages/modules/installments/payment/PaymentEditPage.tsx
📄 src/pages/modules/installments/payment/PaymentViewPage.tsx
```

Copy Purchase pages, find/replace class names and imports.

### Step 8: Wire Everything

1. **`hooks/modules/index.ts`** → Add exports for all 5 payment hooks
2. **`i18n.config.ts`** → Add `'payment'` to NAMESPACES array
3. **`routes.config.ts`** → Routes already exist (PAYMENTS.LIST, etc.)
4. **`router/index.tsx`** → Lazy imports already exist (update if paths differ)
5. **`Sidebar.tsx`** → Nav item already exists
6. **`validation.json`** → Add payment-specific validation keys
7. **`common.json`** → Nav label already exists

### Step 9: Test

1. Start backend: `mvn spring-boot:run`
2. Start frontend: `npm run dev`
3. Navigate to `/payments` — list should load
4. Click "Create" — form should validate
5. Submit — should create and redirect to list
6. Edit — should prefill and update
7. Delete — should confirm and remove
8. Switch language — everything should translate

### Advanced: Multi-Entity Modules (Contract Pattern)

If your module has **sub-entities** (like Contract → InstallmentSchedule + ContractExpense), follow the Contract module pattern:

1. **Types**: Put ALL types in one file (`contract.types.ts`) — main entity, sub-entities, and all enums
2. **API services**: Create **one file per backend controller** — `contract.api.ts`, `installmentSchedule.api.ts`, `contractExpense.api.ts`
3. **Hooks**: Group sub-entity actions into **action hooks** rather than 5 CRUD hooks per entity:
   - `useInstallmentActions()` → generate, reschedule, swap, skip (action-oriented, not CRUD)
   - `useContractExpenseActions()` → create, update, delete (CRUD in one hook)
4. **Detail page**: Fetch all sub-entities in `useContract(id)` via `Promise.all`, render as separate sections
5. **No separate routes** for sub-entities — they live on the detail page, not their own URL

```
Standard module:   5 hooks  +  1 API service  +  1 types file
Contract module:  10 hooks  +  3 API services +  1 types file (5 enums)
```

---

## 10. Handling Backend Changes

### Scenario 1: Backend adds a new field to an existing DTO

**Example:** Backend adds `email: String` to `VendorSummary`

**Steps:**

1. **Update types:** Add `email: string` to the `Vendor` interface in `vendor.types.ts`
2. **Update list page:** Add a new column to the table
3. **Update detail page:** Add a field to display it
4. **If the field is in the Request DTO too:**
   - Update `VendorRequest` interface
   - Update Zod validator
   - Update form page (add input)
5. **Update translations:** Add labels for the new field

### Scenario 2: Backend changes an endpoint method (PUT → PATCH)

**Steps:**

1. **Update API service:** Change `api.put()` to `api.patch()` in the `update` method
2. Nothing else changes — hooks, pages, and validators stay the same

### Scenario 3: Backend adds a new endpoint

**Example:** `GET /api/vendors/search?name=Tech`

**Steps:**

1. **Add method to API service:**
   ```typescript
   async search(name: string): Promise<Vendor[]> {
       return api.get<Vendor[]>(`${BASE}/search`, { params: { name } })
   }
   ```
2. **Create a hook if needed:**
   ```typescript
   export function useVendorSearch(name: string) { ... }
   ```
3. **Use in a component** wherever the search is needed

### Scenario 4: Backend changes the response wrapper

**Example:** Backend starts wrapping list responses in `ApiResponse<Page<T>>`

**Steps:**

1. **Update API service** `getAll` method to unwrap:
   ```typescript
   async getAll(filters?: VendorFilters): Promise<PaginatedResponse<Vendor>> {
       const res = await api.get<ApiResponse<PaginatedResponse<Vendor>>>(BASE, { params: filters })
       return res.data  // ← added unwrapping
   }
   ```
2. Nothing else changes — hooks still receive `PaginatedResponse<Vendor>`

---

## 11. Updating an Existing Module

### Adding a filter to the Vendor list

**Example:** Add a date range filter to vendors

1. **Update `VendorFilters`** in `vendor.types.ts`:
   ```typescript
   export interface VendorFilters extends PaginationParams {
       search?: string
       startDate?: string    // ← new
       endDate?: string      // ← new
   }
   ```

2. **Update `VendorListPage`** — add date inputs and include in filters:
   ```typescript
   const [startDate, setStartDate] = useState('')
   const [endDate, setEndDate] = useState('')

   const filters = useMemo<VendorFilters>(() => ({
       page, size,
       ...(debouncedSearch && { search: debouncedSearch }),
       ...(startDate && { startDate }),
       ...(endDate && { endDate }),
   }), [page, size, debouncedSearch, startDate, endDate])
   ```

3. **No changes needed** in hooks, API service, or validator — the filter flows through automatically!

### Adding a new action button

**Example:** Add an "Export PDF" button to the vendor detail page

1. **Add API method** (if backend supports it):
   ```typescript
   async exportPdf(id: number): Promise<Blob> {
       return api.get(`${BASE}/${id}/export`, { responseType: 'blob' })
   }
   ```

2. **Add button** to `VendorDetailsPage.tsx`:
   ```typescript
   <Button variant="secondary" onClick={() => handleExport(vendor.id)}>
       {t('details.export')}
   </Button>
   ```

3. **Add translations** to `vendor.json`

---

## 12. Common Patterns You'll See Everywhere

### Pattern 1: Error Boundary (try/catch + toast)

Every mutation hook follows this exact pattern:

```typescript
try {
    const result = await apiCall(data)
    toast.success(t('successKey'))    // Show green toast
    return result                      // Return data to caller
} catch (err) {
    const message = err instanceof Error ? err.message : t('errorKey')
    setError(message)                  // Store for display
    toast.error(t('errorKey'))         // Show red toast
    return null                        // Signal failure to caller
} finally {
    setLoading(false)                  // Always stop spinner
}
```

### Pattern 2: Conditional Navigation After Mutation

```typescript
const created = await createVendor(payload)
if (created) navigate(APP_ROUTES.VENDORS.LIST)  // only navigate on success
// if null (failed), user stays on form, sees error toast, can fix and retry
```

### Pattern 3: useMemo for Filters

```typescript
const filters = useMemo<VendorFilters>(() => ({
    page, size,
    ...(debouncedSearch && { search: debouncedSearch }),
}), [page, size, debouncedSearch])
```

**Why `useMemo`?** Without it, `filters` would be a new object on every render (even if values haven't changed), causing `useVendors` to refetch unnecessarily.

**Why `...(condition && { key: value })`?** This only includes the property if the condition is true. Empty search strings are excluded from the query params, keeping the URL clean.

### Pattern 4: Create/Edit Form Dual Mode

```typescript
interface FormProps { moduleId?: number }

function FormPage({ moduleId }: FormProps) {
    const isEditMode = moduleId !== undefined

    // Fetch existing data only in edit mode
    const { data } = useFetch(isEditMode ? moduleId : 0)

    // Use different mutation hooks based on mode
    const { create } = useCreate()
    const { update } = useUpdate()

    // Pre-fill form in edit mode
    useEffect(() => {
        if (isEditMode && data) reset(data)
    }, [isEditMode, data])

    // Submit to correct endpoint
    const onSubmit = async (formData) => {
        if (isEditMode) {
            await update(moduleId, formData)
        } else {
            await create(formData)
        }
    }
}
```

### Pattern 5: CSS BEM Naming

Every component uses BEM (Block-Element-Modifier):

```css
.vendor-list__header          /* block: vendor-list, element: header */
.vendor-list__title           /* block: vendor-list, element: title */
.vendor-list__actions         /* block: vendor-list, element: actions */
.vendor-form__select--error   /* block: vendor-form, element: select, modifier: error */
```

**Why BEM?** Prevents CSS conflicts. `.header` might clash with another component. `.vendor-list__header` never will.

---

## 13. Configuration & Infrastructure Files

### File: `.env.development`

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_NAME=MaalFlow
VITE_DEFAULT_LANGUAGE=ar
VITE_ENABLE_DEBUG=true
```

**Why `VITE_` prefix?** Vite only exposes env vars starting with `VITE_` to the browser. Without the prefix, secrets (like DB passwords) would leak into the JavaScript bundle.

### File: `vite.config.ts`

Three critical features:

1. **Path aliases:** `@components` → `./src/components` (avoid `../../../`)
2. **Dev proxy:** `/api` → `http://localhost:8080` (avoid CORS)
3. **Manual chunks:** Split vendor libraries into cached bundles

### File: `tsconfig.json`

Key settings:
- `"strict": true` — catches more bugs at compile time
- `"paths"` — must match Vite aliases exactly
- `"jsx": "react-jsx"` — enables modern JSX transform (no `import React`)

---

## 14. Internationalization (EN/AR) Strategy

### How it works

```
public/locales/
  ├── en/
  │   ├── common.json      ← "Save", "Cancel", nav labels
  │   ├── purchase.json    ← "Create Purchase", column labels
  │   ├── vendor.json      ← "Create Vendor", column labels
  │   └── validation.json  ← "Name is required", "Phone invalid"
  └── ar/
      ├── common.json      ← "حفظ", "إلغاء", أسماء القوائم
      ├── purchase.json    ← "إضافة منتج", تسميات الأعمدة
      ├── vendor.json      ← "إضافة بائع", تسميات الأعمدة
      └── validation.json  ← "الاسم مطلوب", "الهاتف غير صالح"
```

### Using translations in components

```typescript
// Load from 'vendor' namespace
const { t } = useTranslation('vendor')
t('title')           // EN: "Vendors"      AR: "البائعون"
t('form.name')       // EN: "Name"         AR: "الاسم"
t('created')         // EN: "Vendor created successfully"

// Load from 'common' namespace (shared)
const { t: tc } = useTranslation('common')
tc('save')           // EN: "Save"         AR: "حفظ"
tc('delete')         // EN: "Delete"       AR: "حذف"
tc('nav.vendors')    // EN: "Vendors"      AR: "البائعون"

// Interpolation (dynamic values)
t('pagination.showing', { from: 1, to: 10, total: 42 })
// EN: "Showing 1–10 of 42"
// AR: "عرض 1–10 من 42"
```

### Adding a new translation namespace

1. Create `public/locales/en/{module}.json` and `public/locales/ar/{module}.json`
2. Add namespace to `NAMESPACES` array in `i18n.config.ts`
3. Use `useTranslation('{module}')` in components

---

## 15. Form Validation Strategy

### Three layers of validation

```
Layer 1: HTML attributes       (min, max, required — basic browser validation)
Layer 2: Zod schema            (instant feedback, complex rules, bilingual messages)
Layer 3: Spring Boot @Annotations (server-side, final authority, can't be bypassed)
```

### Zod ↔ Spring Boot mapping

```
Java                              Zod
─────────────────────────         ────────────────────────
@NotNull                      →   z.number()              (required by default)
@NotBlank                     →   z.string().min(1)
@Positive                     →   .positive()
@DecimalMin("1")              →   .min(1)
@Digits(integer=10, fraction=2) → .refine(regex)
@PastOrPresent                →   .refine(date <= today)
@Size(min=4, max=50)          →   .min(4).max(50)
@Pattern(regexp="...")        →   .regex(/.../)
Optional field                →   .optional().or(z.literal(''))
```

### Why validate on BOTH sides?

| Frontend (Zod) | Backend (Spring) |
|----------------|-------------------|
| Instant feedback (no network) | Final authority (can't be bypassed) |
| Better UX | Security (users can disable JS) |
| Reduces server load | Business rules that need DB access |

---

## 16. RTL Support Strategy

### How it works

1. `LanguageContext` sets `document.documentElement.dir = "rtl"` when Arabic is selected
2. CSS uses **logical properties** that auto-flip:

```css
/* ❌ Physical properties — DON'T flip */
margin-left: 16px;
padding-right: 8px;
border-left: 1px solid gray;
text-align: left;

/* ✅ Logical properties — DO flip automatically */
margin-inline-start: 16px;    /* left in LTR, right in RTL */
padding-inline-end: 8px;      /* right in LTR, left in RTL */
border-inline-start: 1px solid gray;
text-align: start;            /* left in LTR, right in RTL */
```

### Logical property cheat sheet

| Physical (DON'T use) | Logical (USE this) |
|-----------------------|-------------------|
| `margin-left` | `margin-inline-start` |
| `margin-right` | `margin-inline-end` |
| `padding-left` | `padding-inline-start` |
| `padding-right` | `padding-inline-end` |
| `border-left` | `border-inline-start` |
| `text-align: left` | `text-align: start` |
| `left: 0` | `inset-inline-start: 0` |
| `margin-top` | `margin-block-start` |
| `margin-bottom` | `margin-block-end` |

---

## 17. Tips & Best Practices

### For TypeScript

1. **Never use `any`** — use `unknown` and narrow with type guards
2. **Use `interface` for objects, `type` for unions/intersections**
3. **Always type function return values** for hooks and API services
4. **Use `as const`** for constant objects (routes, API objects)

### For React

1. **One component per file** — easier to find, easier to test
2. **Keep pages thin** — move logic to hooks, keep pages as composition
3. **Use `useMemo` for filter objects** — prevents unnecessary re-fetches
4. **Use `useCallback` for functions passed to useEffect** — prevents infinite loops
5. **Always handle loading, error, and empty states** — users should never see a blank screen

### For API Integration

1. **Match the backend exactly** — if backend uses PUT, use PUT. If PATCH, use PATCH.
2. **Don't invent frontend-only fields** — every type should map to a real DTO
3. **Handle errors gracefully** — toast for user, console for developer
4. **Use interceptors for cross-cutting concerns** — auth, language, logging
5. **Don't duplicate error handling** — interceptors handle 401/403/500, hooks handle business errors

### For CSS

1. **Use BEM naming** — `.module__element--modifier`
2. **Use CSS custom properties** — defined in `variables.css`
3. **Use logical properties** — for RTL support
4. **Mobile-first** — default styles for mobile, `@media` for desktop
5. **One CSS file per page component** — not per tiny component

### For File Organization

1. **Follow the module pattern** — same structure for every module
2. **Use barrel exports** — `index.ts` in every folder
3. **Keep translations organized** — one namespace per module
4. **Update ALL integration points** — routes, sidebar, barrel exports, i18n

### For Performance

1. **Lazy load pages** — `React.lazy()` + `Suspense`
2. **Debounce search** — 400ms delay prevents API spam
3. **Paginate everything** — never load all records at once
4. **Split vendor chunks** — `manualChunks` in Vite config
5. **Use `useMemo`** for expensive computations (column definitions, filter objects)

---

## 18. Troubleshooting Common Issues

### Problem: TypeScript doesn't recognize new routes

**Symptom:** `APP_ROUTES.VENDORS` shows "Property does not exist" error

**Cause:** TypeScript language server has stale cache

**Fix:** Restart TypeScript server (in VS Code: Ctrl+Shift+P → "TypeScript: Restart TS Server"). Or run `npx tsc --noEmit` to verify — if it passes, the IDE just needs a refresh.

### Problem: Translations not loading

**Symptom:** Keys show as `"vendor.title"` instead of `"Vendors"`

**Cause:** Namespace not registered in i18n config

**Fix:** Add the namespace to `NAMESPACES` array in `src/config/i18n.config.ts`:
```typescript
export const NAMESPACES = ['common', 'purchase', 'vendor', 'validation', ...]
```

### Problem: API returns 404

**Symptom:** `GET /api/vendors` returns 404

**Cause:** Vite proxy not forwarding, or backend not running

**Fix:** 
1. Check backend is running on port 8080
2. Check `vite.config.ts` has proxy config for `/api`
3. Check the endpoint URL matches exactly

### Problem: Form validation not in the right language

**Symptom:** Validation errors always show in English

**Cause:** Zod messages are evaluated at import time, not at render time

**Fix:** Use functions (not values) for error messages:
```typescript
// ❌ Evaluated once at import time
vendorId: z.number({ error: t('vendorId.required') })

// ✅ Evaluated each time validation runs
vendorId: z.number({ error: () => t('vendorId.required') })
```

### Problem: Infinite re-render loop

**Symptom:** Console shows hundreds of API calls

**Cause:** Filter object recreated on every render without `useMemo`

**Fix:** Wrap filter objects in `useMemo`:
```typescript
const filters = useMemo(() => ({ page, size, search }), [page, size, search])
```

### Problem: Edit form doesn't prefill

**Symptom:** Form shows empty fields in edit mode

**Cause:** `reset()` called before data is fetched, or dependency array missing

**Fix:** Include data AND dependencies in useEffect:
```typescript
useEffect(() => {
    if (isEditMode && existingData) {
        reset({ ...existingData })
    }
}, [isEditMode, existingData, reset])  // ← all three dependencies
```

---

## Quick Reference Card

### "I need to..." → "Here's where..."

| Task | File(s) to edit |
|------|----------------|
| Add a new module | Follow Section 9 (7 file groups) |
| Add a backend field | types → api → pages → translations |
| Change API base URL | `.env.development` |
| Add a nav menu item | `Sidebar.tsx` + `common.json` (en+ar) |
| Add a validation rule | `{module}.validator.ts` + `validation.json` (en+ar) |
| Change error handling | `interceptors.ts` |
| Add a new route | `routes.config.ts` + `router/index.tsx` |
| Fix a translation | `public/locales/{lang}/{namespace}.json` |
| Add a new language | `i18n.config.ts` → SUPPORTED_LANGUAGES + add locale folder |
| Debug API calls | Set `VITE_ENABLE_DEBUG=true` in `.env.development` |

---

*Generated on 2026-02-28 — MaalFlow Frontend v0.0.0*

