import { useState } from 'react'
import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'

import { useCapitalPool, useCapitalPoolActions, useCapitalTransactions } from '@hooks/modules'
import { usePagination } from '@hooks/common'
import Table from '@components/common/Table'
import type { TableColumn } from '@components/common/Table'
import Button from '@components/common/Button'
import Card from '@components/ui/Card'
import LoadingSpinner from '@components/ui/LoadingSpinner'
import { APP_ROUTES } from '@/router/routes.config'
import { CapitalTransactionType } from '@/types/modules/capital.types'
import type { CapitalTransaction } from '@/types/modules/capital.types'
import { formatCurrency } from '@utils/helpers/format.helper'
import { formatDate } from '@utils/helpers/ index'
import './CapitalListPage.css'

type TypeFilter = CapitalTransactionType | 'ALL'
const TYPE_TABS: TypeFilter[] = ['ALL', ...Object.values(CapitalTransactionType)]

export default function CapitalListPage(): ReactNode {
    const { t } = useTranslation('capital')
    const navigate = useNavigate()

    const [activeTab, setActiveTab] = useState<TypeFilter>('ALL')
    const { page, size, setSize, nextPage, prevPage } = usePagination()

    // Pool
    const { pool, loading: poolLoading, refetch: refetchPool } = useCapitalPool()
    const { recalculate, loading: recalcLoading } = useCapitalPoolActions()

    // Transactions
    const typeParam = activeTab === 'ALL' ? undefined : activeTab
    const { transactions, loading: txLoading, error: txError, totalPages, totalElements, refetch: refetchTx } = useCapitalTransactions(page, size, typeParam)

    const handleRecalculate = async () => {
        const result = await recalculate()
        if (result) refetchPool()
    }

    const from = totalElements === 0 ? 0 : page * size + 1
    const to = Math.min((page + 1) * size, totalElements)

    const columns: TableColumn<CapitalTransaction>[] = [
        { key: 'id', label: t('transaction.columns.id') },
        {
            key: 'transactionType', label: t('transaction.columns.transactionType'),
            render: (row) => (
                <span className={`capital-tx__badge capital-tx__badge--${row.transactionType}`}>
                    {t(`transactionType.${row.transactionType}`)}
                </span>
            ),
        },
        {
            key: 'amount', label: t('transaction.columns.amount'),
            render: (row) => formatCurrency(row.amount),
        },
        {
            key: 'availableBefore', label: t('transaction.columns.availableBefore'),
            render: (row) => formatCurrency(row.availableBefore),
        },
        {
            key: 'availableAfter', label: t('transaction.columns.availableAfter'),
            render: (row) => formatCurrency(row.availableAfter),
        },
        {
            key: 'partnerName', label: t('transaction.columns.partnerName'),
            render: (row) => row.partnerName || '—',
        },
        {
            key: 'transactionDate', label: t('transaction.columns.transactionDate'),
            render: (row) => formatDate(row.transactionDate),
        },
        {
            key: 'description', label: t('transaction.columns.description'),
            render: (row) => row.description || '—',
        },
    ]

    return (
        <div>
            {/* ═══ Header ═══ */}
            <div className="capital-page__header">
                <h1 className="capital-page__title">{t('title')}</h1>
                <div className="capital-page__header-actions">
                    <Button variant="secondary" onClick={handleRecalculate} loading={recalcLoading}>
                        {t('pool.recalculate')}
                    </Button>
                    {!pool && (
                        <Button onClick={() => navigate(APP_ROUTES.CAPITAL.CREATE)}>{t('editPool')}</Button>
                    )}
                    {pool && (
                        <Button variant="secondary" onClick={() => navigate(APP_ROUTES.CAPITAL.CREATE)}>{t('editPool')}</Button>
                    )}
                    <Button onClick={() => navigate(APP_ROUTES.CAPITAL.CREATE + '?tab=transaction')}>{t('createTransaction')}</Button>
                </div>
            </div>

            {/* ═══ Capital Pool Dashboard ═══ */}
            <div className="capital-pool">
                <Card title={t('pool.currentStatus')}>
                    {poolLoading ? (
                        <div style={{ display: 'flex', justifyContent: 'center', padding: 'var(--spacing-8)' }}>
                            <LoadingSpinner />
                        </div>
                    ) : pool ? (
                        <>
                            <div className="capital-pool__grid">
                                <div className="capital-pool__card">
                                    <span className="capital-pool__card-label">{t('pool.totalAmount')}</span>
                                    <span className="capital-pool__card-value">{formatCurrency(pool.totalAmount)}</span>
                                </div>
                                <div className="capital-pool__card">
                                    <span className="capital-pool__card-label">{t('pool.availableAmount')}</span>
                                    <span className="capital-pool__card-value capital-pool__card-value--positive">{formatCurrency(pool.availableAmount)}</span>
                                </div>
                                <div className="capital-pool__card">
                                    <span className="capital-pool__card-label">{t('pool.lockedAmount')}</span>
                                    <span className="capital-pool__card-value capital-pool__card-value--warning">{formatCurrency(pool.lockedAmount)}</span>
                                </div>
                                <div className="capital-pool__card">
                                    <span className="capital-pool__card-label">{t('pool.returnedAmount')}</span>
                                    <span className="capital-pool__card-value capital-pool__card-value--info">{formatCurrency(pool.returnedAmount)}</span>
                                </div>
                            </div>
                            <div className="capital-pool__shares">
                                <div className="capital-pool__share">
                                    <span className="capital-pool__share-label">{t('pool.ownerSharePercentage')}</span>
                                    <span className="capital-pool__share-value">{pool.ownerSharePercentage?.toFixed(1) ?? '—'}%</span>
                                </div>
                                <div className="capital-pool__share">
                                    <span className="capital-pool__share-label">{t('pool.partnerSharePercentage')}</span>
                                    <span className="capital-pool__share-value">{pool.partnerSharePercentage?.toFixed(1) ?? '—'}%</span>
                                </div>
                                <div className="capital-pool__share">
                                    <span className="capital-pool__share-label">{t('pool.utilizationPercentage')}</span>
                                    <span className="capital-pool__share-value">{pool.utilizationPercentage?.toFixed(1) ?? '—'}%</span>
                                </div>
                            </div>
                        </>
                    ) : (
                        <div className="capital-pool__empty">
                            <span className="capital-pool__empty-icon">🏦</span>
                            <span className="capital-pool__empty-text">{t('pool.noPool')}</span>
                            <Button onClick={() => navigate(APP_ROUTES.CAPITAL.CREATE)}>{t('editPool')}</Button>
                        </div>
                    )}
                </Card>
            </div>

            {/* ═══ Transaction Type Filter ═══ */}
            <h2 style={{ fontSize: 'var(--font-size-xl)', fontWeight: 'var(--font-weight-semibold)', marginBlockEnd: 'var(--spacing-4)' }}>
                {t('transactionsTitle')}
            </h2>

            <div className="capital-tx__filters">
                <div className="capital-tx__type-tabs">
                    {TYPE_TABS.map((tab) => (
                        <button
                            key={tab}
                            type="button"
                            className={`capital-tx__tab ${activeTab === tab ? 'capital-tx__tab--active' : ''}`}
                            onClick={() => setActiveTab(tab)}
                        >
                            {t(`transactionType.${tab}`)}
                        </button>
                    ))}
                </div>
                {activeTab !== 'ALL' && (
                    <Button variant="secondary" size="sm" onClick={() => setActiveTab('ALL')}>{t('filters.clearFilters')}</Button>
                )}
            </div>

            {/* ═══ Transaction Table ═══ */}
            {txError && transactions.length === 0 ? (
                <div className="capital-page__error">
                    <p className="capital-page__error-text">{txError}</p>
                    <Button onClick={refetchTx}>{t('errorRetry')}</Button>
                </div>
            ) : (
                <Card>
                    <Table<CapitalTransaction>
                        columns={columns}
                        data={transactions}
                        loading={txLoading}
                        emptyMessage={t('transaction.empty')}
                    />

                    {totalElements > 0 && !typeParam && (
                        <div className="capital-tx__pagination">
                            <span className="capital-tx__pagination-info">
                                {t('pagination.showing', { from, to, total: totalElements })}
                            </span>
                            <div className="capital-tx__pagination-controls">
                                <span className="capital-tx__pagination-info">{t('pagination.pageSize')}</span>
                                <select className="capital-tx__page-size" value={size} onChange={(e) => setSize(Number(e.target.value))}>
                                    <option value={10}>10</option>
                                    <option value={20}>20</option>
                                    <option value={50}>50</option>
                                </select>
                                <Button size="sm" variant="secondary" disabled={page === 0} onClick={prevPage}>{t('pagination.previous')}</Button>
                                <Button size="sm" variant="secondary" disabled={page >= totalPages - 1} onClick={nextPage}>{t('pagination.next')}</Button>
                            </div>
                        </div>
                    )}
                </Card>
            )}
        </div>
    )
}
