import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { usePurchaseStatistics } from '@hooks/modules'
import Button from '@components/common/Button'
import Card from '@components/ui/Card'
import { APP_ROUTES } from '@/router/routes.config'
import { formatCurrency } from '@utils/helpers/format.helper'
import './PurchaseStatisticsPage.css'

// ─────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────

/** Sort a Record<string, number> descending and return top N entries */
function topEntries(record: Record<string, number> | undefined, n = 5): [string, number][] {
    if (!record) return []
    return Object.entries(record)
        .sort(([, a], [, b]) => b - a)
        .slice(0, n)
}

// ─────────────────────────────────────────────────────────────
// Component
// ─────────────────────────────────────────────────────────────

export default function PurchaseStatisticsPage(): ReactNode {
    const { t } = useTranslation('purchase')
    const navigate = useNavigate()

    const { statistics: stats, loading, error, refetch } = usePurchaseStatistics()

    // ── Loading skeleton ──────────────────────────────────────
    if (loading) {
        return (
            <div>
                <div className="purchase-stats__header">
                    <h1 className="purchase-stats__title">{t('statistics.title')}</h1>
                </div>
                <div className="purchase-stats__skeleton">
                    <div className="purchase-stats__skeleton-kpi" />
                    <div className="purchase-stats__skeleton-kpi" />
                    <div className="purchase-stats__skeleton-kpi" />
                </div>
            </div>
        )
    }

    // ── Error state ───────────────────────────────────────────
    if (error) {
        return (
            <div className="purchase-stats__error">
                <p className="purchase-stats__error-text">{error}</p>
                <Button onClick={refetch}>{t('errorRetry')}</Button>
            </div>
        )
    }

    // ── Derived values ────────────────────────────────────────
    const topByCount  = topEntries(stats?.countByVendor)
    const topByAmount = topEntries(stats?.amountByVendor)

    const maxCount  = topByCount[0]?.[1]  ?? 1
    const maxAmount = topByAmount[0]?.[1] ?? 1

    return (
        <div>
            {/* ── Header ──────────────────────────────────── */}
            <div className="purchase-stats__header">
                <h1 className="purchase-stats__title">{t('statistics.title')}</h1>
                <Button
                    variant="secondary"
                    onClick={() => navigate(APP_ROUTES.PROCUREMENT.LIST)}
                >
                    ← {t('statistics.backToList')}
                </Button>
            </div>

            {/* ── KPI Cards ───────────────────────────────── */}
            <div className="purchase-stats__kpis">
                <div className="purchase-stats__kpi">
                    <span className="purchase-stats__kpi-icon">🛒</span>
                    <span className="purchase-stats__kpi-label">{t('statistics.totalCount')}</span>
                    <span className="purchase-stats__kpi-value purchase-stats__kpi-value--primary">
                        {stats?.totalCount ?? '—'}
                    </span>
                </div>

                <div className="purchase-stats__kpi">
                    <span className="purchase-stats__kpi-icon">💰</span>
                    <span className="purchase-stats__kpi-label">{t('statistics.totalAmount')}</span>
                    <span className="purchase-stats__kpi-value purchase-stats__kpi-value--success">
                        {formatCurrency(stats?.totalAmount)}
                    </span>
                </div>

                <div className="purchase-stats__kpi">
                    <span className="purchase-stats__kpi-icon">📊</span>
                    <span className="purchase-stats__kpi-label">{t('statistics.avgAmount')}</span>
                    <span className="purchase-stats__kpi-value purchase-stats__kpi-value--info">
                        {formatCurrency(stats?.avgAmount)}
                    </span>
                </div>
            </div>

            {/* ── Vendor breakdown ────────────────────────── */}
            <div className="purchase-stats__sections">

                {/* Top vendors by purchase count */}
                <Card>
                    <h2 className="purchase-stats__section-title">
                        {t('statistics.topVendorsByCount')}
                    </h2>

                    {topByCount.length === 0 ? (
                        <div className="purchase-stats__empty">
                            <span className="purchase-stats__empty-icon">📭</span>
                            {t('statistics.noData')}
                        </div>
                    ) : (
                        <table className="purchase-stats__vendor-table">
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>{t('statistics.vendor')}</th>
                                    <th>{t('statistics.count')}</th>
                                    <th>{t('statistics.share')}</th>
                                </tr>
                            </thead>
                            <tbody>
                                {topByCount.map(([vendor, count], i) => (
                                    <tr key={vendor}>
                                        <td>
                                            <span className={`purchase-stats__vendor-rank purchase-stats__vendor-rank--${i + 1}`}>
                                                {i + 1}
                                            </span>
                                        </td>
                                        <td className="purchase-stats__vendor-name">{vendor}</td>
                                        <td>{count}</td>
                                        <td>
                                            <div className="purchase-stats__bar-wrap">
                                                <div className="purchase-stats__bar-track">
                                                    <div
                                                        className="purchase-stats__bar-fill"
                                                        style={{ width: `${(count / maxCount) * 100}%` }}
                                                    />
                                                </div>
                                                <span className="purchase-stats__bar-pct">
                                                    {stats?.totalCount
                                                        ? `${((count / stats.totalCount) * 100).toFixed(0)}%`
                                                        : '—'}
                                                </span>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </Card>

                {/* Top vendors by total spend */}
                <Card>
                    <h2 className="purchase-stats__section-title">
                        {t('statistics.topVendorsByAmount')}
                    </h2>

                    {topByAmount.length === 0 ? (
                        <div className="purchase-stats__empty">
                            <span className="purchase-stats__empty-icon">📭</span>
                            {t('statistics.noData')}
                        </div>
                    ) : (
                        <table className="purchase-stats__vendor-table">
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>{t('statistics.vendor')}</th>
                                    <th>{t('statistics.totalSpend')}</th>
                                    <th>{t('statistics.share')}</th>
                                </tr>
                            </thead>
                            <tbody>
                                {topByAmount.map(([vendor, amount], i) => (
                                    <tr key={vendor}>
                                        <td>
                                            <span className={`purchase-stats__vendor-rank purchase-stats__vendor-rank--${i + 1}`}>
                                                {i + 1}
                                            </span>
                                        </td>
                                        <td className="purchase-stats__vendor-name">{vendor}</td>
                                        <td>{formatCurrency(amount)}</td>
                                        <td>
                                            <div className="purchase-stats__bar-wrap">
                                                <div className="purchase-stats__bar-track">
                                                    <div
                                                        className="purchase-stats__bar-fill"
                                                        style={{ width: `${(amount / maxAmount) * 100}%` }}
                                                    />
                                                </div>
                                                <span className="purchase-stats__bar-pct">
                                                    {stats?.totalAmount
                                                        ? `${((amount / stats.totalAmount) * 100).toFixed(0)}%`
                                                        : '—'}
                                                </span>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </Card>
            </div>
        </div>
    )
}


