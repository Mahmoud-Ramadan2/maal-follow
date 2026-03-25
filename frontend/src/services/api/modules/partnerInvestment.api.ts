import { api } from '@services/api'
import type { PartnerInvestment, PartnerInvestmentRequest } from '@/types/modules/partner.types'

const BASE = '/v1/partner-investments'

export const partnerInvestmentApi = {
    async create(data: PartnerInvestmentRequest): Promise<PartnerInvestment> {
        return api.post<PartnerInvestment>(BASE, data)
    },
    async getByPartner(partnerId: number): Promise<PartnerInvestment[]> {
        return api.get<PartnerInvestment[]>(`${BASE}/${partnerId}/by-partner`)
    },
    async getById(id: number): Promise<PartnerInvestment> {
        return api.get<PartnerInvestment>(`${BASE}/${id}`)
    },
    async confirm(id: number): Promise<PartnerInvestment> {
        return api.post<PartnerInvestment>(`${BASE}/${id}/confirm`)
    },
} as const

