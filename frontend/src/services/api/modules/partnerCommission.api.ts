import { api } from '@services/api'
import type { PaginatedResponse } from '@/types/common.types'
import type {
    PartnerAcquisitionCommissionRequest,
    PartnerCommission,
    PartnerCommissionRequest,
    PartnerCommissionSummary,
    PartnerSalesCommissionRequest,
    PayoutReconciliation,
} from '@/types/modules/partner.types'

const BASE = '/partner-commissions'

export const partnerCommissionApi = {
    async create(data: PartnerCommissionRequest): Promise<PartnerCommission> {
        return api.post<PartnerCommission>(BASE, data)
    },
    async getByPartner(partnerId: number, page = 0, size = 10): Promise<PaginatedResponse<PartnerCommission>> {
        return api.get<PaginatedResponse<PartnerCommission>>(`${BASE}/partner/${partnerId}`, { params: { page, size } })
    },
    async getByContract(contractId: number): Promise<PartnerCommission[]> {
        return api.get<PartnerCommission[]>(`${BASE}/contract/${contractId}`)
    },
    async approve(id: number, approvedByUserId: number): Promise<PartnerCommission> {
        return api.put<PartnerCommission>(`${BASE}/${id}/approve`, null, { params: { approvedByUserId } })
    },
    async pay(id: number): Promise<PartnerCommission> {
        return api.put<PartnerCommission>(`${BASE}/${id}/pay`)
    },
    async cancel(id: number, reason?: string): Promise<void> {
        await api.put<void>(`${BASE}/${id}/cancel`, null, reason ? { params: { reason } } : undefined)
    },
    async bulkApprove(partnerId: number, approvedByUserId: number): Promise<void> {
        await api.put<void>(`${BASE}/bulk-approve`, null, { params: { partnerId, approvedByUserId } })
    },
    async getSummary(partnerId: number): Promise<PartnerCommissionSummary> {
        return api.get<PartnerCommissionSummary>(`${BASE}/${partnerId}/summary`)
    },
    async createSalesCommission(data: PartnerSalesCommissionRequest): Promise<PartnerCommission> {
        return api.post<PartnerCommission>(`${BASE}/sales`, data)
    },
    async createAcquisitionCommission(data: PartnerAcquisitionCommissionRequest): Promise<PartnerCommission> {
        return api.post<PartnerCommission>(`${BASE}/acquisition`, data)
    },
    async getReconciliation(id: number): Promise<PayoutReconciliation> {
        return api.get<PayoutReconciliation>(`${BASE}/${id}/reconciliation`)
    },
} as const

