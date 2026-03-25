import { useState, useMemo } from 'react'
import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'

import { usePartners, usePartnerDelete } from '@hooks/modules'
import Table from '@components/common/Table'
import type { TableColumn } from '@components/common/Table'
import Button from '@components/common/Button'
import Input from '@components/common/Input'
import Card from '@components/ui/Card'
import { APP_ROUTES, ROUTE_HELPERS } from '@/router/routes.config'
import { PartnerStatus } from '@/types/modules/partner.types'
import type { Partner } from '@/types/modules/partner.types'
import { formatCurrency } from '@utils/helpers/format.helper'
import { formatDate } from '@utils/helpers/ index'
import './PartnerListPage.css'

type StatusFilter = PartnerStatus | 'ALL'
const STATUS_TABS: StatusFilter[] = ['ALL', PartnerStatus.ACTIVE, PartnerStatus.INACTIVE]

export default function PartnerListPage(): ReactNode {
    const { t } = useTranslation('partner')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()

    const [activeTab, setActiveTab] = useState<StatusFilter>('ALL')
    const [searchTerm, setSearchTerm] = useState('')

    const statusParam = activeTab === 'ALL' ? undefined : activeTab
    const { partners: rawPartners, loading, error, refetch } = usePartners(statusParam)
    const { deletePartner } = usePartnerDelete()

    const partners = useMemo(() => {
        if (!searchTerm.trim()) return rawPartners
        const q = searchTerm.toLowerCase()
        return rawPartners.filter(p =>
            p.name?.toLowerCase().includes(q) ||
            p.phone?.toLowerCase().includes(q),
        )
    }, [rawPartners, searchTerm])

    const handleDelete = async (id: number) => {
        const ok = await deletePartner(id)
        if (ok) refetch()
    }

    const handleClear = () => { setSearchTerm(''); setActiveTab('ALL') }

    const columns: TableColumn<Partner>[] = [
        { key: 'name', label: t('columns.name') },
        { key: 'phone', label: t('columns.phone') },
        {
            key: 'partnershipType', label: t('columns.partnershipType'),
            render: (row) => t(`partnershipType.${row.partnershipType}`),
        },
        {
            key: 'sharePercentage', label: t('columns.sharePercentage'),
            render: (row) => row.sharePercentage != null ? `${row.sharePercentage}%` : '—',
        },
        {
            key: 'currentBalance', label: t('columns.currentBalance'),
            render: (row) => formatCurrency(row.currentBalance),
        },
        {
            key: 'totalInvestment', label: t('columns.totalInvestment'),
            render: (row) => formatCurrency(row.totalInvestment),
        },
        {
            key: 'investmentStartDate', label: t('columns.investmentStartDate'),
            render: (row) => formatDate(row.investmentStartDate),
        },
        {
            key: 'status', label: t('columns.status'),
            render: (row) => (
                <span className={`partner-list__badge partner-list__badge--${row.status}`}>
                    <span className="partner-list__badge-dot" />
                    {t(`status.${row.status}`)}
                </span>
            ),
        },
        {
            key: 'actions', label: t('columns.actions'),
            render: (row) => (
                <div className="partner-list__actions">
                    <Button size="sm" variant="secondary" onClick={() => navigate(ROUTE_HELPERS.PARTNER_VIEW(row.id))}>
                        {tc('view')}
                    </Button>
                    <Button size="sm" variant="secondary" onClick={() => navigate(ROUTE_HELPERS.PARTNER_EDIT(row.id))}>
                        {tc('edit')}
                    </Button>
                    <Button size="sm" variant="danger" onClick={() => handleDelete(row.id)}>
                        {tc('delete')}
                    </Button>
                </div>
            ),
        },
    ]

    if (error && partners.length === 0) {
        return (
            <div className="partner-list__error">
                <p className="partner-list__error-text">{error}</p>
                <Button onClick={refetch}>{t('errorRetry')}</Button>
            </div>
        )
    }

    return (
        <div>
            <div className="partner-list__header">
                <h1 className="partner-list__title">{t('title')}</h1>
                <Button onClick={() => navigate(APP_ROUTES.PARTNERS.CREATE)}>{t('createNew')}</Button>
            </div>

            <div className="partner-list__status-tabs">
                {STATUS_TABS.map((tab) => (
                    <button
                        key={tab}
                        type="button"
                        className={`partner-list__tab ${activeTab === tab ? 'partner-list__tab--active' : ''}`}
                        onClick={() => setActiveTab(tab)}
                    >
                        {t(`statusFilter.${tab}`)}
                    </button>
                ))}
            </div>

            <div className="partner-list__filters">
                <div className="partner-list__search">
                    <Input name="search" placeholder={t('searchPlaceholder')} value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
                </div>
                {(searchTerm || activeTab !== 'ALL') && (
                    <Button variant="secondary" size="sm" onClick={handleClear}>{t('clearFilters')}</Button>
                )}
            </div>

            <Card>
                <Table<Partner>
                    columns={columns}
                    data={partners}
                    loading={loading}
                    emptyMessage={t('empty')}
                />
            </Card>
        </div>
    )
}
