import { useTranslation } from 'react-i18next'
import { NavLink, useLocation } from 'react-router-dom'
import { APP_ROUTES } from '@/router/routes.config'
import DashboardIcon from '@mui/icons-material/Dashboard'
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart'
import PaymentIcon from '@mui/icons-material/Payment'
import HandshakeIcon from '@mui/icons-material/Handshake'
import AccountBalanceIcon from '@mui/icons-material/AccountBalance'
import PeopleIcon from '@mui/icons-material/People'
import DescriptionIcon from '@mui/icons-material/Description'
import './Sidebar.css'

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

interface NavItem {
    /** Translation key under `common:nav.*` */
    labelKey: string
    /** Route path */
    path: string
    /** MUI icon element */
    icon: React.ReactNode
}

interface SidebarProps {
    /** Whether the sidebar is open on mobile */
    isOpen: boolean
    /** Called when the user closes the sidebar (tap overlay or link) */
    onClose: () => void
}

// ────────────────────────────────────────────────────────────
// Navigation items — single source of truth
// ────────────────────────────────────────────────────────────

const NAV_ITEMS: NavItem[] = [
    { labelKey: 'nav.dashboard',  path: APP_ROUTES.DASHBOARD,       icon: <DashboardIcon fontSize="small" /> },
    { labelKey: 'nav.procurement', path: APP_ROUTES.PROCUREMENT.LIST, icon: <ShoppingCartIcon fontSize="small" /> },
    { labelKey: 'nav.payments',   path: APP_ROUTES.PAYMENTS.LIST,   icon: <PaymentIcon fontSize="small" /> },
    { labelKey: 'nav.partners',   path: APP_ROUTES.PARTNERS.LIST,   icon: <HandshakeIcon fontSize="small" /> },
    { labelKey: 'nav.capital',    path: APP_ROUTES.CAPITAL.LIST,     icon: <AccountBalanceIcon fontSize="small" /> },
    { labelKey: 'nav.customers',  path: APP_ROUTES.CUSTOMERS.LIST,  icon: <PeopleIcon fontSize="small" /> },
    { labelKey: 'nav.contracts',  path: APP_ROUTES.CONTRACTS.LIST,   icon: <DescriptionIcon fontSize="small" /> },
]

// ────────────────────────────────────────────────────────────
// Component
// ────────────────────────────────────────────────────────────

/**
 * Side navigation panel.
 *
 * - Seven module links with MUI icons
 * - Active link is highlighted with a colored start-border
 * - Collapsible on mobile (controlled by `isOpen` prop)
 * - Fully RTL-safe via CSS logical properties
 */
export default function Sidebar({ isOpen, onClose }: SidebarProps) {
    const { t } = useTranslation()
    const location = useLocation()

    /**
     * Check if a nav item should be marked active.
     * Dashboard is exact-match only; module links match
     * any sub-path (e.g. /purchases/create is still "Purchases").
     */
    const isActive = (path: string): boolean => {
        if (path === APP_ROUTES.PROCUREMENT.LIST) {
            return (
                location.pathname.startsWith(APP_ROUTES.PROCUREMENT.LIST)
                || location.pathname.startsWith(APP_ROUTES.PURCHASES.LIST)
                || location.pathname.startsWith(APP_ROUTES.VENDORS.LIST)
            )
        }
        if (path === '/') return location.pathname === '/'
        return location.pathname.startsWith(path)
    }

    return (
        <>
            {/* Mobile overlay — closes sidebar on tap */}
            <div
                className={`sidebar__overlay ${isOpen ? 'sidebar__overlay--visible' : ''}`}
                onClick={onClose}
                aria-hidden="true"
            />

            <aside className={`sidebar ${isOpen ? 'sidebar--open' : ''}`}>
                <nav className="sidebar__nav">
                    <ul className="sidebar__list">
                        {NAV_ITEMS.map((item) => (
                            <li key={item.path}>
                                <NavLink
                                    to={item.path}
                                    end={item.path === '/'}
                                    className={
                                        `sidebar__link ${isActive(item.path) ? 'sidebar__link--active' : ''}`
                                    }
                                    onClick={onClose}
                                >
                                    <span className="sidebar__icon">{item.icon}</span>
                                    <span>{t(item.labelKey)}</span>
                                </NavLink>
                            </li>
                        ))}
                    </ul>
                </nav>
            </aside>
        </>
    )
}

