import { useState, useMemo } from 'react'
import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'

import { useContracts } from '@hooks/modules'
import { usePagination } from '@hooks/common'
import Table from '@components/common/Table'
import type { TableColumn } from '@components/common/Table'
import Button from '@components/common/Button'
import Input from '@components/common/Input'
import Card from '@components/ui/Card'
import { APP_ROUTES, ROUTE_HELPERS } from '@/router/routes.config'
import { ContractStatus } from '@/types/modules/contract.types'
import type { Contract } from '@/types/modules/contract.types'
import { formatCurrency } from '@utils/helpers/format.helper'
import { formatDate } from '@utils/helpers/ index'
import './ContractListPage.css'

const PAGE_SIZE_OPTIONS = [10, 25, 50] as const
const STATUS_TABS: Array<ContractStatus | 'ALL'> = ['ALL', ContractStatus.ACTIVE, ContractStatus.LATE, ContractStatus.COMPLETED, ContractStatus.CANCELLED]

export default function ContractListPage(): ReactNode {
    const { t } = useTranslation('contract')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()

    // ── Status tab ─────────────────────────────────────────
    const [activeTab, setActiveTab] = useState<ContractStatus | 'ALL'>('ACTIVE')
    const [searchTerm, setSearchTerm] = useState('')

    // ── Pagination ─────────────────────────────────────────
    const { page, size, nextPage, prevPage, setSize } = usePagination()

    const statusToFetch= activeTab
    const { contracts: rawContracts, loading, error, totalPages, totalElements, refetch } =
        useContracts(statusToFetch, page, size)

    // ── Client-side search filter ──────────────────────────
    const contracts = useMemo(() => {
        if (!searchTerm.trim()) return rawContracts
        const q = searchTerm.toLowerCase()
        return rawContracts.filter(c =>
            c.id?.toString().toLowerCase().includes(q) ||
            c.contractNumber?.toLowerCase().includes(q) ||
            c.customerName?.toLowerCase().includes(q) ||
            c.productName?.toLowerCase().includes(q) ||
            c.notes?.toLowerCase().includes(q),
        )
    }, [rawContracts, searchTerm])


    const handleClearFilters = () => { setSearchTerm(''); setActiveTab('ACTIVE') }

    const from = totalElements === 0 ? 0 : page * size + 1
    const to = Math.min((page + 1) * size, totalElements)

    // ── Table columns ──────────────────────────────────────
    const columns: TableColumn<Contract>[] = [
        { key: 'id', label: t('columns.contractNumber') },
        { key: 'customerName', label: t('columns.customerName') },
        { key: 'productName', label: t('columns.productName') },
        {
            key: 'finalPrice', label: t('columns.finalPrice'),
            render: (row) => formatCurrency(row.finalPrice),
        },
        {
            key: 'monthlyAmount', label: t('columns.monthlyAmount'),
            render: (row) => formatCurrency(row.monthlyAmount),
        },
        {
            key: 'remainingAmount', label: t('columns.remainingAmount'),
            render: (row) => formatCurrency(row.remainingAmount),
        },
        { key: 'months', label: t('columns.months') },
        {
            key: 'status', label: t('columns.status'),
            render: (row) => (
                <span className={`contract-list__badge contract-list__badge--${row.status}`}>
                    <span className="contract-list__badge-dot" />
                    {t(`status.${row.status}`)}
                </span>
            ),
        },
        {
            key: 'startDate', label: t('columns.startDate'),
            render: (row) => formatDate(row.startDate),
        },
        {
            key: 'actions', label: t('columns.actions'),
            render: (row) => (
                <div className="contract-list__actions">
                    <Button size="sm" variant="secondary" onClick={() => navigate(ROUTE_HELPERS.CONTRACT_VIEW(row.id))}>
                        {tc('view')}
                    </Button>
                    <Button size="sm" variant="secondary" onClick={() => navigate(ROUTE_HELPERS.CONTRACT_EDIT(row.id))}>
                        {tc('edit')}
                    </Button>
                </div>
            ),
        },
    ]

    if (error && contracts.length === 0) {
        return (
            <div className="contract-list__error">
                <p className="contract-list__error-text">{error}</p>
                <Button onClick={refetch}>{t('errorRetry')}</Button>
            </div>
        )
    }

    return (
        <div>
            {/* Header */}
            <div className="contract-list__header">
                <h1     className="contract-list__title">{t('title')}</h1>
                <Button onClick={() => navigate(APP_ROUTES.CONTRACTS.CREATE)}>
                    {t('createNew')}
                </Button>
            </div>

            {/* Status tabs */}
            <div className="contract-list__status-tabs">
                {STATUS_TABS.map((tab) => (
                    <button
                        key={tab}
                        type="button"
                        className={`contract-list__tab ${activeTab === tab ? 'contract-list__tab--active' : ''}`}
                        onClick={() => setActiveTab(tab)}
                    >
                        {t(`statusFilter.${tab}`)}
                    </button>
                ))}
            </div>

            {/* Filters */}
            <div className="contract-list__filters">
                <div className="contract-list__search">
                    <Input
                        name="search"
                        placeholder={t('searchPlaceholder')}
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                {(searchTerm || activeTab !== 'ACTIVE') && (
                    <Button variant="secondary" size="sm" onClick={handleClearFilters}>
                        {t('clearFilters')}
                    </Button>
                )}
            </div>

            {/* Table */}
            <Card>
                <Table<Contract>
                    columns={columns}
                    data={contracts}
                    loading={loading}
                    emptyMessage={t('empty')}
                />

                {totalElements > 0 && (
                    <div className="contract-list__pagination">
                        <span className="contract-list__page-info">
                            {t('pagination.showing', { from, to, total: totalElements })}
                        </span>
                        <div className="contract-list__page-size">
                            <span>{t('pagination.pageSize')}</span>
                            <select value={size} onChange={(e) => setSize(Number(e.target.value))}>
                                {PAGE_SIZE_OPTIONS.map((o) => <option key={o} value={o}>{o}</option>)}
                            </select>
                        </div>
                        <div className="contract-list__page-controls">
                            <Button size="sm" variant="secondary" onClick={prevPage} disabled={page === 0}>
                                {t('pagination.previous')}
                            </Button>
                            <Button size="sm" variant="secondary" onClick={nextPage} disabled={page >= totalPages - 1}>
                                {t('pagination.next')}
                            </Button>
                        </div>
                    </div>
                )}
            </Card>
        </div>
    )
}
