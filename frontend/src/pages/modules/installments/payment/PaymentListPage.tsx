import { useState } from 'react'
import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'

import { usePayments } from '@hooks/modules'
import { usePagination } from '@hooks/common'
import Table from '@components/common/Table'
import type { TableColumn } from '@components/common/Table'
import Button from '@components/common/Button'
import Card from '@components/ui/Card'
import { APP_ROUTES, ROUTE_HELPERS } from '@/router/routes.config'
import type { PaymentSummary } from '@/types/modules/payment.types'
import { formatCurrency } from '@utils/helpers/format.helper'
import { formatDate } from '@utils/helpers/ index'
import './PaymentListPage.css'

export default function PaymentListPage(): ReactNode {
    const { t } = useTranslation('payment')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()

    const [monthFilter, setMonthFilter] = useState('')
    const { page, size, setSize, nextPage, prevPage } = usePagination()

    const monthParam = monthFilter || undefined
    const { payments, loading, error, totalPages, totalElements, refetch } = usePayments(page, size, monthParam)

    const handleClear = () => { setMonthFilter('') }

    const from = totalElements === 0 ? 0 : page * size + 1
    const to = Math.min((page + 1) * size, totalElements)

    const columns: TableColumn<PaymentSummary>[] = [
        { key: 'id', label: t('columns.id') },
        {
            key: 'amount', label: t('columns.amount'),
            render: (row) => formatCurrency(row.amount),
        },
        {
            key: 'netAmount', label: t('columns.netAmount'),
            render: (row) => formatCurrency(row.netAmount),
        },
        {
            key: 'paymentMethod', label: t('columns.paymentMethod'),
            render: (row) => t(`paymentMethod.${row.paymentMethod}`),
        },
        {
            key: 'status', label: t('columns.status'),
            render: (row) => (
                <span className={`payment-list__badge payment-list__badge--${row.status}`}>
                    <span className="payment-list__badge-dot" />
                    {t(`status.${row.status}`)}
                </span>
            ),
        },
        {
            key: 'actualPaymentDate', label: t('columns.actualPaymentDate'),
            render: (row) => formatDate(row.actualPaymentDate),
        },
        {
            key: 'agreedPaymentMonth', label: t('columns.agreedPaymentMonth'),
            render: (row) => row.agreedPaymentMonth || '—',
        },
        {
            key: 'isEarlyPayment', label: t('columns.isEarlyPayment'),
            render: (row) => row.isEarlyPayment
                ? <span className="payment-list__early">✓</span>
                : '—',
        },
        {
            key: 'actions', label: t('columns.actions'),
            render: (row) => (
                <div className="payment-list__actions">
                    <Button size="sm" variant="secondary" onClick={() => navigate(ROUTE_HELPERS.PAYMENT_VIEW(row.id))}>
                        {tc('view')}
                    </Button>
                </div>
            ),
        },
    ]

    if (error && payments.length === 0) {
        return (
            <div className="payment-list__error">
                <p className="payment-list__error-text">{error}</p>
                <Button onClick={refetch}>{t('errorRetry')}</Button>
            </div>
        )
    }

    return (
        <div>
            <div className="payment-list__header">
                <h1 className="payment-list__title">{t('title')}</h1>
                <Button onClick={() => navigate(APP_ROUTES.PAYMENTS.CREATE)}>{t('createNew')}</Button>
            </div>

            <div className="payment-list__filters">
                <div>
                    <label className="form-field__label" style={{ marginBlockEnd: 'var(--spacing-1)', display: 'block' }}>
                        {t('filterByMonth')}
                    </label>
                    <input
                        type="month"
                        className="payment-list__month-input"
                        value={monthFilter}
                        onChange={(e) => setMonthFilter(e.target.value)}
                    />
                </div>
                {monthFilter && (
                    <Button variant="secondary" size="sm" onClick={handleClear}>{t('clearFilters')}</Button>
                )}
            </div>

            <Card>
                <Table<PaymentSummary>
                    columns={columns}
                    data={payments}
                    loading={loading}
                    emptyMessage={t('empty')}
                />

                {totalElements > 0 && (
                    <div className="payment-list__pagination">
                        <span className="payment-list__pagination-info">
                            {t('pagination.showing', { from, to, total: totalElements })}
                        </span>
                        <div className="payment-list__pagination-controls">
                            <span className="payment-list__pagination-info">{t('pagination.pageSize')}</span>
                            <select className="payment-list__page-size" value={size} onChange={(e) => setSize(Number(e.target.value))}>
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
        </div>
    )
}
