import type { ReactNode } from 'react'

/**
 * Full-page loading indicator shown while a lazy-loaded
 * route chunk is being downloaded.
 */
export default function PageLoader(): ReactNode {
    return (
        <div className="loading">
            <p>Loading…</p>
        </div>
    )
}

