import { api } from '@services/api'
import type {
    PartnerCustomerAcquisition,
    PartnerCustomerAcquisitionRequest,
    PartnerPerformanceMetrics,
} from '@/types/modules/partner.types'

const BASE = '/partner-customer-acquisitions'

export const partnerCustomerAcquisitionApi = {
    async assign(data: PartnerCustomerAcquisitionRequest): Promise<PartnerCustomerAcquisition> {
        return api.post<PartnerCustomerAcquisition>(BASE, data)
    },
    async getByPartner(partnerId: number): Promise<PartnerCustomerAcquisition[]> {
        return api.get<PartnerCustomerAcquisition[]>(`${BASE}/partner/${partnerId}`)
    },
    async transfer(customerId: number, fromPartnerId: number, toPartnerId: number, reason?: string): Promise<void> {
        await api.put<void>(`${BASE}/transfer`, null, {
            params: { customerId, fromPartnerId, toPartnerId, ...(reason ? { reason } : {}) },
        })
    },
    async getPerformance(partnerId: number): Promise<PartnerPerformanceMetrics> {
        return api.get<PartnerPerformanceMetrics>(`${BASE}/partner/${partnerId}/performance`)
    },
    async updateCommission(partnerId: number, customerId: number, commissionAmount: number): Promise<void> {
        await api.put<void>(`${BASE}/commission`, null, { params: { partnerId, customerId, commissionAmount } })
    },
} as const

