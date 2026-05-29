import { useEffect, useState, useCallback, useRef } from 'react'
import { useTranslation } from 'react-i18next'
import { dashboardApi } from '@/services/api/modules/dashboard.api'
import type { DashboardOverview } from '@/types/modules/dashboard.types'
import DashboardHeader from './components/DashboardHeader'
import DashboardSkeleton from './components/DashboardSkeleton'
import KpiRow from './components/KpiRow'
import CollectionsChart from './components/CollectionsChart'
import CollectionPieChart from './components/CollectionPieChart'
import CapitalUtilizationChart from './components/CapitalUtilizationChart'
import LedgerDailyChart from './components/LedgerDailyChart'
import ScheduleOverviewPanel from './components/ScheduleOverviewPanel'

export default function DashboardPage() {
    const { t } = useTranslation('dashboard')
    const now = new Date()
    const [year, setYear] = useState(now.getFullYear())
    const [month, setMonth] = useState(now.getMonth() + 1)

    const [overview, setOverview] = useState<DashboardOverview | null>(null)
    const [chartDataReady, setChartDataReady] = useState(false)
    const [criticalLoading, setCriticalLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const latestReqId = useRef(0)

    const loadData = useCallback((y: number, m: number) => {
        const reqId = ++latestReqId.current
        const controller = new AbortController()

        const params = { year: y, month: m }
        setChartDataReady(false)
        setError(null)

        const critical = dashboardApi.getOverview(params, { criticalOnly: true })
            .then((data) => {
                if (reqId !== latestReqId.current || controller.signal.aborted) return
                if (data.errors?.length && data.errors.length >= 5) {
                    setError(t('loadError'))
                    setCriticalLoading(false)
                    return
                }
                setOverview(data)
                setCriticalLoading(false)
            })

        const secondary = critical.then(() =>
            dashboardApi.getOverview(params)
                .then((full) => {
                    if (reqId !== latestReqId.current || controller.signal.aborted) return
                    setOverview((prev) => prev ? { ...prev, ...full, trends: full.trends, errors: full.errors } : full)
                    setChartDataReady(true)
                }),
        )

        secondary.catch((err) => {
            if (reqId !== latestReqId.current) return
            if ((err as Error)?.name === 'AbortError') return
            console.error('Failed to load dashboard data', err)
            setError(t('loadError'))
            setCriticalLoading(false)
        })

        return controller
    }, [t])

    useEffect(() => {
        setOverview(null)
        const controller = loadData(year, month)
        return () => { controller.abort() }
    }, [year, month, loadData])

    const handleYearChange = useCallback((y: number) => {
        setYear(y)
    }, [])

    const handleMonthChange = useCallback((m: number) => {
        setMonth(m)
    }, [])

    const handleRefresh = useCallback(() => {
        setOverview(null)
        loadData(year, month)
    }, [year, month, loadData])

    if (error && !overview) return (
        <div className="flex h-64 items-center justify-center p-4 sm:p-6">
            <div className="text-center">
                <p className="text-muted-foreground">{error}</p>
                <button
                    type="button"
                    className="mt-3 rounded-lg border border-border/60 bg-background px-4 py-2 text-sm font-medium transition-colors hover:bg-muted/50"
                    onClick={handleRefresh}
                >
                    {t('retry')}
                </button>
            </div>
        </div>
    )

    if (!overview) return <DashboardSkeleton />

    const { finance, operations, trends, errors } = overview

    const customerCount = Object.values(finance.customerStats || {}).reduce((sum, v) => sum + v, 0)

    const { expectedAmount = 0, actualAmount = 0, shortfall = 0 } = trends.monthlyCollectionSummary

    const collectionRate = finance.paymentStatistics.collectionRate ?? 0
    const overdueTotal = (operations.overdueSchedules ?? []).reduce((sum, s) => sum + (s.amount ?? 0), 0)
    const dueSoonTotal = (operations.dueSoonSchedules ?? []).reduce((sum, s) => sum + (s.amount ?? 0), 0)

    const pool = finance.currentCapitalPool

    const isSecondaryLoading = !chartDataReady
    const chartSkeleton = (
        <div className="flex h-56 items-center justify-center rounded-xl border border-border/60 bg-card p-4">
            <div className="h-6 w-6 animate-spin rounded-full border-2 border-primary/30 border-t-primary" />
        </div>
    )

    return (
        <div className="space-y-4 p-4 sm:p-6">
            {errors && errors.length > 0 && (
                <div className="rounded-lg border border-amber-200 bg-amber-50 p-3 text-xs text-amber-800 dark:border-amber-800/40 dark:bg-amber-900/20 dark:text-amber-300">
                    <span className="font-medium">Some data failed to load:</span>{' '}
                    {errors.join(', ')}.{' '}
                    <button type="button" className="underline hover:no-underline" onClick={handleRefresh}>
                        {t('retry')}
                    </button>
                </div>
            )}

            <DashboardHeader
                year={year}
                month={month}
                generatedAt={overview.generatedAt}
                onYearChange={handleYearChange}
                onMonthChange={handleMonthChange}
                onRefresh={handleRefresh}
                loading={criticalLoading || isSecondaryLoading}
            />

            {criticalLoading ? (
                <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 sm:gap-4 xl:grid-cols-4">
                    {Array.from({ length: 4 }).map((_, i) => (
                        <div key={i} className="rounded-xl border border-border/60 bg-card p-4 shadow-sm">
                            <div className="h-3 w-24 animate-pulse rounded bg-muted" />
                            <div className="mt-3 h-7 w-28 animate-pulse rounded bg-muted" />
                            <div className="mt-3 h-3 w-32 animate-pulse rounded bg-muted" />
                            <div className="mt-2 h-1.5 w-full animate-pulse rounded-full bg-muted" />
                        </div>
                    ))}
                </div>
            ) : (
                <KpiRow
                    monthlyCollected={actualAmount}
                    monthlyExpected={expectedAmount}
                    collectionRate={collectionRate}
                    availableAmount={pool?.availableAmount ?? 0}
                    shortfall={shortfall}
                    overdueTotal={overdueTotal}
                    dueSoonTotal={dueSoonTotal}
                    customerCount={customerCount}
                />
            )}

            <div className="grid grid-cols-1 gap-4 xl:grid-cols-2">
                {isSecondaryLoading ? chartSkeleton : (
                    <CollectionsChart
                        dailyPaymentSummaries={trends.dailyPaymentSummaries}
                        dailyLedgerSummaries={trends.dailyLedgerSummaries}
                        expectedTotal={expectedAmount}
                    />
                )}
                <CollectionPieChart summary={trends.monthlyCollectionSummary} />
            </div>

            <div className="grid grid-cols-1 gap-4 xl:grid-cols-2">
                {isSecondaryLoading ? chartSkeleton : (
                    <LedgerDailyChart dailyLedgerSummaries={trends.dailyLedgerSummaries} />
                )}
                <CapitalUtilizationChart
                    totalAmount={pool?.totalAmount ?? 0}
                    availableAmount={pool?.availableAmount ?? 0}
                    lockedAmount={pool?.lockedAmount ?? 0}
                    utilizationPercentage={Math.round((pool?.utilizationPercentage ?? 0) * 100) / 100}
                />
            </div>

            <div className="grid grid-cols-1 gap-4 xl:grid-cols-2">
                <ScheduleOverviewPanel
                    title={t('overdueSchedules')}
                    schedules={operations.overdueSchedules ?? []}
                    emptyMessage={t('noOverdueSchedules')}
                    retryLabel={t('retry')}
                    onRetry={handleRefresh}
                    duePrefix={t('duePrefix')}
                    sequencePrefix={t('sequencePrefix')}
                    isOverduePanel
                />

                <ScheduleOverviewPanel
                    title={t('dueSoonSchedules')}
                    schedules={operations.dueSoonSchedules ?? []}
                    emptyMessage={t('noDueSoonSchedules')}
                    duePrefix={t('duePrefix')}
                    sequencePrefix={t('sequencePrefix')}
                />
            </div>
        </div>
    )
}
