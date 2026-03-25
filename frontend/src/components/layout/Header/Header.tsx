import { useTranslation } from 'react-i18next'
import { useLanguage } from '@contexts/useLanguage'
import { Link } from 'react-router-dom'
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

                {/* User menu placeholder — will wire to AuthContext later */}
                <button className="header__user-btn">
                    <span className="header__user-avatar">U</span>
                    <span>{t('header.profile')}</span>
                </button>
            </div>
        </header>
    )
}

