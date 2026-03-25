import { useState, useMemo } from 'react'
import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'

import { useVendors, useVendorDelete } from '@hooks/modules'
import { usePagination, useDebounce } from '@hooks/common'
import Table from '@components/common/Table'
import type { TableColumn } from '@components/common/Table'
import Button from '@components/common/Button'
import Input from '@components/common/Input'
import Card from '@components/ui/Card'
import { APP_ROUTES, ROUTE_HELPERS } from '@/router/routes.config'
import type { Vendor, VendorFilters } from '@/types/modules/vendor.types'
import './VendorListPage.css'
import Modal from '@components/common/Modal/Modal'
import { useDeleteConfirmation } from '@hooks/common/useDeleteConfirmation'

// ────────────────────────────────────────────────────────────
// Page-size options for the selector
// ────────────────────────────────────────────────────────────
const PAGE_SIZE_OPTIONS = [10, 25, 50] as const

// ────────────────────────────────────────────────────────────
// Component
// ────────────────────────────────────────────────────────────

export default function VendorListPage(): ReactNode {
    const { t } = useTranslation('vendor')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()

    // ── Search state ───────────────────────────────────────
    const [searchTerm, setSearchTerm] = useState('')
    const debouncedSearch = useDebounce(searchTerm, 400)

    // ── Status filter toggle — default "active" ──────────
    const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('active')

    // ── Pagination ─────────────────────────────────────────
    const {
        page, size, nextPage, prevPage, setSize,
    } = usePagination()

    // ── Build filters (re-computed only when deps change) ──
    const filters = useMemo<VendorFilters>(() => ({
        page,
        size,
        ...(debouncedSearch && { search: debouncedSearch }),
        status: statusFilter,
    }), [page, size, debouncedSearch, statusFilter])

    // ── Data fetching ──────────────────────────────────────
    const {
        vendors, loading, error, totalPages, totalElements, refetch,
    } = useVendors(filters)

    // ── Delete ─────────────────────────────────────────────
    const { deleteVendor, loading: deleteLoading } = useVendorDelete()

    const {
        isModalOpen,
        confirmDelete,
        handleDelete,
        handleCloseModal,
    } = useDeleteConfirmation(t('deleted'), refetch)

    // ── Clear filters ──────────────────────────────────────
    const handleClearFilters = () => {
        setSearchTerm('')
        setStatusFilter('active')
    }

    // ── Pagination info ────────────────────────────────────
    const from = totalElements === 0 ? 0 : page * size + 1
    const to = Math.min((page + 1) * size, totalElements)

    // ── Table columns ──────────────────────────────────────
    const columns = useMemo<TableColumn<Vendor>[]>(() => [
        {
            key: 'name',
            label: t('columns.name'),
        },
        {
            key: 'phone',
            label: t('columns.phone'),
        },
        {
            key: 'address',
            label: t('columns.address'),
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
                <div className="vendor-list__actions">
                    <Button
                        size="sm"
                        variant="secondary"
                        onClick={() => navigate(ROUTE_HELPERS.VENDOR_VIEW(row.id))}
                    >
                        {tc('view')}
                    </Button>
                    <Button
                        size="sm"
                        variant="secondary"
                        onClick={() => navigate(ROUTE_HELPERS.VENDOR_EDIT(row.id))}
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
    ], [t, tc, navigate, deleteLoading, confirmDelete])

    // ── Error state ────────────────────────────────────────
    if (error && vendors.length === 0) {
        return (
            <div className="vendor-list__error">
                <p className="vendor-list__error-text">{error}</p>
                <Button onClick={refetch}>{t('errorRetry')}</Button>
            </div>
        )
    }

    // ── Render ─────────────────────────────────────────────
    return (
        <div>
            {/* ── Page header ──────────────────────────── */}
            <div className="vendor-list__header">
                <h1 className="vendor-list__title">{t('title')}</h1>
                <Button onClick={() => navigate(APP_ROUTES.VENDORS.CREATE)}>
                    {t('createNew')}
                </Button>
            </div>

            {/* ── Filters ──────────────────────────────── */}
            <div className="vendor-list__filters">
                <div className="vendor-list__search">
                    <Input
                        name="search"
                        placeholder={t('searchPlaceholder')}
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                
                {/* ── Active / Inactive Toggle ─────────── */}
                <div className="vendor-list__status-toggle">
                    <button
                        type="button"
                        className={`vendor-list__status-btn ${statusFilter === 'all' ? 'vendor-list__status-btn--active' : ''}`}
                        onClick={() => setStatusFilter('all')}
                    >
                        {t('filterAll')}
                    </button>
                    <button
                        type="button"
                        className={`vendor-list__status-btn ${statusFilter === 'active' ? 'vendor-list__status-btn--active' : ''}`}
                        onClick={() => setStatusFilter('active')}
                    >
                        {t('filterActive')}
                    </button>
                    <button
                        type="button"
                        className={`vendor-list__status-btn ${statusFilter === 'inactive' ? 'vendor-list__status-btn--active' : ''}`}
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
                <Table<Vendor>
                    columns={columns}
                    data={vendors}
                    loading={loading}
                    emptyMessage={t('empty')}
                />

                {/* ── Pagination ───────────────────────── */}
                {totalElements > 0 && (
                    <div className="vendor-list__pagination">
                        {/* Page info */}
                        <span className="vendor-list__page-info">
                            {t('pagination.showing', { from, to, total: totalElements })}
                        </span>

                        {/* Page size selector */}
                        <div className="vendor-list__page-size">
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

                        {/* Prev / Next */}
                        <div className="vendor-list__page-controls">
                            <Button
                                size="sm"
                                variant="secondary"
                                onClick={prevPage}
                                disabled={page === 0}
                            >
                                {t('pagination.previous')}
                            </Button>
                            <Button
                                size="sm"
                                variant="secondary"
                                onClick={nextPage}
                                disabled={page >= totalPages - 1}
                            >
                                {t('pagination.next')}
                            </Button>
                        </div>
                    </div>
                )}
            </Card>

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
                            onClick={() => handleDelete(deleteVendor)}
                            loading={deleteLoading}
                        >
                            {tc('delete')}
                        </Button>
                    </>
                }
            >
                <p>{t('deleteConfirmMessage')}</p>
            </Modal>
        </div>
    )
}

