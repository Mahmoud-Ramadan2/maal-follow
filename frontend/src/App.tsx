import { RouterProvider } from 'react-router-dom'
import { ToastContainer } from 'react-toastify'
import 'react-toastify/dist/ReactToastify.css'

// ── Config (must be imported before anything uses i18n) ─────
import './config/i18n.config'

// ── Styles ──────────────────────────────────────────────────
import './styles/index.css'

// ── Providers ───────────────────────────────────────────────
import { AuthProvider, LanguageProvider, ThemeProvider, useAuth, useLanguage, useTheme } from './contexts'
import PageLoader from '@components/common/PageLoader'

// ── Router ──────────────────────────────────────────────────
import { router } from './router'

// ────────────────────────────────────────────────────────────
// Inner app shell — lives inside the providers so
// useLanguage() and useTheme() can read global UI state.
// ────────────────────────────────────────────────────────────
function AppShell() {
    const { direction } = useLanguage()
    const { resolvedTheme } = useTheme()
    const { isLoading } = useAuth()

    if (isLoading) {
        return (
            <>
                <PageLoader />

                <ToastContainer
                    position={direction === 'rtl' ? 'top-left' : 'top-right'}
                    autoClose={3000}
                    hideProgressBar={false}
                    newestOnTop
                    closeOnClick
                    rtl={direction === 'rtl'}
                    pauseOnFocusLoss
                    draggable
                    pauseOnHover
                    theme={resolvedTheme}
                />
            </>
        )
    }

    return (
        <>
            <RouterProvider router={router} />

            <ToastContainer
                position={direction === 'rtl' ? 'top-left' : 'top-right'}
                autoClose={3000}
                hideProgressBar={false}
                newestOnTop
                closeOnClick
                rtl={direction === 'rtl'}
                pauseOnFocusLoss
                draggable
                pauseOnHover
                theme={resolvedTheme}
            />
        </>
    )
}

// ────────────────────────────────────────────────────────────
// Root component — provider wrapper order:
//
//   1. AuthProvider     → user state, login/logout
//   2. ThemeProvider    → light/dark/system mode
//   3. LanguageProvider → language + dir on <html>
//   4. AppShell         → router + theme-aware toast
//
// AuthProvider stays outermost so auth/session bootstrap runs
// before the rest of the app tries to render protected content.
// ThemeProvider is kept above LanguageProvider so the shell can
// read both UI systems without coupling them together.
// ────────────────────────────────────────────────────────────
export default function App() {
    return (
        <AuthProvider>
            <ThemeProvider>
                <LanguageProvider>
                    <AppShell />
                </LanguageProvider>
            </ThemeProvider>
        </AuthProvider>
    )
}
