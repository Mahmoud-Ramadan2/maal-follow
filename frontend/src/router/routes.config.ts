// ============================================================
// Route Path Constants
//
// Single source of truth for every URL in the app.
// Import APP_ROUTES anywhere you need a path — components,
// navigation helpers, breadcrumbs, etc.
// ============================================================

// ────────────────────────────────────────────────────────────
// Static paths
// ────────────────────────────────────────────────────────────

export const APP_ROUTES = {
    // ── Auth ────────────────────────────────────────────────
    AUTH: {
        LOGIN: '/login',
        REGISTER: '/register',
    },

    // ── Dashboard ───────────────────────────────────────────
    DASHBOARD: '/',

    // ── Purchases ───────────────────────────────────────────
    PURCHASES: {
        LIST: '/purchases',
        CREATE: '/purchases/create',
        STATISTICS: '/purchases/statistics',
        /** Route pattern consumed by React Router (`:id` param) */
        EDIT_PATTERN: '/purchases/:id/edit',
        VIEW_PATTERN: '/purchases/:id',
    },

    // ── Procurement workspace (Purchases + Vendors) ───────
    PROCUREMENT: {
        LIST: '/procurement',
    },

    // ── Payments ────────────────────────────────────────────
    PAYMENTS: {
        LIST: '/payments',
        CREATE: '/payments/create',
        EDIT_PATTERN: '/payments/:id/edit',
        VIEW_PATTERN: '/payments/:id',
    },

    // ── Partners ────────────────────────────────────────────
    PARTNERS: {
        LIST: '/partners',
        CREATE: '/partners/create',
        EDIT_PATTERN: '/partners/:id/edit',
        VIEW_PATTERN: '/partners/:id',
    },

    // ── Customers ───────────────────────────────────────────
    CUSTOMERS: {
        LIST: '/customers',
        CREATE: '/customers/create',
        EDIT_PATTERN: '/customers/:id/edit',
        VIEW_PATTERN: '/customers/:id',
    },

    // ── Capital ─────────────────────────────────────────────
    CAPITAL: {
        LIST: '/capital',
        CREATE: '/capital/create',
        EDIT_PATTERN: '/capital/:id/edit',
    },

    // ── Contracts ───────────────────────────────────────────
    CONTRACTS: {
        LIST: '/contracts',
        CREATE: '/contracts/create',
        EDIT_PATTERN: '/contracts/:id/edit',
        VIEW_PATTERN: '/contracts/:id',
    },

    // ── Vendors ─────────────────────────────────────────────
    VENDORS: {
        LIST: '/vendors',
        CREATE: '/vendors/create',
        /** Route pattern consumed by React Router (`:id` param) */
        EDIT_PATTERN: '/vendors/:id/edit',
        VIEW_PATTERN: '/vendors/:id',
    },

    // ── Catch-all ───────────────────────────────────────────
    NOT_FOUND: '*',
} as const

// ────────────────────────────────────────────────────────────
// Dynamic path helpers
//
// Use these when you need a concrete URL with an actual ID
// (e.g. navigation, links, redirects).
//
//   navigate(ROUTE_HELPERS.PURCHASE_EDIT(42))   → "/purchases/42/edit"
// ────────────────────────────────────────────────────────────

export const ROUTE_HELPERS = {
    // Purchases
    PURCHASE_EDIT: (id: number | string) => `/purchases/${id}/edit`,
    PURCHASE_VIEW: (id: number | string) => `/purchases/${id}`,

    // Payments
    PAYMENT_EDIT: (id: number | string) => `/payments/${id}/edit`,
    PAYMENT_VIEW: (id: number | string) => `/payments/${id}`, // payment view route

    // Partners
    PARTNER_EDIT: (id: number | string) => `/partners/${id}/edit`,
    PARTNER_VIEW: (id: number | string) => `/partners/${id}`,

    // Customers
    CUSTOMER_EDIT: (id: number | string) => `/customers/${id}/edit`,
    CUSTOMER_VIEW: (id: number | string) => `/customers/${id}`,

    // Capital
    CAPITAL_EDIT: (id: number | string) => `/capital/${id}/edit`,

    // Contracts
    CONTRACT_EDIT: (id: number | string) => `/contracts/${id}/edit`,
    CONTRACT_VIEW: (id: number | string) => `/contracts/${id}`,

    // Vendors
    VENDOR_EDIT: (id: number | string) => `/vendors/${id}/edit`,
    VENDOR_VIEW: (id: number | string) => `/vendors/${id}`,
} as const
