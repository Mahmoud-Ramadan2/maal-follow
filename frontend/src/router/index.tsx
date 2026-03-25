// ============================================================
// Application Router
//
// Uses React Router v7 with `createBrowserRouter`.
// Every page is lazy-loaded via `React.lazy()` so the initial
// bundle only contains the layout shell — each page chunk is
// fetched on first navigation.
//
// Auth protection (ProtectedRoute) is NOT applied yet.
// When ready, wrap the MainLayout route's element with it.
// ============================================================

import { lazy, Suspense } from 'react'
import type { ReactNode } from 'react'
import { createBrowserRouter } from 'react-router-dom'
import { APP_ROUTES } from './routes.config'
import PageLoader from '@components/common/PageLoader'

/**
 * Wraps a lazily-imported component in `<Suspense>`.
 * Keeps the router config below clean and repetition-free.
 */
const lazyLoad = (
    factory: () => Promise<{ default: React.ComponentType }>,
): ReactNode => {
    const Component = lazy(factory)
    return (
        <Suspense fallback={<PageLoader />}>
            <Component />
        </Suspense>
    )
}

// ────────────────────────────────────────────────────────────
// Lazy page imports
// ────────────────────────────────────────────────────────────

// Layout
const MainLayout = lazy(() => import('@components/layout/MainLayout'))

// Auth
const loginPage    = () => import('@pages/auth/LoginPage')
const registerPage = () => import('@pages/auth/RegisterPage')

// Dashboard
const dashboardPage = () => import('@pages/Dashboard/DashboardPage')

// Purchases
const purchaseList       = () => import('@pages/modules/installments/purchase/PurchaseListPage')
const purchaseCreate     = () => import('@pages/modules/installments/purchase/PurchaseCreatePage')
const purchaseEdit       = () => import('@pages/modules/installments/purchase/PurchaseEditPage')
const purchaseView       = () => import('@pages/modules/installments/purchase/PurchaseViewPage')
const purchaseStatistics = () => import('@pages/modules/installments/purchase/PurchaseStatisticsPage')

// Procurement workspace
const procurementWorkspace = () => import('@pages/modules/installments/procurement/ProcurementWorkspacePage')

// Payments
const paymentList   = () => import('@pages/modules/installments/payment/PaymentListPage')
const paymentCreate = () => import('@pages/modules/installments/payment/PaymentCreatePage')
const paymentEdit   = () => import('@pages/modules/installments/payment/PaymentEditPage')
const paymentView   = () => import('@pages/modules/installments/payment/PaymentViewPage')

// Partners
const partnerList   = () => import('@pages/modules/installments/partner/PartnerListPage')
const partnerCreate = () => import('@pages/modules/installments/partner/PartnerCreatePage')
const partnerEdit   = () => import('@pages/modules/installments/partner/PartnerEditPage')
const partnerView   = () => import('@pages/modules/installments/partner/PartnerViewPage')

// Customers
const customerList   = () => import('@pages/modules/installments/customer/CustomerListPage')
const customerCreate = () => import('@pages/modules/installments/customer/CustomerCreatePage')
const customerEdit   = () => import('@pages/modules/installments/customer/CustomerEditPage')
const customerView   = () => import('@pages/modules/installments/customer/CustomerViewPage')

// Capital
const capitalList   = () => import('@pages/modules/installments/capital/CapitalListPage')
const capitalCreate = () => import('@pages/modules/installments/capital/CapitalCreatePage')
const capitalEdit   = () => import('@pages/modules/installments/capital/CapitalEditPage')

// Contracts
const contractList   = () => import('@pages/modules/installments/contract/ContractListPage')
const contractCreate = () => import('@pages/modules/installments/contract/ContractCreatePage')
const contractEdit   = () => import('@pages/modules/installments/contract/ContractEditPage')
const contractView   = () => import('@pages/modules/installments/contract/ContractViewPage')

// Vendors
const vendorList   = () => import('@pages/modules/installments/vendor/VendorListPage')
const vendorCreate = () => import('@pages/modules/installments/vendor/VendorCreatePage')
const vendorEdit   = () => import('@pages/modules/installments/vendor/VendorEditPage')
const vendorView   = () => import('@pages/modules/installments/vendor/VendorViewPage')

// 404
const notFoundPage = () => import('@pages/NotFoundPage')

// ────────────────────────────────────────────────────────────
// Router definition
// ────────────────────────────────────────────────────────────
export const router = createBrowserRouter([
    // ── Auth routes (no layout wrapper) ─────────────────────
    {
        path: APP_ROUTES.AUTH.LOGIN,
        element: lazyLoad(loginPage),
    },
    {
        path: APP_ROUTES.AUTH.REGISTER,
        element: lazyLoad(registerPage),
    },

    // ── App routes (wrapped in MainLayout) ──────────────────
    // TODO: wrap element with <ProtectedRoute> when auth is ready
    {
        element: (
            <Suspense fallback={<PageLoader />}>
                <MainLayout />
            </Suspense>
        ),
        children: [
            // Dashboard
            {
                path: APP_ROUTES.DASHBOARD,
                element: lazyLoad(dashboardPage),
            },

            // ── Procurement workspace ─────────────────────
            {
                path: APP_ROUTES.PROCUREMENT.LIST,
                element: lazyLoad(procurementWorkspace),
            },

            // ── Purchases ───────────────────────────────────
            {
                path: APP_ROUTES.PURCHASES.LIST,
                element: lazyLoad(purchaseList),
            },
            {
                path: APP_ROUTES.PURCHASES.CREATE,
                element: lazyLoad(purchaseCreate),
            },
            {
                // MUST be before VIEW_PATTERN so 'statistics' isn't treated as :id
                path: APP_ROUTES.PURCHASES.STATISTICS,
                element: lazyLoad(purchaseStatistics),
            },
            {
                path: APP_ROUTES.PURCHASES.EDIT_PATTERN,
                element: lazyLoad(purchaseEdit),
            },
            {
                path: APP_ROUTES.PURCHASES.VIEW_PATTERN,
                element: lazyLoad(purchaseView),
            },

            // ── Payments ────────────────────────────────────
            {
                path: APP_ROUTES.PAYMENTS.LIST,
                element: lazyLoad(paymentList),
            },
            {
                path: APP_ROUTES.PAYMENTS.CREATE,
                element: lazyLoad(paymentCreate),
            },
            {
                path: APP_ROUTES.PAYMENTS.EDIT_PATTERN,
                element: lazyLoad(paymentEdit),
            },
            {
                path: APP_ROUTES.PAYMENTS.VIEW_PATTERN,
                element: lazyLoad(paymentView),
            },

            // ── Partners ────────────────────────────────────
            {
                path: APP_ROUTES.PARTNERS.LIST,
                element: lazyLoad(partnerList),
            },
            {
                path: APP_ROUTES.PARTNERS.CREATE,
                element: lazyLoad(partnerCreate),
            },
            {
                path: APP_ROUTES.PARTNERS.EDIT_PATTERN,
                element: lazyLoad(partnerEdit),
            },
            {
                path: APP_ROUTES.PARTNERS.VIEW_PATTERN,
                element: lazyLoad(partnerView),
            },

            // ── Customers ───────────────────────────────────
            {
                path: APP_ROUTES.CUSTOMERS.LIST,
                element: lazyLoad(customerList),
            },
            {
                path: APP_ROUTES.CUSTOMERS.CREATE,
                element: lazyLoad(customerCreate),
            },
            {
                path: APP_ROUTES.CUSTOMERS.EDIT_PATTERN,
                element: lazyLoad(customerEdit),
            },
            {
                path: APP_ROUTES.CUSTOMERS.VIEW_PATTERN,
                element: lazyLoad(customerView),
            },

            // ── Capital ─────────────────────────────────────
            {
                path: APP_ROUTES.CAPITAL.LIST,
                element: lazyLoad(capitalList),
            },
            {
                path: APP_ROUTES.CAPITAL.CREATE,
                element: lazyLoad(capitalCreate),
            },
            {
                path: APP_ROUTES.CAPITAL.EDIT_PATTERN,
                element: lazyLoad(capitalEdit),
            },

            // ── Contracts ───────────────────────────────────
            {
                path: APP_ROUTES.CONTRACTS.LIST,
                element: lazyLoad(contractList),
            },
            {
                path: APP_ROUTES.CONTRACTS.CREATE,
                element: lazyLoad(contractCreate),
            },
            {
                path: APP_ROUTES.CONTRACTS.EDIT_PATTERN,
                element: lazyLoad(contractEdit),
            },
            {
                path: APP_ROUTES.CONTRACTS.VIEW_PATTERN,
                element: lazyLoad(contractView),
            },

            // ── Vendors ─────────────────────────────────
            {
                path: APP_ROUTES.VENDORS.LIST,
                element: lazyLoad(vendorList),
            },
            {
                path: APP_ROUTES.VENDORS.CREATE,
                element: lazyLoad(vendorCreate),
            },
            {
                path: APP_ROUTES.VENDORS.EDIT_PATTERN,
                element: lazyLoad(vendorEdit),
            },
            {
                path: APP_ROUTES.VENDORS.VIEW_PATTERN,
                element: lazyLoad(vendorView),
            },

            // ── 404 catch-all ───────────────────────────────
            {
                path: APP_ROUTES.NOT_FOUND,
                element: lazyLoad(notFoundPage),
            },
        ],
    },
])

