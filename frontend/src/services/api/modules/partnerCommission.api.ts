import { api } from '@services/api'
import type { PaginatedResponse } from '@/types/common.types'
import type { PartnerCommission, PartnerCommissionRequest } from '@/types/modules/partner.types'

const BASE = '/v1/partner/commissions'

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
} as const

