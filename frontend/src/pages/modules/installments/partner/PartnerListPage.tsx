import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'

import { usePartners, usePartnerDelete } from '@hooks/modules'
import Table from '@components/common/Table'
import type { TableColumn } from '@components/common/Table'
import Button from '@components/common/Button'
import Input from '@components/common/Input'
import Card from '@components/ui/Card'
import Modal from '@components/common/Modal/Modal'
import { APP_ROUTES, ROUTE_HELPERS } from '@/router/routes.config'
import {
    CommissionType,
    InvestmentStatus,
    PartnerStatus,
    WithdrawalType,
} from '@/types/modules/partner.types'
import type {
    Partner,
    PartnerCommission,
    PartnerCommissionRequest,
    PartnerCommissionSummary,
    PartnerCustomerAcquisition,
    PartnerCustomerAcquisitionRequest,
    PartnerInvestment,
    PartnerInvestmentRequest,
    PartnerMonthlyProfit,
    PartnerMonthlyProfitAdjustRequest,
    PartnerMonthlyProfitPayRequest,
    PartnerProfitConfig,
    PartnerProfitConfigRequest,
    PartnerWithdrawal,
    PartnerWithdrawalRequest,
    PayoutReconciliation,
} from '@/types/modules/partner.types'
import { PaymentMethod } from '@/types/modules/payment.types'
import type { Customer } from '@/types/modules/customer.types'
import { formatCurrency } from '@utils/helpers/format.helper'
import { formatDate } from '@utils/helpers/ index'
import { partnerInvestmentApi } from '@services/api/modules/partnerInvestment.api'
import { partnerWithdrawalApi } from '@services/api/modules/partnerWithdrawal.api'
import { partnerCommissionApi } from '@services/api/modules/partnerCommission.api'
import { partnerCustomerAcquisitionApi } from '@services/api/modules/partnerCustomerAcquisition.api'
import * as partnerMonthlyProfitModule from '@services/api/modules/partnerMonthlyProfit.api'
import { partnerProfitControlApi } from '@services/api/modules/partnerProfitControl.api'
import { customerApi } from '@services/api/modules/customer.api'
import './PartnerListPage.css'

type StatusFilter = PartnerStatus | 'ALL'
const STATUS_TABS: StatusFilter[] = ['ALL', PartnerStatus.ACTIVE, PartnerStatus.INACTIVE]

type DepartmentKey =
    | 'OVERVIEW'
    | 'INVESTMENTS'
    | 'WITHDRAWALS'
    | 'COMMISSIONS'
    | 'ACQUISITIONS'
    | 'MONTHLY_PROFITS'
    | 'PROFIT_CONTROL'

const DEPARTMENT_TABS: DepartmentKey[] = [
    'OVERVIEW',
    'INVESTMENTS',
    'WITHDRAWALS',
    'COMMISSIONS',
    'ACQUISITIONS',
    'MONTHLY_PROFITS',
    'PROFIT_CONTROL',
]

type ActionModalKey =
    | null
    | 'CREATE_INVESTMENT'
    | 'CONFIRM_INVESTMENT'
    | 'CREATE_WITHDRAWAL'
    | 'APPROVE_WITHDRAWAL'
    | 'PROCESS_WITHDRAWAL'
    | 'REJECT_WITHDRAWAL'
    | 'CREATE_COMMISSION'
    | 'APPROVE_COMMISSION'
    | 'PAY_COMMISSION'
    | 'CANCEL_COMMISSION'
    | 'BULK_APPROVE_COMMISSIONS'
    | 'ASSIGN_ACQUISITION'
    | 'TRANSFER_ACQUISITION'
    | 'UPDATE_ACQ_COMMISSION'
    | 'PAY_MONTHLY_PROFIT'
    | 'ADJUST_MONTHLY_PROFIT'
    | 'UPDATE_PROFIT_CONFIG'
    | 'START_PROFIT_SHARING'
    | 'PAUSE_PROFIT_SHARING'
    | 'RESUME_PROFIT_SHARING'
    | 'VIEW_INVESTMENT_DETAIL'
    | 'VIEW_WITHDRAWAL_DETAIL'
    | 'VIEW_COMMISSION_RECONCILIATION'
    | 'VIEW_MONTHLY_PROFIT_RECONCILIATION'

interface ActionFormState {
    amount: string
    status: string
    notes: string
    reason: string
    approvedByUserId: string
    customerId: string
    commissionPercentage: string
    targetPartnerId: string
    paymentMethod: string
    paymentDate: string
    managementFeePercentage: string
    zakatPercentage: string
    profitPaymentDay: string
    startDate: string
    contractId: string
    purchaseId: string
}

const defaultFormState = (today: string): ActionFormState => ({
    amount: '',
    status: '',
    notes: '',
    reason: '',
    approvedByUserId: '1',
    customerId: '',
    commissionPercentage: '5',
    targetPartnerId: '',
    paymentMethod: PaymentMethod.CASH,
    paymentDate: today,
    managementFeePercentage: '',
    zakatPercentage: '',
    profitPaymentDay: '',
    startDate: today,
    contractId: '',
    purchaseId: '',
})

const toNumber = (value: string): number => Number(value || 0)
const toOptionalNumber = (value: string): number | undefined => {
    const trimmed = value.trim()
    if (!trimmed) return undefined
    const parsed = Number(trimmed)
    return Number.isNaN(parsed) ? undefined : parsed
}

const partnerMonthlyProfitApi = partnerMonthlyProfitModule.partnerMonthlyProfitApi

const canApproveWithdrawal = (status: PartnerWithdrawal['status']): boolean => status === 'PENDING'
const canProcessWithdrawal = (status: PartnerWithdrawal['status']): boolean => status === 'APPROVED'
const canRejectWithdrawal = (status: PartnerWithdrawal['status']): boolean => status === 'PENDING'

const canApproveCommission = (status: PartnerCommission['status']): boolean => status === 'PENDING'
const canPayCommission = (status: PartnerCommission['status']): boolean => status === 'PENDING'
const canCancelCommission = (status: PartnerCommission['status']): boolean => status === 'PENDING'

const canPayMonthlyProfit = (status: PartnerMonthlyProfit['status']): boolean => status === 'CALCULATED' || status === 'DEFERRED'
const canAdjustMonthlyProfit = (status: PartnerMonthlyProfit['status']): boolean => status !== 'PAID'

const canStartProfitSharing = (isActive?: boolean): boolean => !isActive
const canPauseProfitSharing = (isActive?: boolean): boolean => !!isActive
const canResumeProfitSharing = (isActive?: boolean): boolean => !isActive

export default function PartnerListPage(): ReactNode {
    const { t } = useTranslation('partner')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()

    const [activeTab, setActiveTab] = useState<StatusFilter>('ALL')
    const [searchTerm, setSearchTerm] = useState('')
    const [department, setDepartment] = useState<DepartmentKey>('OVERVIEW')
    const [selectedPartnerId, setSelectedPartnerId] = useState<number | null>(null)
    const [departmentLoading, setDepartmentLoading] = useState(false)
    const [departmentError, setDepartmentError] = useState<string | null>(null)
    const [departmentVersion, setDepartmentVersion] = useState(0)

    const [investments, setInvestments] = useState<PartnerInvestment[]>([])
    const [withdrawals, setWithdrawals] = useState<PartnerWithdrawal[]>([])
    const [commissions, setCommissions] = useState<PartnerCommission[]>([])
    const [acquisitions, setAcquisitions] = useState<PartnerCustomerAcquisition[]>([])
    const [monthlyProfits, setMonthlyProfits] = useState<PartnerMonthlyProfit[]>([])
    const [profitConfig, setProfitConfig] = useState<PartnerProfitConfig | null>(null)
    const [isEligible, setIsEligible] = useState<boolean | null>(null)
    const [commissionSummary, setCommissionSummary] = useState<PartnerCommissionSummary | null>(null)

    const [customers, setCustomers] = useState<Customer[]>([])
    const [actionModal, setActionModal] = useState<ActionModalKey>(null)
    const [actionTargetId, setActionTargetId] = useState<number | null>(null)
    const [actionLoading, setActionLoading] = useState(false)
    const [actionError, setActionError] = useState<string | null>(null)
    const [selectedInvestmentDetail, setSelectedInvestmentDetail] = useState<PartnerInvestment | null>(null)
    const [selectedWithdrawalDetail, setSelectedWithdrawalDetail] = useState<PartnerWithdrawal | null>(null)
    const [selectedReconciliation, setSelectedReconciliation] = useState<PayoutReconciliation | null>(null)

    const today = useMemo(() => new Date().toISOString().slice(0, 10), [])
    const currentMonth = useMemo(() => today.slice(0, 7), [today])
    const [formState, setFormState] = useState<ActionFormState>(() => defaultFormState(today))

    const statusParam = activeTab === 'ALL' ? undefined : activeTab
    const { partners: rawPartners, loading, error, refetch } = usePartners(statusParam)
    const { deletePartner } = usePartnerDelete()

    const partners = useMemo(() => {
        if (!searchTerm.trim()) return rawPartners
        const q = searchTerm.toLowerCase()
        return rawPartners.filter((p) => p.name?.toLowerCase().includes(q)
            || p.phone?.toLowerCase().includes(q)
            || p.nationalId?.toLowerCase().includes(q))
    }, [rawPartners, searchTerm])

    const selectedPartner = useMemo(
        () => partners.find((p) => p.id === selectedPartnerId) ?? null,
        [partners, selectedPartnerId],
    )

    const resetActionForm = () => {
        setFormState(defaultFormState(today))
        setActionError(null)
    }

    const openActionModal = (modalKey: ActionModalKey, targetId?: number) => {
        setActionModal(modalKey)
        setActionTargetId(targetId ?? null)
        setSelectedInvestmentDetail(null)
        setSelectedWithdrawalDetail(null)
        setSelectedReconciliation(null)
        resetActionForm()
        if (modalKey === 'CREATE_WITHDRAWAL') {
            setFormState((s) => ({ ...s, status: WithdrawalType.FROM_PRINCIPAL }))
        }
    }

    const closeActionModal = () => {
        setActionModal(null)
        setActionTargetId(null)
        setActionError(null)
        setSelectedInvestmentDetail(null)
        setSelectedWithdrawalDetail(null)
        setSelectedReconciliation(null)
    }

    const openInvestmentDetail = async (id: number) => {
        openActionModal('VIEW_INVESTMENT_DETAIL', id)
        setActionLoading(true)
        setActionError(null)
        try {
            setSelectedInvestmentDetail(await partnerInvestmentApi.getById(id))
        } catch (err) {
            setActionError(err instanceof Error ? err.message : t('department.actions.error'))
        } finally {
            setActionLoading(false)
        }
    }

    const openWithdrawalDetail = async (id: number) => {
        openActionModal('VIEW_WITHDRAWAL_DETAIL', id)
        setActionLoading(true)
        setActionError(null)
        try {
            setSelectedWithdrawalDetail(await partnerWithdrawalApi.getById(id))
        } catch (err) {
            setActionError(err instanceof Error ? err.message : t('department.actions.error'))
        } finally {
            setActionLoading(false)
        }
    }

    const openCommissionReconciliation = async (id: number) => {
        openActionModal('VIEW_COMMISSION_RECONCILIATION', id)
        setActionLoading(true)
        setActionError(null)
        try {
            setSelectedReconciliation(await partnerCommissionApi.getReconciliation(id))
        } catch (err) {
            setActionError(err instanceof Error ? err.message : t('department.actions.error'))
        } finally {
            setActionLoading(false)
        }
    }

    const openMonthlyProfitReconciliation = async (id: number) => {
        openActionModal('VIEW_MONTHLY_PROFIT_RECONCILIATION', id)
        setActionLoading(true)
        setActionError(null)
        try {
            setSelectedReconciliation(await partnerMonthlyProfitApi.getReconciliation(id))
        } catch (err) {
            setActionError(err instanceof Error ? err.message : t('department.actions.error'))
        } finally {
            setActionLoading(false)
        }
    }

    const refreshDepartment = () => setDepartmentVersion((v) => v + 1)

    useEffect(() => {
        if (partners.length === 0) {
            setSelectedPartnerId(null)
            return
        }
        const exists = selectedPartnerId != null && partners.some((p) => p.id === selectedPartnerId)
        if (!exists) setSelectedPartnerId(partners[0].id)
    }, [partners, selectedPartnerId])

    useEffect(() => {
        const needsCustomerLookup = actionModal === 'ASSIGN_ACQUISITION'
        if (!needsCustomerLookup) return
        let mounted = true

        customerApi.getAll({ page: 0, size: 100, status: 'active' })
            .then((res) => {
                if (mounted) setCustomers(res.content)
            })
            .catch(() => {
                if (mounted) setCustomers([])
            })

        return () => {
            mounted = false
        }
    }, [actionModal])

    useEffect(() => {
        const partnerId = selectedPartnerId
        if (!partnerId) return

        setDepartmentLoading(true)
        setDepartmentError(null)

        const load = async () => {
            try {
                if (department === 'OVERVIEW') {
                    const [inv, wdl, comm, acq, profit, cfg, eligible, summary] = await Promise.all([
                        partnerInvestmentApi.getByPartner(partnerId),
                        partnerWithdrawalApi.getByPartner(partnerId),
                        partnerCommissionApi.getByPartner(partnerId, 0, 5).then((r) => r.content),
                        partnerCustomerAcquisitionApi.getByPartner(partnerId),
                        partnerMonthlyProfitApi.getByPartner(partnerId),
                        partnerProfitControlApi.getCurrentConfig(),
                        partnerProfitControlApi.isEligible(partnerId, currentMonth),
                        partnerCommissionApi.getSummary(partnerId),
                    ])
                    setInvestments(inv)
                    setWithdrawals(wdl)
                    setCommissions(comm)
                    setAcquisitions(acq)
                    setMonthlyProfits(profit)
                    setProfitConfig(cfg)
                    setIsEligible(eligible)
                    setCommissionSummary(summary)
                    return
                }

                if (department === 'INVESTMENTS') {
                    setInvestments(await partnerInvestmentApi.getByPartner(partnerId))
                    return
                }
                if (department === 'WITHDRAWALS') {
                    setWithdrawals(await partnerWithdrawalApi.getByPartner(partnerId))
                    return
                }
                if (department === 'COMMISSIONS') {
                    const [list, summary] = await Promise.all([
                        partnerCommissionApi.getByPartner(partnerId, 0, 25).then((r) => r.content),
                        partnerCommissionApi.getSummary(partnerId),
                    ])
                    setCommissions(list)
                    setCommissionSummary(summary)
                    return
                }
                if (department === 'ACQUISITIONS') {
                    setAcquisitions(await partnerCustomerAcquisitionApi.getByPartner(partnerId))
                    return
                }
                if (department === 'MONTHLY_PROFITS') {
                    setMonthlyProfits(await partnerMonthlyProfitApi.getByPartner(partnerId))
                    return
                }
                if (department === 'PROFIT_CONTROL') {
                    const [cfg, eligible] = await Promise.all([
                        partnerProfitControlApi.getCurrentConfig(),
                        partnerProfitControlApi.isEligible(partnerId, currentMonth),
                    ])
                    setProfitConfig(cfg)
                    setIsEligible(eligible)
                }
            } catch (err) {
                setDepartmentError(err instanceof Error ? err.message : t('department.fetchError'))
            } finally {
                setDepartmentLoading(false)
            }
        }

        void load()
    }, [selectedPartnerId, department, currentMonth, t, departmentVersion])

    const handleDelete = useCallback(async (id: number) => {
        const ok = await deletePartner(id)
        if (ok) refetch()
    }, [deletePartner, refetch])

    const handleClear = () => {
        setSearchTerm('')
        setActiveTab('ALL')
    }

    const handleActionSubmit = async () => {
        const partnerId = selectedPartnerId
        if (!partnerId || !actionModal) return

        setActionLoading(true)
        setActionError(null)
        try {
            if (actionModal === 'CREATE_INVESTMENT') {
                const payload: PartnerInvestmentRequest = {
                    partnerId,
                    amount: toNumber(formState.amount),
                    status: (formState.status || undefined) as PartnerInvestmentRequest['status'],
                    notes: formState.notes || undefined,
                }
                await partnerInvestmentApi.create(payload)
                toast.success(t('department.actions.success'))
            }

            if (actionModal === 'CONFIRM_INVESTMENT' && actionTargetId) {
                await partnerInvestmentApi.confirm(actionTargetId)
                toast.success(t('department.actions.success'))
            }

            if (actionModal === 'CREATE_WITHDRAWAL') {
                const payload: PartnerWithdrawalRequest = {
                    partnerId,
                    amount: toNumber(formState.amount),
                    withdrawalType: (formState.status || WithdrawalType.FROM_PRINCIPAL) as PartnerWithdrawalRequest['withdrawalType'],
                    requestReason: formState.reason || undefined,
                    notes: formState.notes || undefined,
                }
                await partnerWithdrawalApi.create(payload)
                toast.success(t('department.actions.success'))
            }

            if (actionModal === 'APPROVE_WITHDRAWAL' && actionTargetId) {
                const target = withdrawals.find((w) => w.id === actionTargetId)
                if (!target || !canApproveWithdrawal(target.status)) {
                    setActionError(t('department.actions.notAllowed'))
                    return
                }
                await partnerWithdrawalApi.approve(actionTargetId)
                toast.success(t('department.actions.success'))
            }

            if (actionModal === 'PROCESS_WITHDRAWAL' && actionTargetId) {
                const target = withdrawals.find((w) => w.id === actionTargetId)
                if (!target || !canProcessWithdrawal(target.status)) {
                    setActionError(t('department.actions.notAllowed'))
                    return
                }
                await partnerWithdrawalApi.process(actionTargetId)
                toast.success(t('department.actions.success'))
            }

            if (actionModal === 'REJECT_WITHDRAWAL' && actionTargetId) {
                const target = withdrawals.find((w) => w.id === actionTargetId)
                if (!target || !canRejectWithdrawal(target.status)) {
                    setActionError(t('department.actions.notAllowed'))
                    return
                }
                await partnerWithdrawalApi.reject(actionTargetId, formState.reason || undefined)
                toast.success(t('department.actions.success'))
            }

            if (actionModal === 'CREATE_COMMISSION') {
                const payload: PartnerCommissionRequest = {
                    partnerId,
                    amount: toNumber(formState.amount),
                    commissionType: (formState.status || CommissionType.SALES_COMMISSION) as PartnerCommissionRequest['commissionType'],
                    notes: formState.notes || undefined,
                    contractId: toOptionalNumber(formState.contractId),
                    purchaseId: toOptionalNumber(formState.purchaseId),
                }
                await partnerCommissionApi.create(payload)
                toast.success(t('department.actions.success'))
            }

            if (actionModal === 'APPROVE_COMMISSION' && actionTargetId) {
                const target = commissions.find((c) => c.id === actionTargetId)
                if (!target || !canApproveCommission(target.status)) {
                    setActionError(t('department.actions.notAllowed'))
                    return
                }
                await partnerCommissionApi.approve(actionTargetId, toNumber(formState.approvedByUserId) || 1)
                toast.success(t('department.actions.success'))
            }

            if (actionModal === 'PAY_COMMISSION' && actionTargetId) {
                const target = commissions.find((c) => c.id === actionTargetId)
                if (!target || !canPayCommission(target.status)) {
                    setActionError(t('department.actions.notAllowed'))
                    return
                }
                await partnerCommissionApi.pay(actionTargetId)
                toast.success(t('department.actions.success'))
            }

            if (actionModal === 'CANCEL_COMMISSION' && actionTargetId) {
                const target = commissions.find((c) => c.id === actionTargetId)
                if (!target || !canCancelCommission(target.status)) {
                    setActionError(t('department.actions.notAllowed'))
                    return
                }
                await partnerCommissionApi.cancel(actionTargetId, formState.reason || undefined)
                toast.success(t('department.actions.success'))
            }

            if (actionModal === 'BULK_APPROVE_COMMISSIONS') {
                await partnerCommissionApi.bulkApprove(partnerId, toNumber(formState.approvedByUserId) || 1)
                toast.success(t('department.actions.success'))
            }

            if (actionModal === 'ASSIGN_ACQUISITION') {
                const payload: PartnerCustomerAcquisitionRequest = {
                    partnerId,
                    customerId: toNumber(formState.customerId),
                    commissionPercentage: toNumber(formState.commissionPercentage),
                    acquisitionNotes: formState.notes || undefined,
                }
                await partnerCustomerAcquisitionApi.assign(payload)
                toast.success(t('department.actions.success'))
            }

            if (actionModal === 'TRANSFER_ACQUISITION' && actionTargetId) {
                await partnerCustomerAcquisitionApi.transfer(
                    actionTargetId,
                    partnerId,
                    toNumber(formState.targetPartnerId),
                    formState.reason || undefined,
                )
                toast.success(t('department.actions.success'))
            }

            if (actionModal === 'UPDATE_ACQ_COMMISSION' && actionTargetId) {
                await partnerCustomerAcquisitionApi.updateCommission(partnerId, actionTargetId, toNumber(formState.amount))
                toast.success(t('department.actions.success'))
            }

            if (actionModal === 'PAY_MONTHLY_PROFIT' && actionTargetId) {
                const target = monthlyProfits.find((p) => p.id === actionTargetId)
                if (!target || !canPayMonthlyProfit(target.status)) {
                    setActionError(t('department.actions.notAllowed'))
                    return
                }
                const payload: PartnerMonthlyProfitPayRequest = {
                    paidByUserId: toNumber(formState.approvedByUserId) || 1,
                    paymentMethod: (formState.paymentMethod || PaymentMethod.CASH) as PartnerMonthlyProfitPayRequest['paymentMethod'],
                    paymentDate: formState.paymentDate || undefined,
                    notes: formState.notes || undefined,
                }
                await partnerMonthlyProfitApi.pay(actionTargetId, payload)
                toast.success(t('department.actions.success'))
            }

            if (actionModal === 'ADJUST_MONTHLY_PROFIT' && actionTargetId) {
                const target = monthlyProfits.find((p) => p.id === actionTargetId)
                if (!target || !canAdjustMonthlyProfit(target.status)) {
                    setActionError(t('department.actions.notAllowed'))
                    return
                }
                const payload: PartnerMonthlyProfitAdjustRequest = {
                    newAmount: toNumber(formState.amount),
                    reason: formState.reason || t('department.actions.defaultReason'),
                }
                await partnerMonthlyProfitApi.adjust(actionTargetId, payload)
                toast.success(t('department.actions.success'))
            }

            if (actionModal === 'UPDATE_PROFIT_CONFIG') {
                const payload: PartnerProfitConfigRequest = {
                    managementFeePercentage: toNumber(formState.managementFeePercentage),
                    zakatPercentage: toNumber(formState.zakatPercentage),
                    profitPaymentDay: toNumber(formState.profitPaymentDay),
                }
                await partnerProfitControlApi.updateConfig(payload)
                toast.success(t('department.actions.success'))
            }

            if (actionModal === 'START_PROFIT_SHARING') {
                if (!canStartProfitSharing(selectedPartner?.profitSharingActive)) {
                    setActionError(t('department.actions.notAllowed'))
                    return
                }
                await partnerProfitControlApi.startProfitSharing(partnerId, formState.startDate || today)
                toast.success(t('department.actions.success'))
            }

            if (actionModal === 'PAUSE_PROFIT_SHARING') {
                if (!canPauseProfitSharing(selectedPartner?.profitSharingActive)) {
                    setActionError(t('department.actions.notAllowed'))
                    return
                }
                await partnerProfitControlApi.pauseProfitSharing(partnerId, formState.reason || undefined)
                toast.success(t('department.actions.success'))
            }

            if (actionModal === 'RESUME_PROFIT_SHARING') {
                if (!canResumeProfitSharing(selectedPartner?.profitSharingActive)) {
                    setActionError(t('department.actions.notAllowed'))
                    return
                }
                await partnerProfitControlApi.resumeProfitSharing(partnerId)
                toast.success(t('department.actions.success'))
            }

            closeActionModal()
            refreshDepartment()
        } catch (err) {
            setActionError(err instanceof Error ? err.message : t('department.actions.error'))
        } finally {
            setActionLoading(false)
        }
    }

    const columns = useMemo<TableColumn<Partner>[]>(() => [
        { key: 'name', label: t('columns.name') },
        { key: 'phone', label: t('columns.phone') },
        { key: 'nationalId', label: t('columns.nationalId') },
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
            key: 'effectiveInvestment', label: t('columns.effectiveInvestment'),
            render: (row) => formatCurrency(row.effectiveInvestment),
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
    ], [handleDelete, navigate, t, tc])

    const actionModalTitle = actionModal ? t(`department.actions.modal.${actionModal}`) : ''
    const isReadOnlyModal = actionModal === 'VIEW_INVESTMENT_DETAIL'
        || actionModal === 'VIEW_WITHDRAWAL_DETAIL'
        || actionModal === 'VIEW_COMMISSION_RECONCILIATION'
        || actionModal === 'VIEW_MONTHLY_PROFIT_RECONCILIATION'

    const renderActionModalBody = () => {
        if (!actionModal) return null

        if (actionModal === 'CREATE_INVESTMENT') {
            return (
                <div className="partner-list__modal-grid">
                    <Input name="investmentAmount" label={t('department.actions.amount')} type="number" value={formState.amount} onChange={(e) => setFormState((s) => ({ ...s, amount: e.target.value }))} />
                    <div className="form-field">
                        <label className="form-field__label">{t('department.actions.status')}</label>
                        <select className="partner-list__department-select" value={formState.status} onChange={(e) => setFormState((s) => ({ ...s, status: e.target.value }))}>
                            <option value="">{t('department.actions.autoStatus')}</option>
                            {Object.values(InvestmentStatus).map((status) => <option key={status} value={status}>{status}</option>)}
                        </select>
                    </div>
                    <Input name="investmentNotes" label={t('department.actions.notes')} value={formState.notes} onChange={(e) => setFormState((s) => ({ ...s, notes: e.target.value }))} />
                </div>
            )
        }

        if (actionModal === 'CREATE_WITHDRAWAL') {
            return (
                <div className="partner-list__modal-grid">
                    <Input name="withdrawalAmount" label={t('department.actions.amount')} type="number" value={formState.amount} onChange={(e) => setFormState((s) => ({ ...s, amount: e.target.value }))} />
                    <div className="form-field">
                        <label className="form-field__label">{t('department.actions.type')}</label>
                        <select
                            className="partner-list__department-select"
                            value={formState.status || WithdrawalType.FROM_PRINCIPAL}
                            disabled
                        >
                            <option value={WithdrawalType.FROM_PRINCIPAL}>{t(`withdrawalType.${WithdrawalType.FROM_PRINCIPAL}`)}</option>
                        </select>
                    </div>
                    <Input name="withdrawalReason" label={t('department.actions.reason')} value={formState.reason} onChange={(e) => setFormState((s) => ({ ...s, reason: e.target.value }))} />
                    <Input name="withdrawalNotes" label={t('department.actions.notes')} value={formState.notes} onChange={(e) => setFormState((s) => ({ ...s, notes: e.target.value }))} />
                </div>
            )
        }

        if (actionModal === 'CREATE_COMMISSION') {
            return (
                <div className="partner-list__modal-grid">
                    <Input name="commissionAmount" label={t('department.actions.amount')} type="number" value={formState.amount} onChange={(e) => setFormState((s) => ({ ...s, amount: e.target.value }))} />
                    <div className="form-field">
                        <label className="form-field__label">{t('department.actions.type')}</label>
                        <select className="partner-list__department-select" value={formState.status} onChange={(e) => setFormState((s) => ({ ...s, status: e.target.value }))}>
                            {Object.values(CommissionType).map((status) => <option key={status} value={status}>{t(`commissionType.${status}`)}</option>)}
                        </select>
                    </div>
                    <Input name="commissionContractId" label={t('department.actions.contractId')} type="number" value={formState.contractId} onChange={(e) => setFormState((s) => ({ ...s, contractId: e.target.value }))} />
                    <Input name="commissionPurchaseId" label={t('department.actions.purchaseId')} type="number" value={formState.purchaseId} onChange={(e) => setFormState((s) => ({ ...s, purchaseId: e.target.value }))} />
                    <Input name="commissionNotes" label={t('department.actions.notes')} value={formState.notes} onChange={(e) => setFormState((s) => ({ ...s, notes: e.target.value }))} />
                </div>
            )
        }

        if (actionModal === 'ASSIGN_ACQUISITION') {
            return (
                <div className="partner-list__modal-grid">
                    <div className="form-field">
                        <label className="form-field__label">{t('department.actions.customer')}</label>
                        <select className="partner-list__department-select" value={formState.customerId} onChange={(e) => setFormState((s) => ({ ...s, customerId: e.target.value }))}>
                            <option value="">{t('department.actions.selectCustomer')}</option>
                            {customers.map((customer) => (
                                <option key={customer.id} value={customer.id}>{customer.name}</option>
                            ))}
                        </select>
                    </div>
                    <Input name="acqCommissionPercentage" label={t('department.actions.commissionPercentage')} type="number" value={formState.commissionPercentage} onChange={(e) => setFormState((s) => ({ ...s, commissionPercentage: e.target.value }))} />
                    <Input name="acqNotes" label={t('department.actions.notes')} value={formState.notes} onChange={(e) => setFormState((s) => ({ ...s, notes: e.target.value }))} />
                </div>
            )
        }

        if (actionModal === 'PAY_MONTHLY_PROFIT') {
            return (
                <div className="partner-list__modal-grid">
                    <Input name="payUserId" label={t('department.actions.userId')} type="number" value={formState.approvedByUserId} onChange={(e) => setFormState((s) => ({ ...s, approvedByUserId: e.target.value }))} />
                    <div className="form-field">
                        <label className="form-field__label">{t('department.actions.paymentMethod')}</label>
                        <select className="partner-list__department-select" value={formState.paymentMethod} onChange={(e) => setFormState((s) => ({ ...s, paymentMethod: e.target.value }))}>
                            {Object.values(PaymentMethod).map((method) => <option key={method} value={method}>{method}</option>)}
                        </select>
                    </div>
                    <Input name="payDate" label={t('department.actions.paymentDate')} type="date" value={formState.paymentDate} onChange={(e) => setFormState((s) => ({ ...s, paymentDate: e.target.value }))} />
                    <Input name="payNotes" label={t('department.actions.notes')} value={formState.notes} onChange={(e) => setFormState((s) => ({ ...s, notes: e.target.value }))} />
                </div>
            )
        }

        if (actionModal === 'ADJUST_MONTHLY_PROFIT') {
            return (
                <div className="partner-list__modal-grid">
                    <Input name="adjustAmount" label={t('department.actions.amount')} type="number" value={formState.amount} onChange={(e) => setFormState((s) => ({ ...s, amount: e.target.value }))} />
                    <Input name="adjustReason" label={t('department.actions.reason')} value={formState.reason} onChange={(e) => setFormState((s) => ({ ...s, reason: e.target.value }))} />
                </div>
            )
        }

        if (actionModal === 'UPDATE_PROFIT_CONFIG') {
            return (
                <div className="partner-list__modal-grid">
                    <Input name="managementFee" label={t('department.config.managementFee')} type="number" value={formState.managementFeePercentage} onChange={(e) => setFormState((s) => ({ ...s, managementFeePercentage: e.target.value }))} />
                    <Input name="zakat" label={t('department.config.zakat')} type="number" value={formState.zakatPercentage} onChange={(e) => setFormState((s) => ({ ...s, zakatPercentage: e.target.value }))} />
                    <Input name="paymentDay" label={t('department.config.paymentDay')} type="number" value={formState.profitPaymentDay} onChange={(e) => setFormState((s) => ({ ...s, profitPaymentDay: e.target.value }))} />
                </div>
            )
        }

        if (actionModal === 'START_PROFIT_SHARING') {
            return <Input name="startDate" label={t('department.actions.startDate')} type="date" value={formState.startDate} onChange={(e) => setFormState((s) => ({ ...s, startDate: e.target.value }))} />
        }

        if (actionModal === 'TRANSFER_ACQUISITION') {
            return (
                <div className="partner-list__modal-grid">
                    <div className="form-field">
                        <label className="form-field__label">{t('department.actions.targetPartner')}</label>
                        <select className="partner-list__department-select" value={formState.targetPartnerId} onChange={(e) => setFormState((s) => ({ ...s, targetPartnerId: e.target.value }))}>
                            <option value="">{t('department.actions.selectPartner')}</option>
                            {partners.filter((p) => p.id !== selectedPartnerId).map((partner) => (
                                <option key={partner.id} value={partner.id}>{partner.name}</option>
                            ))}
                        </select>
                    </div>
                    <Input name="transferReason" label={t('department.actions.reason')} value={formState.reason} onChange={(e) => setFormState((s) => ({ ...s, reason: e.target.value }))} />
                </div>
            )
        }

        if (actionModal === 'UPDATE_ACQ_COMMISSION') {
            return <Input name="acqCommissionAmount" label={t('department.actions.amount')} type="number" value={formState.amount} onChange={(e) => setFormState((s) => ({ ...s, amount: e.target.value }))} />
        }

        if (actionModal === 'APPROVE_COMMISSION' || actionModal === 'BULK_APPROVE_COMMISSIONS') {
            return <Input name="approvedByUserId" label={t('department.actions.userId')} type="number" value={formState.approvedByUserId} onChange={(e) => setFormState((s) => ({ ...s, approvedByUserId: e.target.value }))} />
        }

        if (actionModal === 'CANCEL_COMMISSION' || actionModal === 'REJECT_WITHDRAWAL' || actionModal === 'PAUSE_PROFIT_SHARING') {
            return <Input name="reason" label={t('department.actions.reason')} value={formState.reason} onChange={(e) => setFormState((s) => ({ ...s, reason: e.target.value }))} />
        }

        if (actionModal === 'VIEW_INVESTMENT_DETAIL') {
            return (
                <div className="partner-list__modal-grid">
                    {!selectedInvestmentDetail && !actionLoading && <p>{t('department.actions.noData')}</p>}
                    {selectedInvestmentDetail && (
                        <div className="partner-list__detail-grid">
                            <p><strong>{t('investment.amount')}:</strong> {formatCurrency(selectedInvestmentDetail.amount)}</p>
                            <p><strong>{t('investment.type')}:</strong> {selectedInvestmentDetail.investmentType}</p>
                            <p><strong>{t('investment.status')}:</strong> {t(`investmentStatus.${selectedInvestmentDetail.status}`)}</p>
                            <p><strong>{t('investment.investedAt')}:</strong> {selectedInvestmentDetail.investedAt}</p>
                            <p><strong>{t('investment.returnedAt')}:</strong> {selectedInvestmentDetail.returnedAt ?? '—'}</p>
                            <p><strong>{t('investment.notes')}:</strong> {selectedInvestmentDetail.notes ?? '—'}</p>
                        </div>
                    )}
                </div>
            )
        }

        if (actionModal === 'VIEW_WITHDRAWAL_DETAIL') {
            return (
                <div className="partner-list__modal-grid">
                    {!selectedWithdrawalDetail && !actionLoading && <p>{t('department.actions.noData')}</p>}
                    {selectedWithdrawalDetail && (
                        <div className="partner-list__detail-grid">
                            <p><strong>{t('withdrawal.amount')}:</strong> {formatCurrency(selectedWithdrawalDetail.amount)}</p>
                            <p><strong>{t('withdrawal.principalAmount')}:</strong> {formatCurrency(selectedWithdrawalDetail.principalAmount)}</p>
                            <p><strong>{t('withdrawal.profitAmount')}:</strong> {formatCurrency(selectedWithdrawalDetail.profitAmount)}</p>
                            <p><strong>{t('withdrawal.type')}:</strong> {t(`withdrawalType.${selectedWithdrawalDetail.withdrawalType}`)}</p>
                            <p><strong>{t('withdrawal.status')}:</strong> {t(`withdrawalStatus.${selectedWithdrawalDetail.status}`)}</p>
                            <p><strong>{t('withdrawal.reason')}:</strong> {selectedWithdrawalDetail.requestReason ?? '—'}</p>
                            <p><strong>{t('withdrawal.requestedAt')}:</strong> {selectedWithdrawalDetail.requestedAt}</p>

                            {selectedWithdrawalDetail.status === 'APPROVED' && selectedWithdrawalDetail.approvedAt && (
                                <>
                                    <p><strong>{t('withdrawal.approvedAt')}:</strong> {selectedWithdrawalDetail.approvedAt}</p>
                                    <p><strong>{t('withdrawal.approvedBy')}:</strong> {selectedWithdrawalDetail.approvedByName ?? t('department.actions.unavailable')}</p>
                                </>
                            )}

                            {selectedWithdrawalDetail.status === 'COMPLETED' && (
                                <>
                                    {selectedWithdrawalDetail.processedAt && (
                                        <p><strong>{t('withdrawal.processedAt')}:</strong> {selectedWithdrawalDetail.processedAt}</p>
                                    )}
                                    <p><strong>{t('withdrawal.processedBy')}:</strong> {selectedWithdrawalDetail.processedByName ?? t('department.actions.unavailable')}</p>
                                </>
                            )}

                            {selectedWithdrawalDetail.status === 'CANCELLED' && (
                                <>
                                    {selectedWithdrawalDetail.rejectedAt && (
                                        <p><strong>{t('department.actions.rejectedAt')}:</strong> {selectedWithdrawalDetail.rejectedAt}</p>
                                    )}
                                    <p><strong>{t('department.actions.rejectedBy')}:</strong> {selectedWithdrawalDetail.rejectedByName ?? t('department.actions.unavailable')}</p>
                                    <p><strong>{t('department.actions.rejectReason')}:</strong> {selectedWithdrawalDetail.rejectionReason ?? t('department.actions.unavailable')}</p>
                                </>
                            )}
                        </div>
                    )}
                </div>
            )
        }

        if (actionModal === 'VIEW_COMMISSION_RECONCILIATION' || actionModal === 'VIEW_MONTHLY_PROFIT_RECONCILIATION') {
            return (
                <div className="partner-list__modal-grid">
                    {!selectedReconciliation && !actionLoading && <p>{t('department.actions.noData')}</p>}
                    {selectedReconciliation && (
                        <pre className="partner-list__recon-json">{JSON.stringify(selectedReconciliation, null, 2)}</pre>
                    )}
                </div>
            )
        }

        return <p>{t('department.actions.confirmMessage')}</p>
    }

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

            <section className="partner-list__department-panel" aria-label={t('department.title')}>
                <div className="partner-list__department-header">
                    <h2 className="partner-list__department-title">{t('department.title')}</h2>
                    <div className="partner-list__department-controls">
                        <label htmlFor="partner-department-select" className="partner-list__department-label">
                            {t('department.partner')}
                        </label>
                        <select
                            id="partner-department-select"
                            className="partner-list__department-select"
                            value={selectedPartnerId ?? ''}
                            onChange={(e) => setSelectedPartnerId(Number(e.target.value))}
                        >
                            {partners.map((p) => (
                                <option key={p.id} value={p.id}>{p.name}</option>
                            ))}
                        </select>
                    </div>
                </div>

                <div className="partner-list__department-tabs">
                    {DEPARTMENT_TABS.map((tab) => (
                        <button
                            key={tab}
                            type="button"
                            className={`partner-list__dept-tab ${department === tab ? 'partner-list__dept-tab--active' : ''}`}
                            onClick={() => setDepartment(tab)}
                        >
                            {t(`department.tabs.${tab}`)}
                        </button>
                    ))}
                </div>

                {!selectedPartner && (
                    <p className="partner-list__department-empty">{t('department.noPartner')}</p>
                )}

                {selectedPartner && (
                    <Card>
                        <div className="partner-list__department-actions">
                            {department === 'INVESTMENTS' && (
                                <Button size="sm" onClick={() => openActionModal('CREATE_INVESTMENT')}>{t('department.actions.createInvestment')}</Button>
                            )}
                            {department === 'WITHDRAWALS' && (
                                <Button size="sm" onClick={() => openActionModal('CREATE_WITHDRAWAL')}>{t('department.actions.createWithdrawal')}</Button>
                            )}
                            {department === 'COMMISSIONS' && (
                                <>
                                    <Button size="sm" onClick={() => openActionModal('CREATE_COMMISSION')}>{t('department.actions.createCommission')}</Button>
                                    <Button size="sm" variant="secondary" onClick={() => openActionModal('BULK_APPROVE_COMMISSIONS')}>{t('department.actions.bulkApproveCommissions')}</Button>
                                </>
                            )}
                            {department === 'ACQUISITIONS' && (
                                <Button size="sm" onClick={() => openActionModal('ASSIGN_ACQUISITION')}>{t('department.actions.assignCustomer')}</Button>
                            )}
                            {department === 'PROFIT_CONTROL' && (
                                <>
                                    <Button size="sm" onClick={() => openActionModal('UPDATE_PROFIT_CONFIG')}>{t('department.actions.updateProfitConfig')}</Button>
                                    <Button size="sm" variant="secondary" disabled={!canStartProfitSharing(selectedPartner?.profitSharingActive)} onClick={() => openActionModal('START_PROFIT_SHARING')}>{t('department.actions.startSharing')}</Button>
                                    <Button size="sm" variant="secondary" disabled={!canPauseProfitSharing(selectedPartner?.profitSharingActive)} onClick={() => openActionModal('PAUSE_PROFIT_SHARING')}>{t('department.actions.pauseSharing')}</Button>
                                    <Button size="sm" variant="secondary" disabled={!canResumeProfitSharing(selectedPartner?.profitSharingActive)} onClick={() => openActionModal('RESUME_PROFIT_SHARING')}>{t('department.actions.resumeSharing')}</Button>
                                </>
                            )}
                        </div>

                        {departmentError && <p className="partner-list__error-text">{departmentError}</p>}
                        {departmentLoading && <p>{t('department.loading')}</p>}

                        {!departmentLoading && department === 'OVERVIEW' && (
                            <div className="partner-list__overview-grid">
                                <div className="partner-list__overview-card"><strong>{t('department.cards.investments')}</strong><span>{investments.length}</span></div>
                                <div className="partner-list__overview-card"><strong>{t('department.cards.withdrawals')}</strong><span>{withdrawals.length}</span></div>
                                <div className="partner-list__overview-card"><strong>{t('department.cards.commissions')}</strong><span>{commissions.length}</span></div>
                                <div className="partner-list__overview-card"><strong>{t('department.cards.acquisitions')}</strong><span>{acquisitions.length}</span></div>
                                <div className="partner-list__overview-card"><strong>{t('department.cards.monthlyProfits')}</strong><span>{monthlyProfits.length}</span></div>
                                <div className="partner-list__overview-card"><strong>{t('department.cards.eligibility')}</strong><span>{isEligible ? t('details.yes') : t('details.no')}</span></div>
                                <div className="partner-list__overview-card"><strong>{t('department.cards.pendingCommissions')}</strong><span>{formatCurrency(commissionSummary?.pendingAmount ?? 0)}</span></div>
                                <div className="partner-list__overview-card"><strong>{t('department.cards.paidCommissions')}</strong><span>{formatCurrency(commissionSummary?.paidAmount ?? 0)}</span></div>
                            </div>
                        )}

                        {!departmentLoading && department === 'INVESTMENTS' && (
                            <ul className="partner-list__department-list">
                                {investments.map((item) => (
                                    <li key={item.id} className="partner-list__department-list-item">
                                        <span>{formatCurrency(item.amount)} - {t(`investmentStatus.${item.status}`)}</span>
                                        <div className="partner-list__row-actions">
                                            <Button size="sm" variant="secondary" onClick={() => void openInvestmentDetail(item.id)}>{t('department.actions.viewDetails')}</Button>
                                            {item.status === InvestmentStatus.PENDING && (
                                                <Button size="sm" variant="secondary" onClick={() => openActionModal('CONFIRM_INVESTMENT', item.id)}>{t('department.actions.confirmInvestment')}</Button>
                                            )}
                                        </div>
                                    </li>
                                ))}
                                {investments.length === 0 && <li>{t('investment.empty')}</li>}
                            </ul>
                        )}

                        {!departmentLoading && department === 'WITHDRAWALS' && (
                            <ul className="partner-list__department-list">
                                {withdrawals.map((item) => (
                                    <li key={item.id} className="partner-list__department-list-item">
                                        <span>{formatCurrency(item.amount)} - {t(`withdrawalStatus.${item.status}`)}</span>
                                        <div className="partner-list__row-actions">
                                            <Button size="sm" variant="secondary" onClick={() => void openWithdrawalDetail(item.id)}>{t('department.actions.viewDetails')}</Button>
                                            <Button size="sm" variant="secondary" disabled={!canApproveWithdrawal(item.status)} onClick={() => openActionModal('APPROVE_WITHDRAWAL', item.id)}>{t('department.actions.approve')}</Button>
                                            <Button size="sm" variant="secondary" disabled={!canProcessWithdrawal(item.status)} onClick={() => openActionModal('PROCESS_WITHDRAWAL', item.id)}>{t('department.actions.process')}</Button>
                                            <Button size="sm" variant="danger" disabled={!canRejectWithdrawal(item.status)} onClick={() => openActionModal('REJECT_WITHDRAWAL', item.id)}>{t('department.actions.reject')}</Button>
                                        </div>
                                    </li>
                                ))}
                                {withdrawals.length === 0 && <li>{t('withdrawal.empty')}</li>}
                            </ul>
                        )}

                        {!departmentLoading && department === 'COMMISSIONS' && (
                            <>
                                {commissionSummary && (
                                    <div className="partner-list__summary-strip">
                                        <span>{t('department.cards.pendingCommissions')}: {formatCurrency(commissionSummary.pendingAmount)}</span>
                                        <span>{t('department.cards.paidCommissions')}: {formatCurrency(commissionSummary.paidAmount)}</span>
                                        <span>{t('department.cards.totalCommissions')}: {formatCurrency(commissionSummary.totalAmount)}</span>
                                    </div>
                                )}
                                <ul className="partner-list__department-list">
                                    {commissions.map((item) => (
                                        <li key={item.id} className="partner-list__department-list-item">
                                            <span>{formatCurrency(item.commissionAmount)} - {t(`commissionStatus.${item.status}`)}</span>
                                            <div className="partner-list__row-actions">
                                                <Button size="sm" variant="secondary" onClick={() => void openCommissionReconciliation(item.id)}>{t('department.actions.viewReconciliation')}</Button>
                                                <Button size="sm" variant="secondary" disabled={!canApproveCommission(item.status)} onClick={() => openActionModal('APPROVE_COMMISSION', item.id)}>{t('department.actions.approve')}</Button>
                                                <Button size="sm" variant="secondary" disabled={!canPayCommission(item.status)} onClick={() => openActionModal('PAY_COMMISSION', item.id)}>{t('department.actions.pay')}</Button>
                                                <Button size="sm" variant="danger" disabled={!canCancelCommission(item.status)} onClick={() => openActionModal('CANCEL_COMMISSION', item.id)}>{t('department.actions.cancel')}</Button>
                                            </div>
                                        </li>
                                    ))}
                                    {commissions.length === 0 && <li>{t('commission.empty')}</li>}
                                </ul>
                            </>
                        )}

                        {!departmentLoading && department === 'ACQUISITIONS' && (
                            <ul className="partner-list__department-list">
                                {acquisitions.map((item) => (
                                    <li key={item.id} className="partner-list__department-list-item">
                                        <span>{item.customerName} - {item.commissionPercentage}%</span>
                                        <div className="partner-list__row-actions">
                                            <Button size="sm" variant="secondary" onClick={() => openActionModal('UPDATE_ACQ_COMMISSION', item.customerId)}>{t('department.actions.updateCommission')}</Button>
                                            <Button size="sm" variant="secondary" onClick={() => openActionModal('TRANSFER_ACQUISITION', item.customerId)}>{t('department.actions.transfer')}</Button>
                                        </div>
                                    </li>
                                ))}
                                {acquisitions.length === 0 && <li>{t('acquisition.empty')}</li>}
                            </ul>
                        )}

                        {!departmentLoading && department === 'MONTHLY_PROFITS' && (
                            <ul className="partner-list__department-list">
                                {monthlyProfits.map((item) => (
                                    <li key={item.id} className="partner-list__department-list-item">
                                        <span>
                                            {item.profitDistributionMonth} - {formatCurrency(item.calculatedProfit)}
                                            {item.paidByName ? ` (${item.paidByName})` : ''}
                                        </span>
                                        <div className="partner-list__row-actions">
                                            <Button size="sm" variant="secondary" onClick={() => void openMonthlyProfitReconciliation(item.id)}>{t('department.actions.viewReconciliation')}</Button>
                                            <Button size="sm" variant="secondary" disabled={!canPayMonthlyProfit(item.status)} onClick={() => openActionModal('PAY_MONTHLY_PROFIT', item.id)}>{t('department.actions.pay')}</Button>
                                            <Button size="sm" variant="secondary" disabled={!canAdjustMonthlyProfit(item.status)} onClick={() => openActionModal('ADJUST_MONTHLY_PROFIT', item.id)}>{t('department.actions.adjust')}</Button>
                                        </div>
                                    </li>
                                ))}
                                {monthlyProfits.length === 0 && <li>{t('monthlyProfit.empty')}</li>}
                            </ul>
                        )}

                        {!departmentLoading && department === 'PROFIT_CONTROL' && (
                            <div className="partner-list__profit-config">
                                <p>{t('department.config.managementFee')}: {profitConfig?.managementFeePercentage ?? '—'}%</p>
                                <p>{t('department.config.zakat')}: {profitConfig?.zakatPercentage ?? '—'}%</p>
                                <p>{t('department.config.paymentDay')}: {profitConfig?.profitPaymentDay ?? '—'}</p>
                                <p>{t('department.config.eligibleThisMonth')}: {isEligible ? t('details.yes') : t('details.no')}</p>
                            </div>
                        )}
                    </Card>
                )}
            </section>

            <Modal
                isOpen={actionModal != null}
                onClose={closeActionModal}
                title={actionModalTitle}
                footer={
                    <>
                        <Button variant="secondary" onClick={closeActionModal}>{tc('close')}</Button>
                        {!isReadOnlyModal && <Button onClick={handleActionSubmit} loading={actionLoading}>{tc('save')}</Button>}
                    </>
                }
            >
                {actionError && <p className="partner-list__error-text">{actionError}</p>}
                {actionLoading && <p>{t('department.loading')}</p>}
                {renderActionModalBody()}
            </Modal>
        </div>
    )
}
