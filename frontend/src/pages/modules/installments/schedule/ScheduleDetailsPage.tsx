import type { ReactNode } from 'react'
import {useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'

import {useSchedule, useSchedulePayment} from '@hooks/modules'
import Button from '@components/common/Button'
import Card from '@components/ui/Card'
import { APP_ROUTES, ROUTE_HELPERS } from '@/router/routes.config'
import { formatCurrency } from '@utils/helpers/format.helper'
import './ScheduleDetailsPage.css'
import { formatDate } from '@utils/helpers/ index'
import LoadingSpinner from "@components/ui/LoadingSpinner";

// interface LocationState {
//     schedule?: InstallmentSchedule
// }



export default function ScheduleDetailsPage(): ReactNode {
    const { t } = useTranslation('schedule')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()
    const { id } = useParams<{ id: string }>()
    const scheduleId = Number(id)

    const { schedule, loading, error } = useSchedule(scheduleId)

    // const schedule = state?.schedule ?? null
    const { openPaymentForm, sendReminder, loading: reminderLoading } = useSchedulePayment()


    // ── Loading state ──────────────────────────────────────
    if (loading) {
        return <div className="schedule-details__center"><LoadingSpinner size="lg"/></div>
    }

    // ── Error state ────────────────────────────────────────
    if (error) {
        return (
            <div className="schedule-details__center">
                <p className="schedule-details__error-text">{error}</p>
                <Button onClick={() => navigate(APP_ROUTES.SCHEDULES.LIST)}>{t('details.backToList')}</Button>
            </div>
        )
    }
    // ── Not found ──────────────────────────────────────────

    if (!schedule) {
        return (
            <div className="schedule-details__center">
                <p>{t('details.notFound')}</p>
                <Button onClick={() => navigate(APP_ROUTES.SCHEDULES.LIST)}>{t('details.backToList')}</Button>
            </div>
        )
    }

    // const scheduleId = schedule.id

    return (
        <div>
            <div className="schedule-details__header">
                <h1 className="schedule-details__title">{t('details.title', { id: schedule.id })}</h1>
                <div className="schedule-details__actions">
                    <Button variant="secondary" onClick={() => navigate(APP_ROUTES.SCHEDULES.LIST)}>
                        {t('details.backToList')}
                    </Button>
                    <Button
                        onClick={() => schedule && openPaymentForm(schedule)}
                        disabled={schedule.status === 'PAID' || schedule.status === 'CANCELLED'}
                    >
                        {t('actions.pay')}
                    </Button>
                    <Button
                        variant="secondary"
                        onClick={() => schedule && sendReminder(schedule)}
                        disabled={reminderLoading || schedule.status === 'PAID' || schedule.status === 'CANCELLED'}
                    >
                        {t('actions.remind')}
                    </Button>
                    <Button
                        variant="secondary"
                        onClick={() => scheduleId && navigate(ROUTE_HELPERS.SCHEDULE_EDIT(scheduleId), { state: { schedule } })}
                        disabled={!scheduleId}
                    >
                        {tc('edit')}
                    </Button>
                </div>
            </div>
            <Card className="schedule-details__card">
                <div className="schedule-details__topbar">
        <span className={`schedule-details__badge schedule-details__badge--${schedule.status}`}>
            {t(`status.${schedule.status}`)}
        </span>
                    <span className="schedule-details__meta">
            #{schedule.sequenceNumber} · {t('columns.contractNumber')}: {schedule.contractId}
        </span>
                </div>

                <div className="schedule-details__sections">
                    {/* Dates */}
                    <section className="schedule-details__panel">
                        <h2 className="schedule-details__panel-title">{t('details.dates', 'Dates')}</h2>
                        <div className="schedule-details__kv">
                            <div className="schedule-details__item">
                                <span className="schedule-details__label">{t('columns.dueDate')}</span>
                                <span className="schedule-details__value schedule-details__value--strong">
                        {formatDate(schedule.dueDate)}
                    </span>
                            </div>
                            <div className="schedule-details__item">
                                <span className="schedule-details__label">{t('form.paidDate', 'Paid Date')}</span>
                                <span className="schedule-details__value">
                        {schedule.paidDate ? formatDate(schedule.paidDate) : t('details.notPaid', 'Not paid yet')}
                    </span>
                            </div>
                            <div className="schedule-details__item">
                                <span className="schedule-details__label">{t('columns.profitMonth')}</span>
                                <span className="schedule-details__value">{schedule.profitMonth || '—'}</span>
                            </div>
                        </div>
                    </section>

                    {/* Paid Money */}
                    <section className="schedule-details__panel">
                        <h2 className="schedule-details__panel-title">{t('details.paidMoney', 'Paid Money')}</h2>
                        <div className="schedule-details__kv">
                            <div className="schedule-details__item">
                                <span className="schedule-details__label">{t('form.paidAmount', 'Paid Amount')}</span>
                                <span className="schedule-details__value schedule-details__value--success">
                        {formatCurrency(schedule.paidAmount || 0)}
                    </span>
                            </div>
                            <div className="schedule-details__item">
                                <span className="schedule-details__label">{t('details.remaining', 'Remaining')}</span>
                                <span className="schedule-details__value schedule-details__value--warning">
                        {formatCurrency(Math.max((schedule.amount || 0) - (schedule.paidAmount || 0), 0))}
                    </span>
                            </div>
                        </div>
                    </section>

                    {/* Amounts */}
                    <section className="schedule-details__panel">
                        <h2 className="schedule-details__panel-title">{t('details.amounts', 'Amounts')}</h2>
                        <div className="schedule-details__kv">
                            <div className="schedule-details__item">
                                <span className="schedule-details__label">{t('form.originalAmount', 'Original Amount')}</span>
                                <span className="schedule-details__value">{formatCurrency(schedule.originalAmount || 0)}</span>
                            </div>
                            <div className="schedule-details__item">
                                <span className="schedule-details__label">{t('form.principalAmount', 'Principal Amount')}</span>
                                <span className="schedule-details__value">{formatCurrency(schedule.principalAmount || 0)}</span>
                            </div>
                            <div className="schedule-details__item">
                                <span className="schedule-details__label">{t('form.profitAmount', 'Profit Amount')}</span>
                                <span className="schedule-details__value">{formatCurrency(schedule.profitAmount || 0)}</span>
                            </div>
                            <div className="schedule-details__item">
                                <span className="schedule-details__label">{t('form.discountApplied')}</span>
                                <span className="schedule-details__value">{formatCurrency(schedule.discountApplied || 0)}</span>
                            </div>
                            <div className="schedule-details__item">
                                <span className="schedule-details__label">{t('columns.amount')}</span>
                                <span className="schedule-details__value schedule-details__value--strong">
                        {formatCurrency(schedule.amount || 0)}
                    </span>
                            </div>
                        </div>
                    </section>

                    {/* Other */}
                    <section className="schedule-details__panel">
                        <h2 className="schedule-details__panel-title">{t('details.other', 'Other')}</h2>
                        <div className="schedule-details__kv">
                            <div className="schedule-details__item">
                                <span className="schedule-details__label">{t('columns.customerName')}</span>
                                <span className="schedule-details__value">{schedule.customerName || '—'}</span>
                            </div>
                            <div className="schedule-details__item">
                                <span className="schedule-details__label">{t('details.customerPhone', 'Customer Phone')}</span>
                                <span className="schedule-details__value">{schedule.customerPhone || '—'}</span>
                            </div>
                            <div className="schedule-details__item">
                                <span className="schedule-details__label">{t('form.collectorName', 'Collector')}</span>
                                <span className="schedule-details__value">{schedule.collectorName || '—'}</span>
                            </div>
                            <div className="schedule-details__item">
                                <span className="schedule-details__label">{t('form.isFinalPayment')}</span>
                                <span className="schedule-details__value">
                        {schedule.isFinalPayment ? t('details.yes', 'Yes') : t('details.no', 'No')}
                    </span>
                            </div>
                            <div className="schedule-details__item schedule-details__item--full">
                                <span className="schedule-details__label">{t('form.notes')}</span>
                                <span className="schedule-details__value schedule-details__notes">
                        {schedule.notes || '—'}
                    </span>
                            </div>
                        </div>
                    </section>
                </div>
            </Card>
        </div>
    )
}