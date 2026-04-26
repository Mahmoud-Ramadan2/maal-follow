import { useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'

import { useScheduleMonthlySummary, useSchedulePayment, useSchedules } from '@hooks/modules'
import { useDebounce, usePagination } from '@hooks/common'
import Table from '@components/common/Table'
import type { TableColumn } from '@components/common/Table'
import Button from '@components/common/Button'
import Input from '@components/common/Input'
import Card from '@components/ui/Card'
import { APP_ROUTES, ROUTE_HELPERS } from '@/router/routes.config'
import { formatCurrency } from '@utils/helpers/format.helper'
import { formatDate } from '@utils/helpers/ index'
import type { InstallmentSchedule, PaymentStatus, ScheduleFilters } from '@/types/modules/schedule.types'
import './ScheduleListPage.css'

const PAGE_SIZE_OPTIONS = [10, 25, 50] as const


export default function ScheduleListPage(): ReactNode {
    const { t } = useTranslation('schedule')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()

    const [nameSearch, setNameSearch] = useState('')
    const [contractIdInput, setContractIdInput] = useState('')
    const [status, setStatus] = useState<PaymentStatus | 'ALL'>('ALL')
            const currentMonth = useMemo(() => new Date().toISOString().slice(0, 7), [])
            const [summaryMonth, setSummaryMonth] = useState(currentMonth)
    const [paymentDayInput, setPaymentDayInput] = useState('')
    const [dueSoonDaysInput, setDueSoonDaysInput] = useState('')
    const [startDate, setStartDate] = useState('')
    const [endDate, setEndDate] = useState('')
    const [overdueOnly, setOverdueOnly] = useState(false)

    const debouncedName = useDebounce(nameSearch, 400)
    const { page, size, nextPage, prevPage, setSize } = usePagination()
    const { openPaymentForm, sendReminder, loading: reminderLoading } = useSchedulePayment()
    const { summary, loading: summaryLoading, error: summaryError } = useScheduleMonthlySummary(summaryMonth)

    const filters = useMemo<ScheduleFilters>(() => ({
        page,
        size,
        ...(debouncedName.trim() ? { name: debouncedName.trim() } : {}),
        ...(contractIdInput.trim() ? { contractId: Number(contractIdInput) } : {}),
        ...(paymentDayInput.trim() ? { paymentDay: Number(paymentDayInput) } : {}),
        ...(dueSoonDaysInput.trim() ? { dueSoonDays: Number(dueSoonDaysInput) } : {}),
        ...(startDate ? { startDate } : {}),
        ...(endDate ? { endDate } : {}),
        ...(overdueOnly ? { overdueOnly: true } : {}),
        ...(status !== 'ALL' ? { status } : {}),
    }), [page, size, debouncedName, contractIdInput, paymentDayInput, dueSoonDaysInput, startDate, endDate, overdueOnly, status])

    const { schedules, loading, error, totalPages, totalElements, refetch } = useSchedules(filters)

    const from = totalElements === 0 ? 0 : page * size + 1
    const to = Math.min((page + 1) * size, totalElements)
    const hasSchedules = schedules.length > 0

    const clearFilters = () => {
        setNameSearch('')
        setContractIdInput('')
        setPaymentDayInput('')
        setDueSoonDaysInput('')
        setStartDate('')
        setEndDate('')
        setOverdueOnly(false)
        setStatus('ALL')
    }

    const columns: TableColumn<InstallmentSchedule>[] = [
        // { key: 'id', label: t('columns.id') },
        { key: 'sequenceNumber', label: t('columns.sequence') },
        { key: 'contractId', label: t('columns.contractNumber') },
        { key: 'customerName', label: t('columns.customerName') },
        { key: 'dueDate', label: t('columns.dueDate'), render: (row) => formatDate(row.dueDate) },
        { key: 'amount', label: t('columns.amount'), render: (row) => formatCurrency(row.amount) },
        { key: 'paidAmount', label: t('columns.paidAmount'), render: (row) => formatCurrency(row.paidAmount) },
        {
            key: 'status',
            label: t('columns.status'),
            render: (row) => (
                <span className={`schedule-list__badge schedule-list__badge--${row.status}`}>
                    {t(`status.${row.status}`)}
                </span>
            ),
        },
        { key: 'profitMonth', label: t('columns.profitMonth') },
        {
            key: 'actions',
            label: t('columns.actions'),
            render: (row) => {
                const scheduleId = row.id
                return (
                    <div className="schedule-list__actions">
                        <Button
                            size="sm"
                            onClick={() => openPaymentForm(row)}
                            disabled={!scheduleId || row.status === 'PAID' || row.status === 'CANCELLED'}
                        >
                            {t('actions.pay')}
                        </Button>
                        <Button
                            size="sm"
                            variant="secondary"
                            onClick={() => sendReminder(row)}
                            disabled={!scheduleId || reminderLoading || row.status  === 'PAID' || row.status === 'CANCELLED'}
                        >
                            {t('actions.remind')}
                        </Button>
                        <Button
                            size="sm"
                            variant="secondary"
                            onClick={() => scheduleId && navigate(ROUTE_HELPERS.SCHEDULE_VIEW(scheduleId), { state: { schedule: row } })}
                            disabled={!scheduleId}
                        >
                            {tc('view')}
                        </Button>
                        <Button
                            size="sm"
                            variant="secondary"
                            onClick={() => scheduleId && navigate(ROUTE_HELPERS.SCHEDULE_EDIT(scheduleId), { state: { schedule: row } })}
                            disabled={!scheduleId}
                        >
                            {tc('edit')}
                        </Button>
                    </div>
                )
            },
        },
    ]

    if (error && schedules.length === 0) {
        return (
            <div className="schedule-list__error">
                <p>{error}</p>
                <Button onClick={refetch}>{tc('retry')}</Button>
            </div>
        )
    }

    return (
        <div>
            <div className="schedule-list__header">
                <h1 className="schedule-list__title">{t('title')}</h1>
                <Button disabled={true} onClick={() => navigate(APP_ROUTES.SCHEDULES.CREATE)}>{t('createNew')}</Button>
            </div>

            <Card>
                <div className="schedule-list__summary">
                    <div className="schedule-list__summary-header">
                        <h3 className="schedule-list__summary-title">{t('monthlySummary.title')}</h3>
                        <Input
                            name="summaryMonth"
                            type="month"
                            value={summaryMonth}
                            onChange={(e) => setSummaryMonth(e.target.value)}
                        />
                    </div>
                    {summaryError ? (
                        <p className="schedule-list__summary-error">{summaryError}</p>
                    ) : (
                        <div className="schedule-list__summary-grid">
                            <div className="schedule-list__summary-item">
                                <span>{t('monthlySummary.expectedAmount')}</span>
                                <strong>{summaryLoading ? '...' : formatCurrency(summary?.expectedAmount ?? 0)}</strong>
                            </div>
                            <div className="schedule-list__summary-item">
                                <span>{t('monthlySummary.actualAmount')}</span>
                                <strong>{summaryLoading ? '...' : formatCurrency(summary?.actualAmount ?? 0)}</strong>
                            </div>
                            <div className="schedule-list__summary-item">
                                <span>{t('monthlySummary.shortfall')}</span>
                                <strong>{summaryLoading ? '...' : formatCurrency(summary?.shortfall ?? 0)}</strong>
                            </div>
                        </div>
                    )}
                </div>

                <div className="schedule-list__filters">
                    <Input
                        name="name"
                        placeholder={t('searchPlaceholder')}
                        value={nameSearch}
                        onChange={(e) => setNameSearch(e.target.value)}
                    />
                    <Input
                        name="contractId"
                        placeholder={t('filters.contractId')}
                        value={contractIdInput}
                        onChange={(e) => setContractIdInput(e.target.value)}
                        type="number"
                    />
                    <Input
                        name="paymentDay"
                        placeholder={t('filters.paymentDay')}
                        value={paymentDayInput}
                        onChange={(e) => setPaymentDayInput(e.target.value)}
                        type="number"
                    />
                    <Input
                        name="dueSoonDays"
                        placeholder={t('filters.dueSoonDays')}
                        value={dueSoonDaysInput}
                        onChange={(e) => setDueSoonDaysInput(e.target.value)}
                        type="number"
                    />
                    <Input
                        name="startDate"
                        placeholder={t('filters.startDate')}
                        value={startDate}
                        onChange={(e) => setStartDate(e.target.value)}
                        type="date"
                    />
                    <Input
                        name="endDate"
                        placeholder={t('filters.endDate')}
                        value={endDate}
                        onChange={(e) => setEndDate(e.target.value)}
                        type="date"
                    />
                    <select
                        className="schedule-list__select"
                        value={status}
                        onChange={(e) => setStatus(e.target.value as PaymentStatus | 'ALL')}
                    >
                        <option value="ALL">{t('filters.allStatuses')}</option>
                        <option value="PENDING">{t('status.PENDING')}</option>
                        <option value="PAID">{t('status.PAID')}</option>
                        <option value="LATE">{t('status.LATE')}</option>
                        <option value="PARTIALLY_PAID">{t('status.PARTIALLY_PAID')}</option>
                        <option value="CANCELLED">{t('status.CANCELLED')}</option>
                    </select>
                    <label className="schedule-list__checkbox">
                        <input
                            type="checkbox"
                            checked={overdueOnly}
                            onChange={(e) => setOverdueOnly(e.target.checked)}
                        />
                        <span>{t('filters.overdueOnly')}</span>
                    </label>
                    <Button variant="secondary" onClick={clearFilters}>{t('filters.clear')}</Button>
                </div>

                {loading ? (
                    <Table columns={columns} data={schedules} loading={true} emptyMessage={t('empty')} />
                ) : hasSchedules ? (
                    <>
                        <Table columns={columns} data={schedules} loading={false} emptyMessage={t('empty')} />

                        <div className="schedule-list__pagination">
                            <span>{t('pagination.showing', { from, to, total: totalElements })}</span>
                            <div className="schedule-list__pagination-controls">
                                <label htmlFor="schedule-page-size">{t('pagination.pageSize')}</label>
                                <select
                                    id="schedule-page-size"
                                    className="schedule-list__select"
                                    value={size}
                                    onChange={(e) => setSize(Number(e.target.value))}
                                >
                                    {PAGE_SIZE_OPTIONS.map((option) => (
                                        <option key={option} value={option}>{option}</option>
                                    ))}
                                </select>
                                <Button size="sm" variant="secondary" onClick={prevPage} disabled={page === 0}>
                                    {t('pagination.previous')}
                                </Button>
                                <Button size="sm" variant="secondary" onClick={nextPage} disabled={page + 1 >= totalPages}>
                                    {t('pagination.next')}
                                </Button>
                            </div>
                        </div>
                    </>
                ) : (
                    <div className="schedule-list__empty-state">
                        <span className="schedule-list__empty-icon" aria-hidden="true">📅</span>
                        <p className="schedule-list__empty-text">{t('emptyState.noSchedules')}</p>
                    </div>
                )}
            </Card>
        </div>
    )
}

