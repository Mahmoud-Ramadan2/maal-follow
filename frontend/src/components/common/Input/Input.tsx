import { forwardRef } from 'react'
import type { InputHTMLAttributes } from 'react'
import './Input.css'

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

interface InputProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'size'> {
    /** Visible label rendered above the field */
    label?: string
    /** Unique field name (used by forms) */
    name: string
    /** Validation error message — shows in red below the input */
    error?: string
}

// ────────────────────────────────────────────────────────────
// Component
// ────────────────────────────────────────────────────────────

/**
 * Controlled text input with label, error state, and RTL support.
 *
 * Uses `forwardRef` so it integrates with `react-hook-form`'s
 * `register()` / `Controller`.
 *
 * All CSS text alignment uses `start` / `end` which flip
 * automatically in RTL layouts.
 *
 * @example
 * ```tsx
 * <Input
 *     label={t('username')}
 *     name="username"
 *     value={username}
 *     onChange={(e) => setUsername(e.target.value)}
 *     error={errors.username}
 *     required
 * />
 * ```
 */
const Input = forwardRef<HTMLInputElement, InputProps>(
    ({ label, name, error, className, required, ...rest }, ref) => {
        const inputCls = [
            'form-field__input',
            error && 'form-field__input--error',
            className,
        ].filter(Boolean).join(' ')

        return (
            <div className="form-field">
                {label && (
                    <label htmlFor={name} className="form-field__label">
                        {label}
                        {required && <span className="form-field__required">*</span>}
                    </label>
                )}

                <input
                    ref={ref}
                    id={name}
                    name={name}
                    className={inputCls}
                    aria-invalid={!!error}
                    aria-describedby={error ? `${name}-error` : undefined}
                    required={required}
                    {...rest}
                />

                {error && (
                    <span id={`${name}-error`} className="form-field__error" role="alert">
                        {error}
                    </span>
                )}
            </div>
        )
    },
)

Input.displayName = 'Input'
export default Input

