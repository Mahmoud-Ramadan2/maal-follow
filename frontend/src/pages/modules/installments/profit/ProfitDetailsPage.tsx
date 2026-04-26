import type { ReactNode } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'

import {
    useProfitDistribution,
    useProfitDistributionActions,
} from '@hooks/modules'
import Button from '@components/common/Button'
import Card from '@components/ui/Card'
import LoadingSpinner from '@components/ui/LoadingSpinner'
import { APP_ROUTES, ROUTE_HELPERS } from '@/router/routes.config'
import { formatCurrency } from '@utils/helpers/format.helper'
import { formatDateTime } from '@utils/helpers/ index'
import { ProfitDistributionStatus } from '@/types/modules/profit.types'
import './ProfitDetailsPage.css'

export default function ProfitDetailsPage(): ReactNode {
    const { id } = useParams<{ id: string }>()
    const distributionId = Number(id)
    const navigate = useNavigate()
    const { t } = useTranslation('profit')
    const { t: tc } = useTranslation('common')

    const { distribution, lifecycleStatus, loading, error, refetch } = useProfitDistribution(distributionId)
    const actions = useProfitDistributionActions()

    const canCalculate = distribution?.status === ProfitDistributionStatus.PENDING
    const canDistribute = distribution?.status === ProfitDistributionStatus.CALCULATED
    const canLock = distribution != null && distribution.status !== ProfitDistributionStatus.LOCKED

    const runAction = async (action: 'calculate' | 'distribute' | 'lock') => {
        if (!distribution) return
        const result = await actions[action](distribution.id)
        if (result) refetch()
    }



    if (loading) {
        return <div className="profit-details__center"><LoadingSpinner size="lg" /></div>
    }

    if (error || !distribution) {
        return (
            <div className="profit-details__center">
                <p className="profit-details__error-text">{error || t('details.notFound')}</p>
                <Button onClick={() => navigate(APP_ROUTES.PROFITS.LIST)}>{t('details.backToList')}</Button>
            </div>
        )
    }

    return (
        <div>
            <div className="profit-details__header">
                <h1 className="profit-details__title">{t('details.title', { month: distribution.monthYear })}</h1>
                <div className="profit-details__actions">
                    <Button variant="secondary" onClick={() => navigate(APP_ROUTES.PROFITS.LIST)}>{t('details.backToList')}</Button>
                    <Button variant="secondary" onClick={() => navigate(ROUTE_HELPERS.PROFIT_EDIT(distribution.id))}>{tc('edit')}</Button>
                </div>
            </div>

            <Card title={t('details.lifecycle')}>
                <div className="profit-details__grid">
                    <p><strong>{t('columns.status')}:</strong> {t(`status.${(lifecycleStatus?.status ?? distribution.status)}`)}</p>
                    <p><strong>{t('details.createdAt')}:</strong> {formatDateTime(distribution.createdAt)}</p>
                    <p><strong>{t('details.updatedAt')}:</strong> {formatDateTime(distribution.updatedAt)}</p>
                </div>
                <div className="profit-details__workflow-actions">
                    <Button size="sm" disabled={!canCalculate} onClick={() => runAction('calculate')}>{t('actions.calculate.label')}</Button>
                    <Button size="sm" variant="secondary" disabled={!canDistribute} onClick={() => runAction('distribute')}>{t('actions.distribute.label')}</Button>
                    <Button size="sm" variant="secondary" disabled={!canLock} onClick={() => runAction('lock')}>{t('actions.lock.label')}</Button>
                </div>
            </Card>

            <Card title={t('details.financialBreakdown')}>
                <div className="profit-details__grid">
                    <p><strong>{t('columns.totalProfit')}:</strong> {formatCurrency(distribution.totalProfit)}</p>
                    <p><strong>{t('columns.managementFeeAmount')}:</strong> {formatCurrency(distribution.managementFeeAmount)}</p>
                    <p><strong>{t('columns.zakatAmount')}:</strong> {formatCurrency(distribution.zakatAmount)}</p>
                    <p><strong>{t('columns.contractExpensesAmount')}:</strong> {formatCurrency(distribution.contractExpensesAmount)}</p>
                    <p><strong>{t('columns.distributableProfit')}:</strong> {formatCurrency(distribution.distributableProfit)}</p>
                    <p><strong>{t('columns.ownerProfit')}:</strong> {formatCurrency(distribution.ownerProfit)}</p>
                    <p><strong>{t('columns.partnersTotalProfit')}:</strong> {formatCurrency(distribution.partnersTotalProfit)}</p>
                </div>
            </Card>

            <Card title={t('details.notes')}>
                <p>{distribution.calculationNotes || '—'}</p>
            </Card>
        </div>
    )
}

