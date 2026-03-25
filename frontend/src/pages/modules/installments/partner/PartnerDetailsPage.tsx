import type { ReactNode } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { usePartner, usePartnerDelete } from '@hooks/modules'
import Button from '@components/common/Button'
import Card from '@components/ui/Card'
import LoadingSpinner from '@components/ui/LoadingSpinner'
import { APP_ROUTES, ROUTE_HELPERS } from '@/router/routes.config'
import { formatCurrency } from '@utils/helpers/format.helper'
import { formatDate, formatDateTime } from '@utils/helpers/ index'
import type { PartnerInvestment, PartnerWithdrawal } from '@/types/modules/partner.types'
import './PartnerDetailsPage.css'

export default function PartnerDetailsPage(): ReactNode {
    const { id } = useParams<{ id: string }>()
    const partnerId = Number(id)
    const navigate = useNavigate()
    const { t } = useTranslation('partner')
    const { t: tc } = useTranslation('common')

    const { partner, investments, withdrawals, loading, error } = usePartner(partnerId)
    const { deletePartner, loading: deleteLoading } = usePartnerDelete()

    const handleDelete = async () => {
        const ok = await deletePartner(partnerId)
        if (ok) navigate(APP_ROUTES.PARTNERS.LIST)
    }

    if (loading) {
        return <div className="partner-details__center"><LoadingSpinner size="lg" /></div>
    }

    if (error || !partner) {
        return (
            <div className="partner-details__center">
                <p className="partner-details__error-text">{error || t('details.notFound')}</p>
                <Button onClick={() => navigate(APP_ROUTES.PARTNERS.LIST)}>{t('details.backToList')}</Button>
            </div>
        )
    }

    return (
        <div>
            {/* ═══ Header ═══ */}
            <div className="partner-details__header">
                <div className="partner-details__header-start">
                    <Button variant="secondary" size="sm" onClick={() => navigate(APP_ROUTES.PARTNERS.LIST)}>
                        ← {t('details.backToList')}
                    </Button>
                    <h1 className="partner-details__title">{t('details.title', { name: partner.name })}</h1>
                </div>
                <div className="partner-details__header-actions">
                    <Button variant="secondary" onClick={() => navigate(ROUTE_HELPERS.PARTNER_EDIT(partnerId))}>{tc('edit')}</Button>
                    <Button variant="danger" onClick={handleDelete} loading={deleteLoading}>{tc('delete')}</Button>
                </div>
            </div>

            {/* ═══ Partner Info ═══ */}
            <Card title={t('details.partnerInfo')}>
                <div className="partner-details__grid">
                    <div className="partner-details__field">
                        <span className="partner-details__label">{t('details.name')}</span>
                        <span className="partner-details__value">{partner.name}</span>
                    </div>
                    <div className="partner-details__field">
                        <span className="partner-details__label">{t('details.phone')}</span>
                        <span className="partner-details__value">{partner.phone}</span>
                    </div>
                    <div className="partner-details__field">
                        <span className="partner-details__label">{t('details.address')}</span>
                        <span className="partner-details__value">{partner.address || '—'}</span>
                    </div>
                    <div className="partner-details__field">
                        <span className="partner-details__label">{t('details.partnershipType')}</span>
                        <span className="partner-details__value">{t(`partnershipType.${partner.partnershipType}`)}</span>
                    </div>
                    <div className="partner-details__field">
                        <span className="partner-details__label">{t('details.sharePercentage')}</span>
                        <span className="partner-details__value">{partner.sharePercentage != null ? `${partner.sharePercentage}%` : '—'}</span>
                    </div>
                    <div className="partner-details__field">
                        <span className="partner-details__label">{t('details.status')}</span>
                        <span className="partner-details__value">
                            <span className={`partner-details__badge partner-details__badge--${partner.status}`}>
                                <span className="partner-details__badge-dot" />
                                {t(`status.${partner.status}`)}
                            </span>
                        </span>
                    </div>
                    <div className="partner-details__field">
                        <span className="partner-details__label">{t('details.investmentStartDate')}</span>
                        <span className="partner-details__value">{formatDate(partner.investmentStartDate)}</span>
                    </div>
                    <div className="partner-details__field">
                        <span className="partner-details__label">{t('details.profitCalculationStartMonth')}</span>
                        <span className="partner-details__value">{partner.profitCalculationStartMonth || '—'}</span>
                    </div>
                    <div className="partner-details__field">
                        <span className="partner-details__label">{t('details.profitSharingActive')}</span>
                        <span className="partner-details__value">{partner.profitSharingActive ? t('details.yes') : t('details.no')}</span>
                    </div>
                    <div className="partner-details__field">
                        <span className="partner-details__label">{t('details.createdAt')}</span>
                        <span className="partner-details__value">{formatDateTime(partner.createdAt)}</span>
                    </div>
                    <div className="partner-details__field">
                        <span className="partner-details__label">{t('details.createdByName')}</span>
                        <span className="partner-details__value">{partner.createdByName || '—'}</span>
                    </div>
                </div>
            </Card>

            {/* ═══ Financial Summary ═══ */}
            <div className="partner-details__section">
                <Card title={t('details.financialSummary')}>
                    <div className="partner-details__financial">
                        <div className="partner-details__financial-card">
                            <span className="partner-details__financial-label">{t('details.totalInvestment')}</span>
                            <span className="partner-details__financial-value partner-details__financial-value--positive">{formatCurrency(partner.totalInvestment)}</span>
                        </div>
                        <div className="partner-details__financial-card">
                            <span className="partner-details__financial-label">{t('details.totalWithdrawals')}</span>
                            <span className="partner-details__financial-value partner-details__financial-value--negative">{formatCurrency(partner.totalWithdrawals)}</span>
                        </div>
                        <div className="partner-details__financial-card">
                            <span className="partner-details__financial-label">{t('details.currentBalance')}</span>
                            <span className={`partner-details__financial-value ${partner.currentBalance >= 0 ? 'partner-details__financial-value--positive' : 'partner-details__financial-value--negative'}`}>
                                {formatCurrency(partner.currentBalance)}
                            </span>
                        </div>
                    </div>
                </Card>
            </div>

            {/* ═══ Investments ═══ */}
            <div className="partner-details__section">
                <Card>
                    <h3 className="partner-details__section-title">{t('investment.title')}</h3>
                    {investments.length > 0 ? (
                        <table className="partner-details__table">
                            <thead>
                                <tr>
                                    <th>{t('investment.amount')}</th>
                                    <th>{t('investment.type')}</th>
                                    <th>{t('investment.status')}</th>
                                    <th>{t('investment.investedAt')}</th>
                                    <th>{t('investment.returnedAt')}</th>
                                    <th>{t('investment.notes')}</th>
                                </tr>
                            </thead>
                            <tbody>
                                {investments.map((inv: PartnerInvestment) => (
                                    <tr key={inv.id}>
                                        <td>{formatCurrency(inv.amount)}</td>
                                        <td>{inv.investmentType}</td>
                                        <td>
                                            <span className={`partner-details__inv-badge partner-details__inv-badge--${inv.status}`}>
                                                {t(`investmentStatus.${inv.status}`)}
                                            </span>
                                        </td>
                                        <td>{formatDateTime(inv.investedAt)}</td>
                                        <td>{inv.returnedAt ? formatDateTime(inv.returnedAt) : '—'}</td>
                                        <td>{inv.notes || '—'}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    ) : (
                        <div className="partner-details__placeholder">
                            <span className="partner-details__placeholder-icon">💰</span>
                            <span className="partner-details__placeholder-text">{t('investment.empty')}</span>
                        </div>
                    )}
                </Card>
            </div>

            {/* ═══ Withdrawals ═══ */}
            <div className="partner-details__section">
                <Card>
                    <h3 className="partner-details__section-title">{t('withdrawal.title')}</h3>
                    {withdrawals.length > 0 ? (
                        <table className="partner-details__table">
                            <thead>
                                <tr>
                                    <th>{t('withdrawal.amount')}</th>
                                    <th>{t('withdrawal.type')}</th>
                                    <th>{t('withdrawal.status')}</th>
                                    <th>{t('withdrawal.reason')}</th>
                                    <th>{t('withdrawal.requestedAt')}</th>
                                    <th>{t('withdrawal.approvedBy')}</th>
                                </tr>
                            </thead>
                            <tbody>
                                {withdrawals.map((w: PartnerWithdrawal) => (
                                    <tr key={w.id}>
                                        <td>{formatCurrency(w.amount)}</td>
                                        <td>{t(`withdrawalType.${w.withdrawalType}`)}</td>
                                        <td>
                                            <span className={`partner-details__inv-badge partner-details__inv-badge--${w.status}`}>
                                                {t(`withdrawalStatus.${w.status}`)}
                                            </span>
                                        </td>
                                        <td>{w.requestReason || '—'}</td>
                                        <td>{formatDateTime(w.requestedAt)}</td>
                                        <td>{w.approvedByName || '—'}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    ) : (
                        <div className="partner-details__placeholder">
                            <span className="partner-details__placeholder-icon">📤</span>
                            <span className="partner-details__placeholder-text">{t('withdrawal.empty')}</span>
                        </div>
                    )}
                </Card>
            </div>

            {/* ═══ Notes ═══ */}
            <div className="partner-details__section">
                <Card title={t('details.notes')}>
                    {partner.notes ? (
                        <p className="partner-details__notes">{partner.notes}</p>
                    ) : (
                        <p className="partner-details__notes partner-details__notes--empty">{t('details.noNotes')}</p>
                    )}
                </Card>
            </div>
        </div>
    )
}

