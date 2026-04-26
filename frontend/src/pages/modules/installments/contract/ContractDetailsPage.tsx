import type { ReactNode } from 'react'
import { useMemo, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useContract, useContractComplete, useInstallmentActions, useSchedulePayment } from '@hooks/modules'
import Button from '@components/common/Button'
import Input from '@components/common/Input'
import Card from '@components/ui/Card'
import LoadingSpinner from '@components/ui/LoadingSpinner'
import { APP_ROUTES, ROUTE_HELPERS } from '@/router/routes.config'
import { formatCurrency } from '@utils/helpers/format.helper'
import { formatDate, formatDateTime } from '@utils/helpers/ index'
import type { ContractExpense } from '@/types/modules/contract.types'
import type { InstallmentSchedule, PaymentStatus } from '@/types/modules/schedule.types'
import './ContractDetailsPage.css'

export default function ContractDetailsPage(): ReactNode {
    const { id } = useParams<{ id: string }>()
    const contractId = Number(id)
    const navigate = useNavigate()
    const { t } = useTranslation('contract')
    const { t: tc } = useTranslation('common')
    const [schedulePage, setSchedulePage] = useState(0)
    const [scheduleSize, setScheduleSize] = useState(10)
    const [scheduleStatusFilter, setScheduleStatusFilter] = useState<PaymentStatus | 'ALL'>('ALL')
    const [scheduleDueSoonInput, setScheduleDueSoonInput] = useState('')
    const [scheduleStartDate, setScheduleStartDate] = useState('')
    const [scheduleEndDate, setScheduleEndDate] = useState('')
    const [scheduleOverdueOnly, setScheduleOverdueOnly] = useState(false)
    const [rescheduleMonthsInput, setRescheduleMonthsInput] = useState('')
    const [rescheduleAmountInput, setRescheduleAmountInput] = useState('')
    const [rescheduleStartDateInput, setRescheduleStartDateInput] = useState('')
    const [skipReason, setSkipReason] = useState('')

    const scheduleFilters = useMemo(() => ({
        ...(scheduleStatusFilter !== 'ALL' ? { status: scheduleStatusFilter } : {}),
        ...(scheduleDueSoonInput.trim() ? { dueSoonDays: Number(scheduleDueSoonInput) } : {}),
        ...(scheduleStartDate ? { startDate: scheduleStartDate } : {}),
        ...(scheduleEndDate ? { endDate: scheduleEndDate } : {}),
        ...(scheduleOverdueOnly ? { overdueOnly: true } : {}),
    }), [scheduleDueSoonInput, scheduleEndDate, scheduleOverdueOnly, scheduleStartDate, scheduleStatusFilter])

    const {
        contract,
        schedules,
        scheduleTotalPages,
        scheduleTotalElements,
        expenses,
        loading,
        error,
        refetch,
    } = useContract(contractId, { schedulePage, scheduleSize, scheduleFilters })
    const { completeContract, loading: completeLoading } = useContractComplete()
    const {
        generate,
        rescheduleUnpaidInstallments,
        skipMonthPayment,
        updateScheduleMetadata,
        swapRemainder,
        deleteUnpaid,
        loading: scheduleActionLoading,
    } = useInstallmentActions()
    const { openPaymentForm, sendReminder, loading: reminderLoading } = useSchedulePayment()

    const handleComplete = async () => {
        const done = await completeContract(contractId)
        if (done) refetch()
    }

    const handleGenerate = async () => {
        const result = await generate(contractId)
        if (result) refetch()
    }

    const handleSwap = async () => {
        const result = await swapRemainder(contractId)
        if (result) refetch()
    }

    const handleDeleteUnpaid = async () => {
        const ok = await deleteUnpaid(contractId)
        if (ok) refetch()
    }

    const handleReschedule = async () => {
        const result = await rescheduleUnpaidInstallments(contractId, {
            ...(rescheduleMonthsInput.trim() ? { newNumberOfMonths: Number(rescheduleMonthsInput) } : {}),
            ...(rescheduleAmountInput.trim() ? { newMonthlyAmount: Number(rescheduleAmountInput) } : {}),
            ...(rescheduleStartDateInput ? { newStartDate: rescheduleStartDateInput } : {}),
        })
        if (result) refetch()
    }

    const handleSkipMonth = async () => {
        if (!skipReason.trim()) return
        const ok = await skipMonthPayment(contractId, skipReason.trim())
        if (ok) {
            setSkipReason('')
            refetch()
        }
    }

    const handleMetadataUpdate = async (schedule: InstallmentSchedule) => {
        if (!schedule.id) return
        const dueDate = window.prompt(t('schedule.metadataDueDatePrompt'), schedule.dueDate || '')
        if (dueDate === null) return
        const notes = window.prompt(t('schedule.metadataNotesPrompt'), schedule.notes || '')
        if (notes === null) return

        const result = await updateScheduleMetadata(schedule.id, {
            dueDate: dueDate || undefined,
            notes: notes || undefined,
        })
        if (result) refetch()
    }

    const clearScheduleFilters = () => {
        setScheduleStatusFilter('ALL')
        setScheduleDueSoonInput('')
        setScheduleStartDate('')
        setScheduleEndDate('')
        setScheduleOverdueOnly(false)
        setSchedulePage(0)
    }

    const scheduleFrom = scheduleTotalElements === 0 ? 0 : (schedulePage * scheduleSize) + 1
    const scheduleTo = Math.min((schedulePage + 1) * scheduleSize, scheduleTotalElements)

    if (loading) {
        return <div className="contract-details__center"><LoadingSpinner size="lg" /></div>
    }

    if (error) {
        return (
            <div className="contract-details__center">
                <p className="contract-details__error-text">{error}</p>
                <Button onClick={() => navigate(APP_ROUTES.CONTRACTS.LIST)}>{t('details.backToList')}</Button>
            </div>
        )
    }

    if (!contract) {
        return (
            <div className="contract-details__center">
                <p className="contract-details__error-text">{t('details.notFound')}</p>
                <Button onClick={() => navigate(APP_ROUTES.CONTRACTS.LIST)}>{t('details.backToList')}</Button>
            </div>
        )
    }

    return (
        <div>
            {/* ═══ 1. Header ═══ */}
            <div className="contract-details__header">
                <div className="contract-details__header-start">
                    <Button variant="secondary" size="sm" onClick={() => navigate(APP_ROUTES.CONTRACTS.LIST)}>
                        ← {t('details.backToList')}
                    </Button>
                    <h1 className="contract-details__title">
                        {t('details.title', { id: contract.id })}
                    </h1>
                    <span className="contract-details__value">
                            <span className={`contract-details__badge contract-details__badge--${contract.status}`}>
                                <span className="contract-details__badge-dot" />
                                {t(`status.${contract.status}`)}
                            </span>
                        </span>
                </div>
                <div className="contract-details__header-actions">
                    <Button variant="secondary" onClick={() => navigate(ROUTE_HELPERS.CONTRACT_EDIT(contractId))}>
                        {tc('edit')}
                    </Button>
                    {contract.status !== 'COMPLETED' && (
                        <Button variant="danger" onClick={handleComplete} loading={completeLoading}>
                            {t('details.markComplete')}
                        </Button>
                    )}
                </div>
            </div>

            {/* ═══ 2. Contract Info ═══ */}
            <Card title={t('details.contractInfo')}>
                <div className="contract-details__grid">
                    <div className="contract-details__field">
                        <span className="contract-details__label">{t('details.contractNumber')}</span>
                        <span className="contract-details__value">{contract.id}</span>
                    </div>
                    <div className="contract-details__field">
                        <span className="contract-details__label">{t('details.customerName')}</span>
                        <span className="contract-details__value">{contract.customerName}</span>
                    </div>
                    <div className="contract-details__field">
                        <span className="contract-details__label">{t('details.productName')}</span>
                        <span className="contract-details__value">{contract.productName}</span>
                    </div>
                    <div className="contract-details__field">
                        <span className="contract-details__label">{t('details.vendorName')}</span>
                        <span className="contract-details__value">{contract.vendorName}</span>
                    </div>
                    <div className="contract-details__field">
                        <span className="contract-details__label">{t('details.partnerName')}</span>
                        <span className="contract-details__value">{contract.partnerName || '—'}</span>
                    </div>
                    <div className="contract-details__field">
                        <span className="contract-details__label">{t('details.responsibleUser')}</span>
                        <span className="contract-details__value">{contract.responsibleUserName || '—'}</span>
                    </div>
                    <div className="contract-details__field">
                        <span className="contract-details__label">{t('details.status')}</span>
                        <span className="contract-details__value">
                            <span className={`contract-details__badge contract-details__badge--${contract.status}`}>
                                <span className="contract-details__badge-dot" />
                                {t(`status.${contract.status}`)}
                            </span>
                        </span>
                    </div>
                    <div className="contract-details__field">
                        <span className="contract-details__label">{t('details.startDate')}</span>
                        <span className="contract-details__value">{formatDate(contract.startDate)}</span>
                    </div>
                    <div className="contract-details__field">
                        <span className="contract-details__label">{t('details.completionDate')}</span>
                        <span className="contract-details__value">{contract.completionDate ? formatDate(contract.completionDate) : '—'}</span>
                    </div>
                    <div className="contract-details__field">
                        <span className="contract-details__label">{t('details.months')}</span>
                        <span className="contract-details__value">{contract.months}</span>
                    </div>
                    <div className="contract-details__field">
                        <span className="contract-details__label">{t('details.agreedPaymentDay')}</span>
                        <span className="contract-details__value">{contract.agreedPaymentDay}</span>
                    </div>
                    <div className="contract-details__field">
                        <span className="contract-details__label">{t('details.createdAt')}</span>
                        <span className="contract-details__value">{formatDateTime(contract.createdAt)}</span>
                    </div>
                </div>
            </Card>

            {/* ═══ 3. Financial Summary ═══ */}
            <div className="contract-details__section">
                <Card title={t('details.financialSummary')}>
                    <div className="contract-details__financial">
                        <div className="contract-details__financial-card">
                            <span className="contract-details__financial-label">{t('details.originalPrice')}</span>
                            <span className="contract-details__financial-value">{formatCurrency(contract.originalPrice)}</span>
                        </div>
                        <div className="contract-details__financial-card">
                            <span className="contract-details__financial-label">{t('details.finalPrice')}</span>
                            <span className="contract-details__financial-value">{formatCurrency(contract.finalPrice)}</span>
                        </div>
                        <div className="contract-details__financial-card">
                            <span className="contract-details__financial-label">{t('details.downPayment')}</span>
                            <span className="contract-details__financial-value">{formatCurrency(contract.downPayment)}</span>
                        </div>
                        <div className="contract-details__financial-card">
                            <span className="contract-details__financial-label">{t('details.remainingAmount')}</span>
                            <span className="contract-details__financial-value">{formatCurrency(contract.remainingAmount)}</span>
                        </div>
                        <div className="contract-details__financial-card">
                            <span className="contract-details__financial-label">{t('details.monthlyAmount')}</span>
                            <span className="contract-details__financial-value">{formatCurrency(contract.monthlyAmount)}</span>
                        </div>
                        <div className="contract-details__financial-card">
                            <span className="contract-details__financial-label">{t('details.profitAmount')}</span>
                            <span className="contract-details__financial-value contract-details__financial-value--positive">{formatCurrency(contract.profitAmount)}</span>
                        </div>
                        <div className="contract-details__financial-card">
                            <span className="contract-details__financial-label">{t('details.totalExpenses')}</span>
                            <span className="contract-details__financial-value contract-details__financial-value--negative">{formatCurrency(contract.totalExpenses)}</span>
                        </div>
                        <div className="contract-details__financial-card">
                            <span className="contract-details__financial-label">{t('details.netProfit')}</span>
                            <span className={`contract-details__financial-value ${contract.netProfit >= 0 ? 'contract-details__financial-value--positive' : 'contract-details__financial-value--negative'}`}>
                                {formatCurrency(contract.netProfit)}
                            </span>
                        </div>
                        <div className="contract-details__financial-card">
                            <span className="contract-details__financial-label">{t('details.capitalAllocated')}</span>
                            <span className="contract-details__financial-value">{formatCurrency(contract.capitalAllocated)}</span>
                        </div>
                        <div className="contract-details__financial-card">
                            <span className="contract-details__financial-label">{t('details.capitalReturned')}</span>
                            <span className="contract-details__financial-value">{formatCurrency(contract.capitalReturned)}</span>
                        </div>
                    </div>
                </Card>
            </div>

            {/* ═══ 4. Installment Schedule ═══ */}
            <div className="contract-details__section">
                <Card>
                    <div className="contract-details__section-header">
                        <h3 className="contract-details__section-title">{t('schedule.title')}</h3>
                        <div className="contract-details__section-actions">
                            {schedules.length === 0 && (
                                <Button size="sm" onClick={handleGenerate} loading={scheduleActionLoading}>
                                    {t('schedule.generate')}
                                </Button>
                            )}
                            {schedules.length > 1 && (
                                <>
                                    <Button size="sm" variant="secondary" onClick={handleSwap} loading={scheduleActionLoading}>
                                        {t('schedule.swapRemainder')}
                                    </Button>
                                    <Button disabled={true} size="sm" variant="danger" onClick={handleDeleteUnpaid} loading={scheduleActionLoading}>
                                        {t('schedule.deleteUnpaid')}
                                    </Button>
                                </>
                            )}
                        </div>
                    </div>

                    <div className="contract-details__schedule-toolbar">
                        <select
                            className="contract-details__pagination-select"
                            value={scheduleStatusFilter}
                            onChange={(e) => {
                                setSchedulePage(0)
                                setScheduleStatusFilter(e.target.value as PaymentStatus | 'ALL')
                            }}
                        >
                            <option value="ALL">{t('schedule.allStatuses')}</option>
                            <option value="PENDING">{t('paymentStatus.PENDING')}</option>
                            <option value="PAID">{t('paymentStatus.PAID')}</option>
                            <option value="LATE">{t('paymentStatus.LATE')}</option>
                            <option value="PARTIALLY_PAID">{t('paymentStatus.PARTIALLY_PAID')}</option>
                            <option value="CANCELLED">{t('paymentStatus.CANCELLED')}</option>
                        </select>
                        <Input
                            name="scheduleDueSoonDays"
                            type="number"
                            placeholder={t('schedule.dueSoonDays')}
                            value={scheduleDueSoonInput}
                            onChange={(e) => {
                                setSchedulePage(0)
                                setScheduleDueSoonInput(e.target.value)
                            }}
                        />
                        <Input
                            name="scheduleStartDate"
                            type="date"
                            value={scheduleStartDate}
                            onChange={(e) => {
                                setSchedulePage(0)
                                setScheduleStartDate(e.target.value)
                            }}
                        />
                        <Input
                            name="scheduleEndDate"
                            type="date"
                            value={scheduleEndDate}
                            onChange={(e) => {
                                setSchedulePage(0)
                                setScheduleEndDate(e.target.value)
                            }}
                        />
                        <label className="contract-details__filter-checkbox">
                            <input
                                type="checkbox"
                                checked={scheduleOverdueOnly}
                                onChange={(e) => {
                                    setSchedulePage(0)
                                    setScheduleOverdueOnly(e.target.checked)
                                }}
                            />
                            <span>{t('schedule.overdueOnly')}</span>
                        </label>
                        <Button size="sm" variant="secondary" onClick={clearScheduleFilters}>
                            {t('schedule.clearFilters')}
                        </Button>
                    </div>

                    <div className="contract-details__schedule-toolbar">
                        <Input
                            name="rescheduleMonths"
                            type="number"
                            placeholder={t('schedule.newNumberOfMonths')}
                            value={rescheduleMonthsInput}
                            onChange={(e) => setRescheduleMonthsInput(e.target.value)}
                        />
                        <Input
                            name="rescheduleAmount"
                            type="number"
                            placeholder={t('schedule.newMonthlyAmount')}
                            value={rescheduleAmountInput}
                            onChange={(e) => setRescheduleAmountInput(e.target.value)}
                        />
                        <Input
                            name="rescheduleStartDate"
                            type="date"
                            value={rescheduleStartDateInput}
                            onChange={(e) => setRescheduleStartDateInput(e.target.value)}
                        />
                        <Button size="sm" variant="secondary" onClick={handleReschedule} loading={scheduleActionLoading}>
                            {t('schedule.reschedule')}
                        </Button>
                        <Input
                            name="skipMonthReason"
                            placeholder={t('schedule.skipMonthReason')}
                            value={skipReason}
                            onChange={(e) => setSkipReason(e.target.value)}
                        />
                        <Button
                            size="sm"
                            variant="secondary"
                            onClick={handleSkipMonth}
                            loading={scheduleActionLoading}
                            disabled={!skipReason.trim()}
                        >
                            {t('schedule.skipMonth')}
                        </Button>
                    </div>

                    {schedules.length > 0 ? (
                        <>
                            <table className="contract-details__table">
                                <thead>
                                    <tr>
                                        <th>{t('schedule.sequence')}</th>
                                        <th>{t('schedule.dueDate')}</th>
                                        <th>{t('schedule.amount')}</th>
                                        <th>{t('schedule.paidAmount')}</th>
                                        <th>{t('schedule.paidDate')}</th>
                                        <th>{t('schedule.status')}</th>
                                        <th>{t('schedule.profitMonth')}</th>
                                        <th>{t('schedule.notes')}</th>
                                        <th>{t('schedule.actions')}</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {schedules.map((s: InstallmentSchedule, idx: number) => (
                                        <tr key={idx}>
                                            <td>{s.sequenceNumber}{s.isFinalPayment ? ' ★' : ''}</td>
                                            <td>{formatDate(s.dueDate)}</td>
                                            <td>{formatCurrency(s.amount)}</td>
                                            <td>{s.paidAmount ? formatCurrency(s.paidAmount) : '—'}</td>
                                            <td>{s.paidDate ? formatDate(s.paidDate) : '—'}</td>
                                            <td>
                                                <span className={`contract-details__payment-badge contract-details__payment-badge--${s.status}`}>
                                                    {t(`paymentStatus.${s.status}`)}
                                                </span>
                                            </td>
                                            <td>{s.profitMonth || '—'}</td>
                                            <td>{s.notes || '—'}</td>
                                            <td>
                                                <div className="contract-details__row-actions">
                                                    <Button
                                                        size="sm"
                                                        onClick={() => openPaymentForm(s)}
                                                        disabled={!s.id || s.status === 'PAID' || s.status === 'CANCELLED'}
                                                    >
                                                        {t('schedule.pay')}
                                                    </Button>
                                                    <Button
                                                        size="sm"
                                                        variant="secondary"
                                                        onClick={() => sendReminder(s)}
                                                        disabled={!s.id || reminderLoading}
                                                    >
                                                        {t('schedule.remind')}
                                                    </Button>
                                                    <Button
                                                        size="sm"
                                                        variant="secondary"
                                                        onClick={() => handleMetadataUpdate(s)}
                                                        disabled={!s.id || scheduleActionLoading}
                                                    >
                                                        {t('schedule.updateMetadata')}
                                                    </Button>
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>

                            <div className="contract-details__pagination">
                                <span className="contract-details__pagination-info">
                                    {t('pagination.showing', {
                                        from: scheduleFrom,
                                        to: scheduleTo,
                                        total: scheduleTotalElements,
                                    })}
                                </span>
                                <div className="contract-details__pagination-actions">
                                    <label htmlFor="schedule-page-size">{t('pagination.pageSize')}</label>
                                    <select
                                        id="schedule-page-size"
                                        className="contract-details__pagination-select"
                                        value={scheduleSize}
                                        onChange={(e) => {
                                            setSchedulePage(0)
                                            setScheduleSize(Number(e.target.value))
                                        }}
                                    >
                                        <option value={10}>10</option>
                                        <option value={25}>25</option>
                                        <option value={50}>50</option>
                                    </select>
                                    <Button
                                        size="sm"
                                        variant="secondary"
                                        onClick={() => setSchedulePage((prev) => Math.max(0, prev - 1))}
                                        disabled={schedulePage === 0}
                                    >
                                        {t('pagination.previous')}
                                    </Button>
                                    <Button
                                        size="sm"
                                        variant="secondary"
                                        onClick={() => setSchedulePage((prev) => prev + 1)}
                                        disabled={schedulePage + 1 >= scheduleTotalPages}
                                    >
                                        {t('pagination.next')}
                                    </Button>
                                </div>
                            </div>
                        </>
                    ) : (
                        <div className="contract-details__placeholder">
                            <span className="contract-details__placeholder-icon">📅</span>
                            <span className="contract-details__placeholder-text">{t('schedule.empty')}</span>
                        </div>
                    )}
                </Card>
            </div>

            {/* ═══ 5. Expenses ═══ */}
            <div className="contract-details__section">
                <Card>
                    <div className="contract-details__section-header">
                        <h3 className="contract-details__section-title">{t('expense.title')}</h3>
                    </div>

                    {expenses.length > 0 ? (
                        <table className="contract-details__table">
                            <thead>
                                <tr>
                                    <th>{t('expense.type')}</th>
                                    <th>{t('expense.amount')}</th>
                                    <th>{t('expense.description')}</th>
                                    <th>{t('expense.date')}</th>
                                    <th>{t('expense.paidBy')}</th>
                                    <th>{t('expense.receipt')}</th>
                                    <th>{t('expense.createdBy')}</th>
                                </tr>
                            </thead>
                            <tbody>
                                {expenses.map((e: ContractExpense, idx: number) => (
                                    <tr key={idx}>
                                        <td>{t(`expenseType.${e.expenseType}`)}</td>
                                        <td>{formatCurrency(e.amount)}</td>
                                        <td>{e.description || '—'}</td>
                                        <td>{formatDate(e.expenseDate)}</td>
                                        <td>{e.paidBy ? t(`paidBy.${e.paidBy}`) : '—'}</td>
                                        <td>{e.receiptNumber || '—'}</td>
                                        <td>{e.createdByName || '—'}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    ) : (
                        <div className="contract-details__placeholder">
                            <span className="contract-details__placeholder-icon">💰</span>
                            <span className="contract-details__placeholder-text">{t('expense.empty')}</span>
                        </div>
                    )}
                </Card>
            </div>

            {/* ═══ 6. Notes ═══ */}
            <div className="contract-details__section">
                <Card title={t('details.notes')}>
                    {contract.notes ? (
                        <p className="contract-details__notes">{contract.notes}</p>
                    ) : (
                        <p className="contract-details__notes contract-details__notes--empty">{t('details.noNotes')}</p>
                    )}
                </Card>
            </div>
        </div>
    )
}

