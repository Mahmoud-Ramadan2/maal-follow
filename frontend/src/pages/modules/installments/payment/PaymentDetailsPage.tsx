import type { ReactNode } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { usePaymentDetail, usePaymentActions } from '@hooks/modules'
import Button from '@components/common/Button'
import Card from '@components/ui/Card'
import LoadingSpinner from '@components/ui/LoadingSpinner'
import { APP_ROUTES } from '@/router/routes.config'
import { formatCurrency } from '@utils/helpers/format.helper'
import { formatDate, formatDateTime } from '@utils/helpers/ index'
import { PaymentStatus } from '@/types/modules/payment.types'
import './PaymentDetailsPage.css'

export default function PaymentDetailsPage(): ReactNode {
    const { id } = useParams<{ id: string }>()
    const paymentId = Number(id)
    const navigate = useNavigate()
    const { t } = useTranslation('payment')
    const { t: tc } = useTranslation('common')

    const { payment, loading, error, refetch } = usePaymentDetail(paymentId)
    const { cancelPayment, refundPayment, loading: actionLoading } = usePaymentActions()

    const handleCancel = async () => {
        const result = await cancelPayment(paymentId)
        if (result) refetch()
    }

    const handleRefund = async () => {
        const result = await refundPayment(paymentId)
        if (result) refetch()
    }

    if (loading) {
        return <div className="payment-details__center"><LoadingSpinner size="lg" /></div>
    }

    if (error || !payment) {
        return (
            <div className="payment-details__center">
                <p className="payment-details__error-text">{error || t('details.notFound')}</p>
                <Button onClick={() => navigate(APP_ROUTES.PAYMENTS.LIST)}>{t('details.backToList')}</Button>
            </div>
        )
    }

    const canCancel = payment.status === PaymentStatus.PENDING || payment.status === PaymentStatus.COMPLETED
    const canRefund = payment.status === PaymentStatus.COMPLETED

    return (
        <div>
            {/* ═══ Header ═══ */}
            <div className="payment-details__header">
                <div className="payment-details__header-start">
                    <Button variant="secondary" size="sm" onClick={() => navigate(APP_ROUTES.PAYMENTS.LIST)}>
                        ← {t('details.backToList')}
                    </Button>
                    <h1 className="payment-details__title">{t('details.title', { id: payment.id })}</h1>
                </div>
                <div className="payment-details__header-actions">
                    {canRefund && (
                        <Button variant="secondary" onClick={handleRefund} loading={actionLoading}>
                            {tc('refund', 'Refund')}
                        </Button>
                    )}
                    {canCancel && (
                        <Button variant="danger" onClick={handleCancel} loading={actionLoading}>
                            {tc('cancel')}
                        </Button>
                    )}
                </div>
            </div>

            {/* ═══ Payment Info ═══ */}
            <Card title={t('details.paymentInfo')}>
                <div className="payment-details__grid">
                    <div className="payment-details__field">
                        <span className="payment-details__label">{t('details.id')}</span>
                        <span className="payment-details__value">#{payment.id}</span>
                    </div>
                    <div className="payment-details__field">
                        <span className="payment-details__label">{t('details.idempotencyKey')}</span>
                        <span className="payment-details__value">{payment.idempotencyKey}</span>
                    </div>
                    <div className="payment-details__field">
                        <span className="payment-details__label">{t('details.status')}</span>
                        <span className="payment-details__value">
                            <span className={`payment-details__badge payment-details__badge--${payment.status}`}>
                                <span className="payment-details__badge-dot" />
                                {t(`status.${payment.status}`)}
                            </span>
                        </span>
                    </div>
                    <div className="payment-details__field">
                        <span className="payment-details__label">{t('details.paymentMethod')}</span>
                        <span className="payment-details__value">{t(`paymentMethod.${payment.paymentMethod}`)}</span>
                    </div>
                    <div className="payment-details__field">
                        <span className="payment-details__label">{t('details.actualPaymentDate')}</span>
                        <span className="payment-details__value">{formatDate(payment.actualPaymentDate)}</span>
                    </div>
                    <div className="payment-details__field">
                        <span className="payment-details__label">{t('details.agreedPaymentMonth')}</span>
                        <span className="payment-details__value">{payment.agreedPaymentMonth || '—'}</span>
                    </div>
                    <div className="payment-details__field">
                        <span className="payment-details__label">{t('details.isEarlyPayment')}</span>
                        <span className="payment-details__value">{payment.isEarlyPayment ? t('details.yes') : t('details.no')}</span>
                    </div>
                    <div className="payment-details__field">
                        <span className="payment-details__label">{t('details.receivedByName')}</span>
                        <span className="payment-details__value">{payment.receivedByName || '—'}</span>
                    </div>
                    <div className="payment-details__field">
                        <span className="payment-details__label">{t('details.collectorName')}</span>
                        <span className="payment-details__value">{payment.collectorName || '—'}</span>
                    </div>
                    <div className="payment-details__field">
                        <span className="payment-details__label">{t('details.createdAt')}</span>
                        <span className="payment-details__value">{formatDateTime(payment.createdAt)}</span>
                    </div>
                </div>
            </Card>

            {/* ═══ Financial Summary ═══ */}
            <div className="payment-details__financial">
                <div className="payment-details__financial-card">
                    <span className="payment-details__financial-label">{t('details.amount')}</span>
                    <span className="payment-details__financial-value">{formatCurrency(payment.amount)}</span>
                </div>
                <div className="payment-details__financial-card">
                    <span className="payment-details__financial-label">{t('details.discountAmount')}</span>
                    <span className="payment-details__financial-value payment-details__financial-value--negative">
                        {formatCurrency(payment.discountAmount)}
                    </span>
                </div>
                <div className="payment-details__financial-card">
                    <span className="payment-details__financial-label">{t('details.netAmount')}</span>
                    <span className="payment-details__financial-value payment-details__financial-value--positive">
                        {formatCurrency(payment.netAmount)}
                    </span>
                </div>
            </div>

            {/* ═══ Notes ═══ */}
            <div className="payment-details__section">
                <Card title={t('details.notes')}>
                    {payment.notes ? (
                        <p className="payment-details__notes">{payment.notes}</p>
                    ) : (
                        <p className="payment-details__notes payment-details__notes--empty">{t('details.noNotes')}</p>
                    )}
                </Card>
            </div>
        </div>
    )
}

