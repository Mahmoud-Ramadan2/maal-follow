import { api } from '@services/api'
import type { PartnerWithdrawal, PartnerWithdrawalRequest } from '@/types/modules/partner.types'

const BASE = '/partner-withdrawals'

export const partnerWithdrawalApi = {
    async create(data: PartnerWithdrawalRequest): Promise<PartnerWithdrawal> {
        return api.post<PartnerWithdrawal>(BASE, data)
    },
    async getByPartner(partnerId: number): Promise<PartnerWithdrawal[]> {
        return api.get<PartnerWithdrawal[]>(`${BASE}/partner/${partnerId}`)
    },
    async getPending(): Promise<PartnerWithdrawal[]> {
        return api.get<PartnerWithdrawal[]>(`${BASE}/pending`)
    },
    async approve(id: number): Promise<PartnerWithdrawal> {
        return api.post<PartnerWithdrawal>(`${BASE}/${id}/approve`)
    },
    async process(id: number): Promise<PartnerWithdrawal> {
        return api.post<PartnerWithdrawal>(`${BASE}/${id}/process`)
    },
    async reject(id: number, reason?: string): Promise<PartnerWithdrawal> {
        return api.post<PartnerWithdrawal>(`${BASE}/${id}/reject`, null, reason ? { params: { reason } } : undefined)
    },
    async getById(id: number): Promise<PartnerWithdrawal> {
        return api.get<PartnerWithdrawal>(`${BASE}/${id}`)
    },
} as const

