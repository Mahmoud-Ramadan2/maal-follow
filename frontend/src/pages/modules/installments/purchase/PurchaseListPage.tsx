import { useState, useMemo } from 'react'
import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { usePurchases, usePurchaseDelete } from '@hooks/modules'
import { usePagination, useDebounce } from '@hooks/common'
import Table from '@components/common/Table'
import type { TableColumn } from '@components/common/Table'
import Button from '@components/common/Button'
import Input from '@components/common/Input'
import Card from '@components/ui/Card'
import { APP_ROUTES, ROUTE_HELPERS } from '@/router/routes.config'
import type { Purchase, PurchaseFilters } from '@/types/modules/purchase.types'
import './PurchaseListPage.css'
import { useVendors } from '@hooks/modules/useVendors'
import {toast} from "react-toastify";
import Modal from '@components/common/Modal/Modal'
// ────────────────────────────────────────────────────────────
// Page-size options for the selector
// ────────────────────────────────────────────────────────────
const PAGE_SIZE_OPTIONS = [10, 25, 50] as const

// ────────────────────────────────────────────────────────────
// Component
// ────────────────────────────────────────────────────────────

export default function PurchaseListPage(): ReactNode {
    const { t } = useTranslation('purchase')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()

    // ── Delete modal state ─────────────────────────────────
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false)
    const [deleteId, setDeleteId] = useState<number | null>(null)

    // ── Search state ───────────────────────────────────────
    const [searchTerm, setSearchTerm] = useState('')
    const debouncedSearch = useDebounce(searchTerm, 400)
    // ── Vendor filter state ───────────────────────────────
    const [vendorId, setVendorId] = useState<number | undefined>(undefined)
    const debouncedVendor = useDebounce(vendorId, 400)


    // ── Date filter state ─────────────────────────────────
    const [startDate, setStartDate] = useState<string | undefined>(undefined)
    // const debouncedStartDate = useDebounce(startDate, 5000)
    const [endDate, setEndDate] = useState<string | undefined>(undefined)
    // const debouncedEndDate = useDebounce(endDate, 8000)

    // ── Pagination ─────────────────────────────────────────
    const {
        page, size, nextPage, prevPage, setSize,
    } = usePagination()

    // ── Fetch vendors for the dropdown ────────────────────
    const vendorFilters = useMemo(() => ({ size: 100 }), [])
    const { vendors, loading: vendorsLoading } = useVendors(vendorFilters)    // ── Build filters (re-computed only when deps change) ──
    const filters = useMemo<PurchaseFilters>(() => ({
        page,
        size,
        sort: 'purchaseDate,desc',
        ...(debouncedSearch && { searchTerm: debouncedSearch }),
        ...(debouncedVendor && { vendorId: debouncedVendor }),
        // ...(debouncedStartDate && { startDate: debouncedStartDate }),
        // ...(debouncedEndDate && { endDate: debouncedEndDate }),
        ...(startDate && { startDate }),
        ...(endDate && { endDate }),
    }), [page, size, debouncedSearch, debouncedVendor, startDate, endDate])

    // ── Data fetching ──────────────────────────────────────
    const {
        purchases, loading, error, totalPages, totalElements, refetch,
    } = usePurchases(filters)

    // ── Delete ─────────────────────────────────────────────
    const { deletePurchase } = usePurchaseDelete()

    const confirmDelete = (id: number) => {
        setDeleteId(id)
        setIsDeleteModalOpen(true)
    }

    const handleDelete = async () => {
        if (!deleteId) return

        const deleted = await deletePurchase(deleteId)
        if (deleted) {
            refetch()
            toast.success(t('deleted'))
        }
        setIsDeleteModalOpen(false)
        setDeleteId(null)
    }

    const handleCloseModal = () => {
        setIsDeleteModalOpen(false)
        setDeleteId(null)
    }


    // ── Clear filters ──────────────────────────────────────
    const handleClearFilters = () => {
        setSearchTerm('')
        setVendorId(undefined)
        setStartDate(undefined)
        setEndDate(undefined)
    }

    // ── Pagination info ────────────────────────────────────
    const from = totalElements === 0 ? 0 : page * size + 1
    const to = Math.min((page + 1) * size, totalElements)

    // ── Table columns ──────────────────────────────────────
    const columns =
        useMemo<TableColumn<Purchase>[]>(() => [
        {
            key: 'productName',
            label: t('columns.productName'),
        },
        {
            key: 'buyPrice',
            label: t('columns.buyPrice'),
            render: (row) => row.buyPrice.toLocaleString(undefined, {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2,
            }),
        },
        {
            key: 'vendorName',
            label: t('columns.vendorName'),
        },
        {
            key: 'purchaseDate',
            label: t('columns.purchaseDate'),
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

                <div className="purchase-list__actions">
                    <Button
                        size="sm"
                        variant="secondary"
                        onClick={() => navigate(ROUTE_HELPERS.PURCHASE_VIEW(row.id))}
                    >
                        {tc('view')}
                    </Button>
                    <Button
                        size="sm"
                        variant="secondary"
                        onClick={() => navigate(ROUTE_HELPERS.PURCHASE_EDIT(row.id))}
                    >
                        {tc('edit')}
                    </Button>
                    <Button
                        size="sm"
                        variant="danger"
                        onClick={() => confirmDelete(row.id)}
                    >
                        {tc('delete')}
                    </Button>
                </div>
            ),
        },
    ], [t, tc, navigate])

    // ── Error state ────────────────────────────────────────
    if (error && purchases.length === 0) {
        return (
            <div className="purchase-list__error">
                <p className="purchase-list__error-text">{error}</p>
                <Button onClick={refetch}>{t('errorRetry')}</Button>
            </div>
        )
    }

    // ── Render ─────────────────────────────────────────────
    return (
        <div>
            {/* ── Page header ──────────────────────────── */}
            <div className="purchase-list__header">
                <h1 className="purchase-list__title">{t('title')}</h1>
                <div className="purchase-list__header-actions">
                    <Button
                        variant="secondary"
                        onClick={() => navigate(APP_ROUTES.PURCHASES.STATISTICS)}
                    >
                        {t('viewStatistics')}
                    </Button>
                    <Button onClick={() => navigate(APP_ROUTES.PURCHASES.CREATE)}>
                        {t('createNew')}
                    </Button>
                </div>
            </div>

            {/* ── Filters bar ──────────────────────────────── */}
            <div className="purchase-list__filters">
                {/* Search */}
                <div className="purchase-list__search">
                    <Input
                        name="search"
                        placeholder={t('searchPlaceholder')}
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>

                {/* Vendor Filter */}
                <div className="purchase-list__filter-item">

                    <select
                        id="vendorFilter"
                        className="purchase-list__select"
                        value={vendorId ?? ''}
                        onChange={(e) => setVendorId(e.target.value ? Number(e.target.value) : undefined)}
                    >
                        <option value="">
                            {vendorsLoading ? '...' : t('filter.allVendors')}
                        </option>
                        {vendors.map((v) => (
                            <option key={v.id} value={v.id}>{v.name}</option>
                        ))}
                    </select>
                </div>

                {/* Start Date Filter */}
                <div className="purchase-list__filter-item">
                    <label htmlFor="startDateFilter" className="purchase-list__filter-label">
                        {t('filter.startDate')}
                    </label>
                    <input
                        id="startDateFilter"
                        type="date"
                        className="purchase-list__date-input"
                        value={startDate ?? ''}
                        onChange={(e) => setStartDate(e.target.value || undefined)}
                    />
                </div>

                {/* End Date Filter */}
                <div className="purchase-list__filter-item">
                    <label htmlFor="endDateFilter" className="purchase-list__filter-label">
                        {t('filter.endDate')}
                    </label>
                    <input
                        id="endDateFilter"
                        type="date"
                        className="purchase-list__date-input"
                        value={endDate ?? ''}
                        onChange={(e) => setEndDate(e.target.value || undefined)}
                    />
                </div>

                {/* Clear Filters Button */}
                {(searchTerm || vendorId || startDate || endDate) && (
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
                <Table<Purchase>
                    columns={columns}
                    data={purchases}
                    loading={loading}
                    emptyMessage={t('empty')}
                />

                {/* ── Pagination ───────────────────────── */}
                {totalElements > 0 && (
                    <div className="purchase-list__pagination">
                        {/* Page info */}
                        <span className="purchase-list__page-info">
                            {t('pagination.showing', { from, to, total: totalElements })}
                        </span>

                        {/* Page size selector */}
                        <div className="purchase-list__page-size">
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
                        <div className="purchase-list__page-controls">
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
                isOpen={isDeleteModalOpen}
                onClose={handleCloseModal}
                title={t('deleteConfirm')}
                footer={
                    <>
                        <Button variant="secondary" onClick={handleCloseModal}>
                            {tc('cancel')}
                        </Button>
                        <Button variant="danger" onClick={handleDelete} loading={false}>
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
