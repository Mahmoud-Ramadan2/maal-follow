import type { ReactNode } from 'react'
import { classNames } from '@utils/helpers/classNames'
import './Badge.css'

export interface BadgeProps {
    children: ReactNode
    variant?: 'neutral' | 'primary' | 'success' | 'warning' | 'danger' | 'info'
    size?: 'sm' | 'md'
    className?: string
}

export default function Badge({
    children,
    variant = 'neutral',
    size = 'md',
    className,
}: BadgeProps): ReactNode {
    return (
        <span className={classNames('badge', `badge--${variant}`, `badge--${size}`, className)}>
            {children}
        </span>
    )
}

