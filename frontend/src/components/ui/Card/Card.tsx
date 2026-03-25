import type { ReactNode } from 'react'
import './Card.css'

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

interface CardProps {
    /** Optional header title (string or JSX element) */
    title?: ReactNode
    /** Card body content */
    children: ReactNode
    /** Optional footer — typically action buttons */
    footer?: ReactNode
    /** When provided the entire card becomes clickable */
    onClick?: () => void
    /** Extra CSS class names */
    className?: string
}

// ────────────────────────────────────────────────────────────
// Component
// ────────────────────────────────────────────────────────────

/**
 * Generic content card with optional header and footer.
 *
 * @example
 * ```tsx
 * <Card title="Monthly Revenue">
 *     <p>$12,340</p>
 * </Card>
 *
 * <Card
 *     title="Purchase #42"
 *     footer={<Button size="sm">View Details</Button>}
 *     onClick={() => navigate('/purchases/42')}
 * >
 *     <p>Amount: $500</p>
 * </Card>
 * ```
 */
export default function Card({ title, children, footer, onClick, className }: CardProps): ReactNode {
    const cls = [
        'card',
        onClick && 'card--clickable',
        className,
    ].filter(Boolean).join(' ')

    return (
        <div
            className={cls}
            onClick={onClick}
            role={onClick ? 'button' : undefined}
            tabIndex={onClick ? 0 : undefined}
            onKeyDown={onClick ? (e) => { if (e.key === 'Enter') onClick() } : undefined}
        >
            {title && (
                <div className="card__header">
                    <h3 className="card__title">{title}</h3>
                </div>
            )}

            <div className="card__body">
                {children}
            </div>

            {footer && (
                <div className="card__footer">
                    {footer}
                </div>
            )}
        </div>
    )
}

