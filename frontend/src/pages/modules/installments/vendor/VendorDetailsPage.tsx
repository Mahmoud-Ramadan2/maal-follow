import type {ReactNode} from 'react'
import {useParams, useNavigate} from 'react-router-dom'
import {useTranslation} from 'react-i18next'

import {useVendor, useVendorDelete} from '@hooks/modules'
import Button from '@components/common/Button'
import Card from '@components/ui/Card'
import LoadingSpinner from '@components/ui/LoadingSpinner'
import {APP_ROUTES, ROUTE_HELPERS} from '@/router/routes.config'
import {formatCurrency} from '@utils/helpers/ index'
import './VendorDetailsPage.css'

// ────────────────────────────────────────────────────────────
// Component
// ────────────────────────────────────────────────────────────

/**
 * Vendor details view page.
 *
 * Reads `:id` from the route, fetches the vendor record,
 * and displays all fields in a read-only card layout.
 *
 * Sections:
 *  1. Page header — title, back / edit / delete buttons
 *  2. Vendor information card — name, phone, address
 *  3. Notes section (only if notes exist)
 *  4. Purchases section (vendor's associated purchases)
 */
export default function VendorDetailsPage(): ReactNode {
    const {id} = useParams<{ id: string }>()
    const vendorId = Number(id)
    const navigate = useNavigate()
    const {t} = useTranslation('vendor')
    const {t: tc} = useTranslation('common')

    // ── Data fetching ──────────────────────────────────────
    const {vendor, loading, error} = useVendor(vendorId)
    const {deleteVendor, loading: deleteLoading} = useVendorDelete()

    // ── Delete handler ─────────────────────────────────────
    const handleDelete = async () => {
        const deleted = await deleteVendor(vendorId)
        if (deleted) navigate(APP_ROUTES.PROCUREMENT.LIST)
    }

    // ── Loading state ──────────────────────────────────────
    if (loading) {
        return (
            <div className="vendor-details__center">
                <LoadingSpinner size="lg"/>
            </div>
        )
    }

    // ── Error state ────────────────────────────────────────
    if (error) {
        return (
            <div className="vendor-details__center">
                <p className="vendor-details__error-text">{error}</p>
                <Button onClick={() => navigate(APP_ROUTES.PROCUREMENT.LIST)}>
                    {t('details.backToList')}
                </Button>
            </div>
        )
    }

    // ── Not found ──────────────────────────────────────────
    if (!vendor) {
        return (
            <div className="vendor-details__center">
                <p className="vendor-details__error-text">
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
            <div className="vendor-details__header">
                <div className="vendor-details__header-start">
                    <Button
                        variant="secondary"
                        size="sm"
                        onClick={() => navigate(APP_ROUTES.PROCUREMENT.LIST)}
                    >
                        ← {t('details.backToList')}
                    </Button>
                    <h1 className="vendor-details__title">
                        {t('details.title', {id: vendor.id})}
                    </h1>

                    <span
                        className={`vendor-details__badge vendor-details__badge--${vendor.active ? 'active' : 'inactive'}`}>
                        <span className="vendor-details__badge-dot"/>
                        {vendor.active ? t('status.active') : t('status.inactive')}
                    </span>
                </div>

                <div className="vendor-details__header-actions">
                    <Button
                        variant="secondary"
                        onClick={() => navigate(ROUTE_HELPERS.VENDOR_EDIT(vendor.id))}
                    >
                        {tc('edit')}
                    </Button>
                    <Button
                        variant="danger"
                        onClick={handleDelete}
                        loading={deleteLoading}
                    >
                        {tc('delete')}
                    </Button>
                </div>
            </div>

            {/* ═══════════════════════════════════════════
                2. Vendor Information Card
               ═══════════════════════════════════════════ */}
            <Card title={t('details.vendorInfo')}>
                <div className="vendor-details__grid">
                    {/* Name */}
                    <div className="vendor-details__field">
                        <span className="vendor-details__label">
                            {t('details.name')}
                        </span>
                        <span className="vendor-details__value">
                            {vendor.name}
                        </span>
                    </div>

                    {/* Phone */}
                    <div className="vendor-details__field">
                        <span className="vendor-details__label">
                            {t('details.phone')}
                        </span>
                        <span className="vendor-details__value">
                            {vendor.phone}
                        </span>
                    </div>

                    {/* Address */}
                    <div className="vendor-details__field">
                        <span className="vendor-details__label">
                            {t('details.address')}
                        </span>
                        <span className="vendor-details__value">
                            {vendor.address}
                        </span>
                    </div>
                </div>
            </Card>

            {/* ═══════════════════════════════════════════
                3. Notes Section
               ═══════════════════════════════════════════ */}
            <div className="vendor-details__section">
                <Card title={t('details.notes')}>
                    {vendor.notes ? (
                        <p className="vendor-details__notes">
                            {vendor.notes}
                        </p>
                    ) : (
                        <p className="vendor-details__notes vendor-details__notes--empty">
                            {t('details.noNotes')}
                        </p>
                    )}
                </Card>
            </div>

            {/* ═══════════════════════════════════════════
                4. Purchases Section
               ═══════════════════════════════════════════ */}
            <div className="vendor-details__section">
                <Card title={t('details.purchases')}>
                    {vendor.purchases && vendor.purchases.length > 0 ? (
                        <>
                            <div className="vendor-details__purchases-total-card">
                                <div className="vendor-details__purchases-total-header">
                                    {t('details.totalPurchases', {count:  vendor.purchases.length})}
                                </div>
                                <div className="vendor-details__purchases-total-amount">
                                    {formatCurrency(
                                        vendor.purchases.reduce((sum, purchase) => sum + (purchase.buyPrice || 0), 0)
                                    )}
                                </div>
                            </div>

                            <table className="vendor-details__purchases-table">
                                <thead>
                                <tr>
                                    <th>{t('details.productName')}</th>
                                    <th>{t('details.buyPrice')}</th>
                                </tr>
                                </thead>
                                <tbody>
                                {vendor.purchases.map((purchase, idx) => (
                                    <tr key={idx} onClick={() => navigate(ROUTE_HELPERS.PURCHASE_VIEW(purchase.id))}
                                        className="cursor-pointer hover:bg-gray-50">
                                        <td>{purchase.productName}</td>
                                        <td>{formatCurrency(purchase.buyPrice)}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </>
                    ) : (
                        <div className="vendor-details__placeholder">
                            <span className="vendor-details__placeholder-icon">🏪</span>
                            <span className="vendor-details__placeholder-text">
                                {t('details.purchasesPlaceholder')}
                            </span>
                        </div>
                    )}
                </Card>
            </div>
        </div>
    )
}

