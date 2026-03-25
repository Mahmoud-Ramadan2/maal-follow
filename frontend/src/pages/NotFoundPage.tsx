import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { APP_ROUTES } from '@/router/routes.config'

/**
 * 404 — displayed when no route matches.
 */
export default function NotFoundPage(): ReactNode {
    return (
        <div className="flex flex-col items-center justify-center min-h-screen gap-4">
            <h1 className="text-4xl font-bold">404</h1>
            <p className="text-secondary text-lg">Page not found</p>
            <Link to={APP_ROUTES.DASHBOARD} className="text-primary">
                Back to Dashboard
            </Link>
        </div>
    )
}

