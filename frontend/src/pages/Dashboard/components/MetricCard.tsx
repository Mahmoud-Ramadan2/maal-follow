import { formatNumberCompact } from '@/utils/formatters/number'
import type { ReactNode } from 'react'

interface MetricCardProps {
    id?: string
    title: string
    value: number | null | undefined
    subtitle?: string
    accent?: 'emerald' | 'slate' | 'amber' | 'rose'
    progress?: number
    progressLabel?: string
    badge?: { text: string; variant: 'success' | 'danger' | 'warning' | 'neutral' }
    children?: ReactNode
}

const accentMap = {
    emerald: 'text-emerald-600 dark:text-emerald-400',
    slate: 'text-slate-600 dark:text-slate-400',
    amber: 'text-amber-600 dark:text-amber-400',
    rose: 'text-rose-600 dark:text-rose-400',
}

const borderAccentMap = {
    emerald: 'border-l-emerald-500',
    slate: 'border-l-slate-400',
    amber: 'border-l-amber-500',
    rose: 'border-l-rose-500',
}

const badgeMap = {
    success: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400',
    danger: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
    warning: 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400',
    neutral: 'bg-muted text-muted-foreground',
}

const progressColorMap = {
    emerald: 'bg-emerald-500',
    slate: 'bg-slate-400',
    amber: 'bg-amber-500',
    rose: 'bg-rose-500',
}

export default function MetricCard({ id, title, value, subtitle, accent = 'slate', progress, progressLabel, badge, children }: MetricCardProps) {
    return (
        <div id={id} className={`rounded-xl border border-border/60 bg-card p-4 shadow-sm border-l-4 ${borderAccentMap[accent]}`} aria-label={title}>
            <div className="flex items-start justify-between gap-2">
                <div className="text-xs font-medium uppercase tracking-wider text-muted-foreground">{title}</div>
                {badge && (
                    <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-semibold leading-none ${badgeMap[badge.variant]}`}>
                        {badge.text}
                    </span>
                )}
            </div>
            <div className={`mt-1 text-2xl font-bold tabular-nums ${accentMap[accent]}`}>
                {value == null ? '-' : formatNumberCompact(value)}
            </div>
            {subtitle && (
                <div className="mt-0.5 text-xs text-muted-foreground">{subtitle}</div>
            )}
            {progress !== undefined && (
                <div className="mt-3">
                    <div className="flex items-center justify-between text-[10px] text-muted-foreground">
                        <span>{progressLabel ?? `${progress}%`}</span>
                        <span>{progress}%</span>
                    </div>
                    <div className="mt-1 h-1.5 w-full overflow-hidden rounded-full bg-muted">
                        <div
                            className={`h-full rounded-full transition-all duration-500 ${progressColorMap[accent]}`}
                            style={{ width: `${Math.min(Math.max(progress, 0), 100)}%` }}
                        />
                    </div>
                </div>
            )}
            {children && <div className="mt-2 border-t border-border/40 pt-2">{children}</div>}
        </div>
    )
}
