import { RouterProvider } from 'react-router-dom'
import { ToastContainer } from 'react-toastify'
import 'react-toastify/dist/ReactToastify.css'

// ── Config (must be imported before anything uses i18n) ─────
import './config/i18n.config'

// ── Styles ──────────────────────────────────────────────────
import './styles/index.css'

// ── Providers ───────────────────────────────────────────────
import { AuthProvider, LanguageProvider, useLanguage } from '@contexts/index'

// ── Router ──────────────────────────────────────────────────
import { router } from './router'

// ────────────────────────────────────────────────────────────
// Inner app shell — lives INSIDE LanguageProvider so
// useLanguage() can read the current direction for the toast.
// ────────────────────────────────────────────────────────────
function AppShell() {
    const { direction } = useLanguage()

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
                theme="colored"
            />
        </>
    )
}

// ────────────────────────────────────────────────────────────
// Root component — provider wrapper order:
//
//   1. AuthProvider     → user state, login/logout
//   2. LanguageProvider → language + dir on <html>
//   3. AppShell         → router + RTL-aware toast
//
// AuthProvider is outermost so the interceptor (which clears
// the token on 401) doesn't depend on i18n being ready.
// LanguageProvider wraps AppShell so useLanguage() works
// inside the toast configuration.
// ────────────────────────────────────────────────────────────
export default function App() {
    return (
        <AuthProvider>
            <LanguageProvider>
                <AppShell />
            </LanguageProvider>
        </AuthProvider>
    )
}

