import type { ReactNode, ButtonHTMLAttributes } from 'react'
import LoadingSpinner from '@components/ui/LoadingSpinner'
import './Button.css'

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

interface ButtonProps extends Omit<ButtonHTMLAttributes<HTMLButtonElement>, 'type'> {
    /** Button content */
    children: ReactNode
    /** HTML button type. @default 'button' */
    type?: 'button' | 'submit' | 'reset'
    /** Visual style. @default 'primary' */
    variant?: 'primary' | 'secondary' | 'danger'
    /** Height / padding preset. @default 'md' */
    size?: 'sm' | 'md' | 'lg'
    /** Disables the button and shows a spinner. */
    loading?: boolean
    /** Stretch to full container width. */
    fullWidth?: boolean
}

// ────────────────────────────────────────────────────────────
// Component
// ────────────────────────────────────────────────────────────

/**
 * Reusable button with variant, size, and loading state.
 *
 * @example
 * ```tsx
 * <Button onClick={handleSave}>Save</Button>
 * <Button variant="danger" size="sm" onClick={handleDelete}>Delete</Button>
 * <Button loading disabled>Submitting…</Button>
 * ```
 */
export default function Button({
    children,
    type = 'button',
    variant = 'primary',
    size = 'md',
    loading = false,
    fullWidth = false,
    disabled,
    className,
    ...rest
}: ButtonProps): ReactNode {
    const cls = [
        'btn',
        `btn--${variant}`,
        `btn--${size}`,
        loading && 'btn--loading',
        fullWidth && 'btn--full',
        className,
    ].filter(Boolean).join(' ')

    return (
        <button
            type={type}
            className={cls}
            disabled={disabled || loading}
            {...rest}
        >
            {loading && <LoadingSpinner size="sm" inheritColor />}
            {children}
        </button>
    )
}

