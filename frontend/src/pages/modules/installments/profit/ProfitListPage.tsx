import { useCallback, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'

import {
    useProfitDistributions,
    useProfitDistributionActions,
} from '@hooks/modules'
import type { TableColumn } from '@components/common/Table'
import Table from '@components/common/Table'
import Button from '@components/common/Button'
import Input from '@components/common/Input'
import Card from '@components/ui/Card'
import Modal from '@components/common/Modal/Modal'
import { APP_ROUTES, ROUTE_HELPERS } from '@/router/routes.config'
import { ProfitDistributionStatus } from '@/types/modules/profit.types'
import type {
    MonthlyProfitDistribution,
    ProfitDistributionFilters,
    ProfitDistributionStatus as ProfitStatus,
} from '@/types/modules/profit.types'
import { formatCurrency } from '@utils/helpers/format.helper'
import { formatDateTime } from '@utils/helpers/ index'
import { profitApi } from '@services/api/modules/profit.api'
import './ProfitListPage.css'

type StatusFilter = ProfitStatus | 'ALL'
const STATUS_TABS: StatusFilter[] = [
    'ALL',
    ProfitDistributionStatus.PENDING,
    ProfitDistributionStatus.CALCULATED,
    ProfitDistributionStatus.DISTRIBUTED,
    ProfitDistributionStatus.LOCKED,
]

export default function ProfitListPage(): ReactNode {
    const { t } = useTranslation('profit')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()

    const [activeTab, setActiveTab] = useState<StatusFilter>('ALL')
    const [startMonth, setStartMonth] = useState('')
    const [endMonth, setEndMonth] = useState('')
    const [monthLookup, setMonthLookup] = useState('')
    const [lookupError, setLookupError] = useState<string | null>(null)
    const [actionModalOpen, setActionModalOpen] = useState(false)
    const [actionError, setActionError] = useState<string | null>(null)
    const [selectedAction, setSelectedAction] = useState<'calculate' | 'distribute' | 'lock' | null>(null)
    const [selectedDistribution, setSelectedDistribution] = useState<MonthlyProfitDistribution | null>(null)

    const filters = useMemo<ProfitDistributionFilters>(() => {
        const f: ProfitDistributionFilters = {}
        if (activeTab !== 'ALL') f.status = activeTab
        if (startMonth) f.startMonth = startMonth
        if (endMonth) f.endMonth = endMonth
        return f
    }, [activeTab, startMonth, endMonth])

    const { distributions, loading, error, refetch } = useProfitDistributions(filters)
    const actions = useProfitDistributionActions()


    const actionEnabled = (
        action: 'calculate' | 'distribute' | 'lock',
        status: ProfitStatus,
    ): boolean => {
        if (action === 'calculate') return status === ProfitDistributionStatus.PENDING
        if (action === 'distribute') return status === ProfitDistributionStatus.CALCULATED
        return status !== ProfitDistributionStatus.LOCKED
    }

    const actionBlockedReason = (
        action: 'calculate' | 'distribute' | 'lock',
        status: ProfitStatus,
    ): string => {
        if (actionEnabled(action, status)) return ''
        return t('actions.transitionNotAllowed')
    }

    const openActionModal = (
        action: 'calculate' | 'distribute' | 'lock',
        distribution: MonthlyProfitDistribution,
    ) => {
        setSelectedAction(action)
        setSelectedDistribution(distribution)
        setActionError(null)
        setActionModalOpen(true)
    }

    const closeActionModal = () => {
        setActionModalOpen(false)
        setSelectedAction(null)
        setSelectedDistribution(null)
        setActionError(null)
    }

    const confirmAction = async () => {
        if (!selectedAction || !selectedDistribution) return
        if (!actionEnabled(selectedAction, selectedDistribution.status)) {
            setActionError(t('actions.transitionNotAllowed'))
            return
        }
        const result = await actions[selectedAction](selectedDistribution.id)
        if (!result) {
            setActionError(actions.error ?? t(`actions.${selectedAction}.error`))
            return
        }
        closeActionModal()
        refetch()
    }



    const handleLookupMonth = async () => {
        if (!monthLookup) return
        setLookupError(null)
        try {
            const found = await profitApi.getByMonth(monthLookup)
            navigate(ROUTE_HELPERS.PROFIT_VIEW(found.id))
        } catch (err) {
            setLookupError(err instanceof Error ? err.message : t('lookup.error'))
        }
    }

    const columns: TableColumn<MonthlyProfitDistribution>[] = [
        { key: 'monthYear', label: t('columns.monthYear') },
        {
            key: 'totalProfit',
            label: t('columns.totalProfit'),
            render: (row) => formatCurrency(row.totalProfit),
        },
        {
            key: 'distributableProfit',
            label: t('columns.distributableProfit'),
            render: (row) => formatCurrency(row.distributableProfit),
        },
        {
            key: 'status',
            label: t('columns.status'),
            render: (row) => (
                <span className={`profit-list__badge profit-list__badge--${row.status}`}>
                    {t(`status.${row.status}`)}
                </span>
            ),
        },
        {
            key: 'updatedAt',
            label: t('columns.updatedAt'),
            render: (row) => formatDateTime(row.updatedAt),
        },
        {
            key: 'actions',
            label: t('columns.actions'),
            render: (row) => {
                const canCalculate = actionEnabled('calculate', row.status)
                const canDistribute = actionEnabled('distribute', row.status)
                const canLock = actionEnabled('lock', row.status)
                return (
                    <div className="profit-list__actions">
                        <Button size="sm" variant="secondary" onClick={() => navigate(ROUTE_HELPERS.PROFIT_VIEW(row.id))}>{tc('view')}</Button>
                        <Button size="sm" variant="secondary" onClick={() => navigate(ROUTE_HELPERS.PROFIT_EDIT(row.id))}>{tc('edit')}</Button>
                        <Button size="sm" variant="secondary" title={actionBlockedReason('calculate', row.status)} disabled={!canCalculate} onClick={() => openActionModal('calculate', row)}>{t('actions.calculate.label')}</Button>
                        <Button size="sm" variant="secondary" title={actionBlockedReason('distribute', row.status)} disabled={!canDistribute} onClick={() => openActionModal('distribute', row)}>{t('actions.distribute.label')}</Button>
                        <Button size="sm" variant="secondary" title={actionBlockedReason('lock', row.status)} disabled={!canLock} onClick={() => openActionModal('lock', row)}>{t('actions.lock.label')}</Button>
                    </div>
                )
            },
        },
    ]

    return (
        <div>
            <div className="profit-list__header">
                <h1 className="profit-list__title">{t('title')}</h1>
                <Button onClick={() => navigate(APP_ROUTES.PROFITS.CREATE)}>{t('createNew')}</Button>
            </div>

            <div className="profit-list__status-tabs">
                {STATUS_TABS.map((tab) => (
                    <button
                        key={tab}
                        type="button"
                        className={`profit-list__tab ${activeTab === tab ? 'profit-list__tab--active' : ''}`}
                        onClick={() => setActiveTab(tab)}
                    >
                        {t(`statusFilter.${tab}`)}
                    </button>
                ))}
            </div>

            <div className="profit-list__filters">
                <Input name="startMonth" type="month" label={t('filters.startMonth')} value={startMonth} onChange={(e) => setStartMonth(e.target.value)} />
                <Input name="endMonth" type="month" label={t('filters.endMonth')} value={endMonth} onChange={(e) => setEndMonth(e.target.value)} />
                <Input name="monthLookup" type="month" label={t('lookup.label')} value={monthLookup} onChange={(e) => setMonthLookup(e.target.value)} />
                <Button variant="secondary" onClick={handleLookupMonth}>{t('lookup.action')}</Button>
            </div>
            {lookupError && <p className="profit-list__error-text">{lookupError}</p>}

            {error && distributions.length === 0 ? (
                <div className="profit-list__error">
                    <p className="profit-list__error-text">{error}</p>
                    <Button onClick={refetch}>{t('errorRetry')}</Button>
                </div>
            ) : (
                <Card>
                    <Table<MonthlyProfitDistribution>
                        columns={columns}
                        data={distributions}
                        loading={loading || actions.loading}
                        emptyMessage={t('empty')}
                    />
                </Card>
            )}

            <Modal
                isOpen={actionModalOpen}
                onClose={closeActionModal}
                title={selectedAction ? t('actions.modal.title', { action: t(`actions.${selectedAction}.label`) }) : t('actions.modal.titleFallback')}
                footer={
                    <>
                        <Button variant="secondary" onClick={closeActionModal}>{tc('cancel')}</Button>
                        <Button onClick={confirmAction} loading={actions.loading}>{tc('confirm')}</Button>
                    </>
                }
            >
                {actionError && <p className="profit-list__error-text">{actionError}</p>}
                {selectedDistribution && selectedAction && (
                    <div className="profit-list__modal-content">
                        <p>{t('actions.modal.confirmMessage', { action: t(`actions.${selectedAction}.label`) })}</p>
                        <p><strong>{t('columns.monthYear')}:</strong> {selectedDistribution.monthYear}</p>
                        <p><strong>{t('columns.status')}:</strong> {t(`status.${selectedDistribution.status}`)}</p>
                    </div>
                )}
            </Modal>
        </div>
    )
}


