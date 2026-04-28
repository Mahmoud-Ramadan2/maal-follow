import { api } from '@services/api'
import type {
    PartnerMonthlyProfit,
    PartnerMonthlyProfitAdjustRequest,
    PartnerMonthlyProfitPayRequest,
    PayoutReconciliation,
} from '@/types/modules/partner.types'

const BASE = '/partner-monthly-profits'

export const partnerMonthlyProfitApi = {
    async getByPartner(partnerId: number): Promise<PartnerMonthlyProfit[]> {
        return api.get<PartnerMonthlyProfit[]>(`${BASE}/partner/${partnerId}`)
    },
    async getByDistribution(distributionId: number): Promise<PartnerMonthlyProfit[]> {
        return api.get<PartnerMonthlyProfit[]>(`${BASE}/distribution/${distributionId}`)
    },
    async pay(id: number, data: PartnerMonthlyProfitPayRequest): Promise<PartnerMonthlyProfit> {
        return api.post<PartnerMonthlyProfit>(`${BASE}/${id}/pay`, data)
    },
    async adjust(id: number, data: PartnerMonthlyProfitAdjustRequest): Promise<PartnerMonthlyProfit> {
        return api.post<PartnerMonthlyProfit>(`${BASE}/${id}/adjust`, data)
    },
    async getReconciliation(id: number): Promise<PayoutReconciliation> {
        return api.get<PayoutReconciliation>(`${BASE}/${id}/reconciliation`)
    },
} as const

