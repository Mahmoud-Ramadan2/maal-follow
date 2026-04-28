import { api } from '@services/api'
import type { PartnerProfitConfig, PartnerProfitConfigRequest } from '@/types/modules/partner.types'

const BASE = '/partner-profit-controls'

export const partnerProfitControlApi = {
    async getCurrentConfig(): Promise<PartnerProfitConfig> {
        return api.get<PartnerProfitConfig>(`${BASE}/current`)
    },
    async updateConfig(data: PartnerProfitConfigRequest): Promise<void> {
        await api.put<void>(`${BASE}`, data)
    },
    async startProfitSharing(partnerId: number, startDate: string): Promise<void> {
        await api.post<void>(`${BASE}/${partnerId}/sharing/start`, null, { params: { startDate } })
    },
    async pauseProfitSharing(partnerId: number, reason?: string): Promise<void> {
        await api.post<void>(`${BASE}/${partnerId}/sharing/pause`, null, reason ? { params: { reason } } : undefined)
    },
    async resumeProfitSharing(partnerId: number): Promise<void> {
        await api.post<void>(`${BASE}/${partnerId}/sharing/resume`)
    },
    async isEligible(partnerId: number, month: string): Promise<boolean> {
        return api.get<boolean>(`${BASE}/${partnerId}/sharing/eligibility`, { params: { month } })
    },
} as const

