import type { ReactNode } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { usePurchase, usePurchaseDelete } from '@hooks/modules'
import Button from '@components/common/Button'
import Card from '@components/ui/Card'
import LoadingSpinner from '@components/ui/LoadingSpinner'
import { APP_ROUTES, ROUTE_HELPERS } from '@/router/routes.config'
import { formatDate, formatDateTime, formatCurrency } from '@utils/helpers/ index'
import './PurchaseDetailsPage.css'
import {useDeleteConfirmation} from "@hooks/common/useDeleteConfirmation.ts";
import Modal from '@components/common/Modal/Modal'


// ────────────────────────────────────────────────────────────
// Component
// ────────────────────────────────────────────────────────────

/**
 * Purchase details view page.
 *
 * Reads `:id` from the route, fetches the purchase record,
 * and displays all fields in a read-only card layout.
 *
 * Sections:
 *  1. Page header — title, back / edit / delete buttons
 *  2. Purchase information card — vendor, product, price, dates
 *  3. Notes section (only if notes exist)
 *  4. Payment history placeholder
 */
export default function PurchaseDetailsPage(): ReactNode {
    const { id } = useParams<{ id: string }>()
    const purchaseId = Number(id)
    const navigate = useNavigate()
    const { t } = useTranslation('purchase')
    const { t: tc } = useTranslation('common')


    // ── Data fetching ──────────────────────────────────────
    const { purchase, loading, error } = usePurchase(purchaseId)

    // handle Delete
    const { deletePurchase, loading: deleteLoading } = usePurchaseDelete()
    // ── Delete confirmation hook ───────────────────────────
    const {
        isModalOpen,
        // itemId,
        confirmDelete,
        handleDelete,
        handleCloseModal,
    } = useDeleteConfirmation(
        t('deleted'),
        () => navigate(APP_ROUTES.PROCUREMENT.LIST)
    )
    // ── Delete handler ─────────────────────────────────────
    // const handleDelete = async () => {
    //     const deleted = await deletePurchase(purchaseId)
    //     if (deleted) navigate(APP_ROUTES.PURCHASES.LIST)
    // }

    // ── Loading state ──────────────────────────────────────
    if (loading) {
        return (
            <div className="purchase-details__center">
                <LoadingSpinner size="lg" />
            </div>
        )
    }

    // ── Error state ────────────────────────────────────────
    if (error) {
        return (
            <div className="purchase-details__center">
                <p className="purchase-details__error-text">{error}</p>
                <Button onClick={() => navigate(APP_ROUTES.PROCUREMENT.LIST)}>
                    {t('details.backToList')}
                </Button>
            </div>
        )
    }

    // ── Not found ──────────────────────────────────────────
    if (!purchase) {
        return (
            <div className="purchase-details__center">
                <p className="purchase-details__error-text">
                    {t('details.notFound')}
                </p>
                <Button onClick={() => navigate(APP_ROUTES.PROCUREMENT.LIST)}>
                    {t('details.backToList')}
                </Button>
            </div>
        )
    }

    // ── Render ─────────────────────────────────────────────
    return (
        <div>
            {/* ═══════════════════════════════════════════
                1. Page Header
               ═══════════════════════════════════════════ */}
            <div className="purchase-details__header">
                <div className="purchase-details__header-start">
                    <Button
                        variant="secondary"
                        size="sm"
                        onClick={() => navigate(APP_ROUTES.PROCUREMENT.LIST)}
                    >
                        ← {t('details.backToList')}
                    </Button>
                    <h1 className="purchase-details__title">
                        {t('details.title', { id: purchase.id })}
                    </h1>
                </div>

                <div className="purchase-details__header-actions">
                    <Button
                        variant="secondary"
                        onClick={() => navigate(ROUTE_HELPERS.PURCHASE_EDIT(purchase.id))}
                    >
                        {tc('edit')}
                    </Button>
                    <Button
                        variant="danger"
                        onClick={() => confirmDelete(purchaseId)}
                        loading={deleteLoading}
                    >
                        {tc('delete')}
                    </Button>

                </div>
            </div>

            {/* ═══════════════════════════════════════════
                2. Purchase Information Card
               ═══════════════════════════════════════════ */}
            <Card title={t('details.purchaseInfo')}>
                <div className="purchase-details__grid">
                    {/* Product Name */}
                    <div className="purchase-details__field">
                        <span className="purchase-details__label">
                            {t('details.productName')}
                        </span>
                        <span className="purchase-details__value">
                            {purchase.productName}
                        </span>
                    </div>

                    {/* Buy Price */}
                    <div className="purchase-details__field">
                        <span className="purchase-details__label">
                            {t('details.buyPrice')}
                        </span>
                        <span className="purchase-details__value purchase-details__value--lg">
                            {formatCurrency(purchase.buyPrice)}
                        </span>
                    </div>

                    {/* Vendor */}
                    <div className="purchase-details__field">
                        <span className="purchase-details__label">
                            {t('details.vendor')}
                        </span>
                        <span className="purchase-details__value">
                            {purchase.vendorName}
                        </span>
                    </div>

                    {/* Purchase Date */}
                    <div className="purchase-details__field">
                        <span className="purchase-details__label">
                            {t('details.purchaseDate')}
                        </span>
                        <span className="purchase-details__value">
                            {formatDate(purchase.purchaseDate)}
                        </span>
                    </div>

                    {/* Created At */}
                    <div className="purchase-details__field">
                        <span className="purchase-details__label">
                            {t('details.createdAt')}
                        </span>
                        <span className="purchase-details__value">
                            {formatDateTime(purchase.createdAt)}
                        </span>
                    </div>
                </div>
            </Card>

            {/* ═══════════════════════════════════════════
                3. Notes Section
               ═══════════════════════════════════════════ */}
            <div className="purchase-details__section">
                <Card title={t('details.notes')}>
                    {purchase.notes ? (
                        <p className="purchase-details__notes">
                            {purchase.notes}
                        </p>
                    ) : (
                        <p className="purchase-details__notes purchase-details__notes--empty">
                            {t('details.noNotes')}
                        </p>
                    )}
                </Card>
            </div>

            {/* ═══════════════════════════════════════════
                4. Payment History (placeholder)
               ═══════════════════════════════════════════ */}
            <div className="purchase-details__section">
                <Card title={t('details.paymentHistory')}>
                    <div className="purchase-details__placeholder">
                        <span className="purchase-details__placeholder-icon">💳</span>
                        <span className="purchase-details__placeholder-text">
                            {t('details.paymentPlaceholder')}
                        </span>
                    </div>
                </Card>
            </div>
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
                            onClick={() => handleDelete(deletePurchase )}
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
