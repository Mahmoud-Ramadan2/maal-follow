import type { ReactNode } from 'react'
import './LoadingSpinner.css'

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

interface LoadingSpinnerProps {
    /** Spinner diameter. @default 'md' */
    size?: 'sm' | 'md' | 'lg'
    /**
     * When `true` the spinner inherits color from its parent
     * (useful inside colored buttons). @default false
     */
    inheritColor?: boolean
    /**
     * When `true` the spinner is centered in a full-width
     * container with min-height. @default false
     */
    fullPage?: boolean
}

// ────────────────────────────────────────────────────────────
// Component
// ────────────────────────────────────────────────────────────

/**
 * Animated circular spinner.
 *
 * @example
 * ```tsx
 * <LoadingSpinner />
 * <LoadingSpinner size="lg" fullPage />
 * <LoadingSpinner size="sm" inheritColor />   // inside a button
 * ```
 */
export default function LoadingSpinner({
    size = 'md',
    inheritColor = false,
    fullPage = false,
}: LoadingSpinnerProps): ReactNode {
    const containerCls = [
        'spinner-container',
        fullPage && 'spinner-container--fullpage',
    ].filter(Boolean).join(' ')

    const spinnerCls = [
        'spinner',
        `spinner--${size}`,
        inheritColor && 'spinner--inherit',
    ].filter(Boolean).join(' ')

    return (
        <div className={containerCls} role="status" aria-label="Loading">
            <div className={spinnerCls} />
        </div>
    )
}

