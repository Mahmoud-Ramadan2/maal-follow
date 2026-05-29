import { useEffect, useMemo, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useLanguage } from '@contexts/useLanguage'
import { Link } from 'react-router-dom'
import { useAuth } from '@contexts/useAuth'
import { APP_ROUTES } from '@/router/routes.config'
import './Header.css'

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

interface HeaderProps {
    /** Called when the mobile hamburger button is pressed */
    onMobileMenuToggle: () => void
}

// ────────────────────────────────────────────────────────────
// Component
// ────────────────────────────────────────────────────────────

/**
 * Top application bar.
 *
 * - Logo + app name (links to dashboard)
 * - Language switcher (EN / AR)
 * - User menu placeholder (auth will be wired later)
 * - Hamburger button for mobile sidebar toggle
 */
export default function Header({ onMobileMenuToggle }: HeaderProps) {
    const { t } = useTranslation()
    const { language, changeLanguage } = useLanguage()
    const { user, logout, logoutAll } = useAuth()
    const [menuOpen, setMenuOpen] = useState(false)
    const menuRef = useRef<HTMLDivElement>(null)

    useEffect(() => {
        const handleOutsideClick = (event: MouseEvent) => {
            if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
                setMenuOpen(false)
            }
        }

        document.addEventListener('mousedown', handleOutsideClick)
        return () => document.removeEventListener('mousedown', handleOutsideClick)
    }, [])

    const userInitials = useMemo(() => {
        const source = user?.fullName?.trim() || user?.username?.trim() || 'U'
        return source
            .split(/\s+/)
            .slice(0, 2)
            .map((part) => part[0]?.toUpperCase() ?? '')
            .join('')
            .slice(0, 2) || 'U'
    }, [user])

    const handleLogout = async () => {
        setMenuOpen(false)
        await logout()
    }

    const handleLogoutAll = async () => {
        setMenuOpen(false)
        await logoutAll()
    }

    return (
        <header className="header">
            {/* ── Start: hamburger + logo ─────────────── */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-3)' }}>
                {/* Mobile hamburger */}
                <button
                    className="header__mobile-toggle"
                    onClick={onMobileMenuToggle}
                    aria-label="Toggle menu"
                >
                    <div className="header__hamburger">
                        <span />
                        <span />
                        <span />
                    </div>
                </button>

                {/* Logo */}
                <Link to={APP_ROUTES.DASHBOARD} className="header__logo">
                    <img
                        src="/logo.png"
                        alt="MaalFlow"
                        className="header__logo-img"
                    />
                    <span className="header__logo-text">
                        {t('appName')}
                    </span>
                </Link>
            </div>

            {/* ── End: actions ────────────────────────── */}
            <div className="header__actions">
                {/* Language switcher */}
                <button
                    className={`header__lang-btn ${language === 'en' ? 'header__lang-btn--active' : ''}`}
                    onClick={() => changeLanguage('en')}
                >
                    EN
                </button>
                <button
                    className={`header__lang-btn ${language === 'ar' ? 'header__lang-btn--active' : ''}`}
                    onClick={() => changeLanguage('ar')}
                >
                    AR
                </button>

                <div className="header__user-menu" ref={menuRef}>
                    <button
                        className="header__user-btn"
                        onClick={() => setMenuOpen((prev) => !prev)}
                        aria-expanded={menuOpen}
                        aria-haspopup="menu"
                    >
                        <span className="header__user-avatar">{userInitials}</span>
                        <span>{user?.fullName || user?.username || t('header.profile')}</span>
                    </button>

                    {menuOpen && (
                        <div className="header__user-menu-panel" role="menu">
                            <div className="header__user-menu-meta">
                                <strong>{user?.fullName || user?.username}</strong>
                                <span>{user?.email}</span>
                            </div>

                            <div className="header__user-menu-actions">
                                <button className="header__user-menu-action" onClick={handleLogout}>
                                    {t('header.logout')}
                                </button>
                                <button
                                    className="header__user-menu-action header__user-menu-action--danger"
                                    onClick={handleLogoutAll}
                                >
                                    {t('header.logoutAll')}
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </header>
    )
}

