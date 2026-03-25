import { useState, useMemo } from 'react'
import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'

import {useCustomers, useCustomerDelete, useCustomerStats} from '@hooks/modules'
import { usePagination, useDebounce } from '@hooks/common'
import Table from '@components/common/Table'
import type { TableColumn } from '@components/common/Table'
import Button from '@components/common/Button'
import Input from '@components/common/Input'
import Card from '@components/ui/Card'
import { APP_ROUTES, ROUTE_HELPERS } from '@/router/routes.config'
import type { Customer, CustomerFilters } from '@/types/modules/customer.types'
import { formatDateTime } from '@utils/helpers/ index'
import './CustomerListPage.css'
import {useDeleteConfirmation} from "@hooks/common/useDeleteConfirmation.ts";
// import Modal from "@mui/material/Modal"
import Modal from '@components/common/Modal/Modal'

// ────────────────────────────────────────────────────────────
// Constants
// ────────────────────────────────────────────────────────────
const PAGE_SIZE_OPTIONS = [10, 25, 50] as const


// ────────────────────────────────────────────────────────────
// Component
// ────────────────────────────────────────────────────────────

export default function CustomerListPage(): ReactNode {
    const { t } = useTranslation('customer')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()

    // ── Search state ───────────────────────────────────────
    const [searchTerm, setSearchTerm] = useState('')
    const debouncedSearch = useDebounce(searchTerm, 400)

    // ── Status filter toggle — default "active" to match backend ──
    const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('active')

    // ── Customer stats ─────────────────────────────────────
    const { stats } = useCustomerStats()

    // ── Pagination ─────────────────────────────────────────
    const {
        page, size, nextPage, prevPage, setSize,
    } = usePagination()

    // ── Build filters ──────────────────────────────────────
    const filters = useMemo<CustomerFilters>(() => ({
        page,
        size,
        ...(debouncedSearch && { search: debouncedSearch }),
        status: statusFilter,
    }), [page, size, debouncedSearch, statusFilter])

    // ── Data fetching ──────────────────────────────────────
    const {
        customers, loading, totalPages, totalElements, refetch,
    } = useCustomers(filters)

    // ── Delete ─────────────────────────────────────────────
    const { deleteCustomer, loading: deleteLoading, error } = useCustomerDelete()

    const {
        isModalOpen,
        // itemId,
        confirmDelete,
        handleDelete,
        handleCloseModal,
    } = useDeleteConfirmation(t('deleted'), () => navigate(APP_ROUTES.CUSTOMERS.LIST))

    // ── Clear filters ──────────────────────────────────────
    const handleClearFilters = () => {
        setSearchTerm('')
        setStatusFilter('active')
    }

    // ── Pagination info ────────────────────────────────────
    const from = totalElements === 0 ? 0 : page * size + 1
    const to = Math.min((page + 1) * size, totalElements)

    // ── Table columns ──────────────────────────────────────
    const columns = useMemo<TableColumn<Customer>[]>(() => [
        { key: 'name',       label: t('columns.name') },
        { key: 'phone',      label: t('columns.phone') },
        { key: 'nationalId', label: t('columns.nationalId') },
        { key: 'address',    label: t('columns.address') },
        {
            key: 'createdAt',
            label: t('columns.createdAt'),
            render: (row) => formatDateTime(row.createdAt),
        },
        {
            key: 'notes',
            label: t('columns.notes'),
            render: (row) => row.notes || '—',
        },
        {
            key: 'actions',
            label: t('columns.actions'),
            render: (row) => (
                <div className="customer-list__actions">
                    <Button
                        size="sm"
                        variant="secondary"
                        onClick={() => navigate(ROUTE_HELPERS.CUSTOMER_VIEW(row.id))}
                    >
                        {tc('view')}
                    </Button>
                    <Button
                        size="sm"
                        variant="secondary"
                        onClick={() => navigate(ROUTE_HELPERS.CUSTOMER_EDIT(row.id))}
                    >
                        {tc('edit')}
                    </Button>
                    <Button
                        size="sm"
                        variant="danger"
                        loading={deleteLoading}
                        onClick={() => confirmDelete(row.id)}
                    >
                        {tc('delete')}
                    </Button>
                </div>
            ),
        },
    ], [t, tc, navigate, confirmDelete, deleteLoading])

    // ── Error state ────────────────────────────────────────
    if (error && customers.length === 0) {
        return (
            <div className="customer-list__error">
                <p className="customer-list__error-text">{error}</p>
                <Button onClick={refetch}>{t('errorRetry')}</Button>
            </div>
        )
    }

    // ── Render ─────────────────────────────────────────────
    return (
        <div>
            {/* ── Page header ──────────────────────────── */}
            <div className="customer-list__header">
                <h1 className="customer-list__title">{t('title')}</h1>
                <Button onClick={() => navigate(APP_ROUTES.CUSTOMERS.CREATE)}>
                    {t('createNew')}
                </Button>
            </div>

            {/* ── Filters ──────────────────────────────── */}
            <div className="customer-list__filters">
                <div className="customer-list__search">
                    <Input
                        name="search"
                        placeholder={t('searchPlaceholder')}
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>

                {/* ── Active / Inactive Toggle ─────────── */}
                <div className="customer-list__status-toggle">
                    <button
                        type="button"
                        className={`customer-list__status-btn ${statusFilter === 'all' ? 'customer-list__status-btn--active' : ''}`}
                        onClick={() => setStatusFilter('all')}
                    >
                        {t('filterAll')}
                    </button>
                    <button
                        type="button"
                        className={`customer-list__status-btn ${statusFilter === 'active' ? 'customer-list__status-btn--active' : ''}`}
                        onClick={() => setStatusFilter('active')}
                    >
                        {t('filterActive')}
                    </button>
                    <button
                        type="button"
                        className={`customer-list__status-btn ${statusFilter === 'inactive' ? 'customer-list__status-btn--active' : ''}`}
                        onClick={() => setStatusFilter('inactive')}
                    >
                        {t('filterInactive')}
                    </button>
                </div>

                {/* ── Clear Filters ───────────────────── */}
                {(searchTerm || statusFilter !== 'active') && (
                    <Button
                        variant="secondary"
                        size="sm"
                        onClick={handleClearFilters}
                    >
                        {t('clearFilters')}
                    </Button>
                )}
            </div>

            {/* ── Table ────────────────────────────────── */}
            <Card>
                <Table<Customer>
                    columns={columns}
                    data={customers}
                    loading={loading}
                    emptyMessage={t('empty')}
                />

                {/* ── Pagination ───────────────────────── */}
                {totalElements > 0 && (
                    <div className="customer-list__pagination">
                        <span className="customer-list__page-info">
                            {t('pagination.showing', { from, to, total: totalElements })}
                        </span>
                        <div className="customer-list__page-size">
                            <span>{t('pagination.pageSize')}</span>
                            <select
                                value={size}
                                onChange={(e) => setSize(Number(e.target.value))}
                            >
                                {PAGE_SIZE_OPTIONS.map((opt) => (
                                    <option key={opt} value={opt}>{opt}</option>
                                ))}
                            </select>
                        </div>
                        <div className="customer-list__page-controls">
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
            {/*  Global Customer Stats                       */}
            {stats && Object.keys(stats).length > 0 && (
                <div className="customer-details__section">
                    <Card title={t('stats.title')}>
                        <div className="customer-details__stats-grid">
                            {Object.entries(stats).map(([label, count]) => (
                                <div key={label} className="customer-details__stat-item">
                                    <span className="customer-details__stat-count">{count}</span>
                                    <span className="customer-details__stat-text">{label}</span>
                                </div>
                            ))}
                        </div>
                    </Card>
                </div>
            )}
            {/* ── Delete Confirmation Modal ─────────────── */}
            <Modal
                isOpen={isModalOpen}
                onClose={handleCloseModal}
                title={t('deleteConfirm')}
                footer={
                    <>
                        <Button variant="secondary" onClick={handleCloseModal}>
                            {tc('cancel')}
                        </Button>
                        <Button
                            variant="danger"
                            onClick={() => handleDelete(deleteCustomer)}
                            loading={deleteLoading}
                        >
                            {tc('delete')}
                        </Button>
                    </>
                }
            >
                <p>{t('deleteConfirmMessage') || 'هل أنت متأكد من أنك تريد حذف هذا العنصر ؟'}</p>
            </Modal>
        </div>
    )
}
