import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { APP_ROUTES } from '@/router/routes.config'

export default function ForbiddenPage(): ReactNode {
    return (
        <div className="flex min-h-screen flex-col items-center justify-center gap-4">
            <h1 className="text-4xl font-bold">403</h1>
            <p className="text-lg text-secondary">You do not have permission to view this page.</p>
            <Link to={APP_ROUTES.DASHBOARD} className="text-primary">
                Back to Dashboard
            </Link>
        </div>
    )
}

