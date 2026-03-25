import { api } from '@services/api'
import type { Partner, PartnerRequest, PartnerStatus } from '@/types/modules/partner.types'

const BASE = '/partners'

export const partnerApi = {
    async create(data: PartnerRequest): Promise<Partner> {
        return api.post<Partner>(BASE, data)
    },
    async getById(id: number): Promise<Partner> {
        return api.get<Partner>(`${BASE}/${id}`)
    },
    async getAll(status?: PartnerStatus): Promise<Partner[]> {
        return api.get<Partner[]>(BASE, status ? { params: { status } } : undefined)
    },
    async update(id: number, data: PartnerRequest): Promise<Partner> {
        return api.put<Partner>(`${BASE}/${id}`, data)
    },
    async delete(id: number): Promise<void> {
        await api.del(`${BASE}/${id}`)
    },
} as const

