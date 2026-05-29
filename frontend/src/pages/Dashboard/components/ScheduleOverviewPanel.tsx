import { useMemo } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import { Card } from '@/components/ui'
import Button from '@/components/common/Button'
import { ROUTE_HELPERS, APP_ROUTES } from '@/router/routes.config'
import type { InstallmentSchedule } from '@/types/modules/schedule.types'
import { formatCurrency } from '@/utils/formatters/number'
import { severityFromOverdue } from './ChartUtils'

interface ScheduleOverviewPanelProps {
    title: string
    schedules: InstallmentSchedule[]
    emptyMessage: string
    retryLabel?: string
    onRetry?: () => void
    duePrefix: string
    sequencePrefix: string
    isOverduePanel?: boolean
}

function statusBadgeClass(status: string): string {
    switch (status) {
        case 'LATE':
            return 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'
        case 'PAID':
            return 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400'
        case 'PARTIALLY_PAID':
            return 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400'
        case 'PENDING':
            return 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400'
        case 'CANCELLED':
            return 'bg-gray-100 text-gray-600 dark:bg-gray-800/40 dark:text-gray-400'
        default:
            return 'bg-muted text-muted-foreground'
    }
}

export default function ScheduleOverviewPanel({
    title,
    schedules,
    emptyMessage,
    retryLabel,
    onRetry,
    duePrefix,
    sequencePrefix,
    isOverduePanel = false,
}: ScheduleOverviewPanelProps) {
    const { t } = useTranslation('dashboard')
    const navigate = useNavigate()
    const visibleSchedules = schedules.slice(0, 5)

    const totalAmount = useMemo(
        () => schedules.reduce((sum, s) => sum + (s.amount ?? 0), 0),
        [schedules],
    )

    const severity = useMemo(
        () => severityFromOverdue(schedules.filter((s) => s.status === 'LATE').length),
        [schedules],
    )

    const handlePay = (schedule: InstallmentSchedule) => {
        const params = new URLSearchParams({
            scheduleId: String(schedule.id ?? ''),
            contractId: String(schedule.contractId),
            amount: String(schedule.amount),
            customerName: schedule.customerName,
        })
        navigate(`${APP_ROUTES.PAYMENTS.CREATE}?${params.toString()}`)
    }

    const handleView = (schedule: InstallmentSchedule) => {
        if (schedule.id) {
            navigate(ROUTE_HELPERS.SCHEDULE_VIEW(schedule.id))
        } else {
            navigate(ROUTE_HELPERS.CONTRACT_VIEW(schedule.contractId))
        }
    }

    return (
        <Card className="h-full">
            <div className="flex h-full flex-col gap-3">
                <div className="flex items-start justify-between gap-3">
                    <div className="flex items-center gap-2 min-w-0">
                        <h3 className="text-sm font-medium text-muted-foreground">{title}</h3>
                        {schedules.length > 0 && (
                            <span
                                className={`inline-flex items-center justify-center min-w-[20px] h-5 rounded-full px-1.5 text-[11px] font-semibold leading-none ${
                                    isOverduePanel
                                        ? 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'
                                        : 'bg-primary/10 text-primary'
                                }`}
                            >
                                {schedules.length}
                            </span>
                        )}
                    </div>
                    <div className="flex items-center gap-2">
                        {schedules.length > 0 && isOverduePanel && (
                            <span
                                className={`inline-flex items-center rounded-full px-2 py-0.5 text-[10px] font-semibold leading-none ${
                                    severity.variant === 'danger'
                                        ? 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'
                                        : severity.variant === 'warning'
                                            ? 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400'
                                            : 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400'
                                }`}
                            >
                                {t(severity.text).toUpperCase()}
                            </span>
                        )}
                        {onRetry && retryLabel ? (
                            <button
                                type="button"
                                className="shrink-0 text-sm font-medium text-primary-600 transition-colors hover:text-primary-700"
                                onClick={onRetry}
                            >
                                {retryLabel}
                            </button>
                        ) : null}
                    </div>
                </div>

                <div className="space-y-2">
                    {visibleSchedules.map((schedule) => (
                        <div
                            key={schedule.id ?? `${schedule.contractId}-${schedule.sequenceNumber}`}
                            className={`rounded-lg border p-3 transition-colors ${
                                isOverduePanel && schedule.status === 'LATE'
                                    ? 'border-red-200 bg-red-50/40 dark:border-red-900/30 dark:bg-red-900/10'
                                    : 'border-border/60 bg-background/50 hover:bg-muted/30'
                            }`}
                        >
                            <div className="flex items-center justify-between gap-3">
                                <div className="min-w-0 flex-1">
                                    <div className={`truncate font-medium ${isOverduePanel && schedule.status === 'LATE' ? 'text-red-800 dark:text-red-300' : ''}`}>
                                        {schedule.customerName}
                                    </div>
                                    <div className="mt-0.5 flex flex-wrap gap-x-3 gap-y-1 text-xs text-muted-foreground">
                                        <span>{duePrefix} {schedule.dueDate}</span>
                                        <span>{sequencePrefix} #{schedule.sequenceNumber}</span>
                                    </div>
                                </div>
                                <div className="flex items-center gap-2">
                                    <span className="text-sm font-semibold tabular-nums">{formatCurrency(schedule.amount)}</span>
                                    <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-[11px] font-semibold leading-none ${statusBadgeClass(schedule.status)}`}>
                                        {schedule.status === 'PARTIALLY_PAID' ? 'PARTIAL' : schedule.status}
                                    </span>
                                </div>
                            </div>
                            {schedule.status !== 'PAID' && schedule.status !== 'CANCELLED' && (
                                <div className="mt-2 flex gap-2 border-t border-border/40 pt-2">
                                    <Button variant={schedule.status === 'LATE' ? 'danger' : 'primary'} size="sm" onClick={() => handlePay(schedule)}>
                                        {t('actionPay')}
                                    </Button>
                                    <Button variant="secondary" size="sm" onClick={() => handleView(schedule)}>
                                        {t('actionView')}
                                    </Button>
                                </div>
                            )}
                        </div>
                    ))}

                    {visibleSchedules.length === 0 ? (
                        <div className="flex flex-col items-center justify-center gap-1.5 rounded-lg border border-dashed border-border/40 py-8 text-sm text-muted-foreground">
                            <svg className="h-8 w-8 text-muted-foreground/40" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                                <path strokeLinecap="round" strokeLinejoin="round" d="M9 12h6m-3-3v6m-7 4h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                            </svg>
                            <span>{emptyMessage}</span>
                        </div>
                    ) : null}
                </div>

                {schedules.length > 0 && (
                    <div className="mt-auto flex items-center justify-between border-t border-border/40 pt-3 text-xs text-muted-foreground">
                        <span>
                            {t('showingCount', { shown: Math.min(schedules.length, 5), total: schedules.length })}
                        </span>
                        <span className="font-semibold tabular-nums" style={{ color: 'var(--text-primary)' }}>
                            {formatCurrency(totalAmount)}
                        </span>
                    </div>
                )}
            </div>
        </Card>
    )
}
