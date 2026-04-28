import { api } from '@services/api'
import type { PaginatedResponse } from '@/types/common.types'
import type {
    CapitalPool, CapitalPoolRequest,
    CapitalTransaction, CapitalTransactionRequest,
    CapitalTransactionType, CapitalMonthlySummary,
} from '@/types/modules/capital.types'

const POOL_BASE = '/capital-pool'
const TX_BASE = '/capital-transactions'

export const capitalPoolApi = {
    async getCurrent(): Promise<CapitalPool> {
        return api.get<CapitalPool>(`${POOL_BASE}/current`)
    },
    async create(data: CapitalPoolRequest): Promise<CapitalPool> {
        return api.post<CapitalPool>(POOL_BASE, data)
    },
    async update(data: CapitalPoolRequest): Promise<CapitalPool> {
        return api.put<CapitalPool>(POOL_BASE, data)
    },
    async recalculate(): Promise<CapitalPool> {
        return api.post<CapitalPool>(`${POOL_BASE}/recalculate`)
    },
    async getHistory(page = 0, size = 10): Promise<PaginatedResponse<CapitalPool>> {
        return api.get<PaginatedResponse<CapitalPool>>(`${POOL_BASE}/history`, { params: { page, size } })
    },
} as const

export const capitalTransactionApi = {
    async create(data: CapitalTransactionRequest): Promise<CapitalTransaction> {
        return api.post<CapitalTransaction>(TX_BASE, data)
    },
    async getAll(page = 0, size = 20): Promise<PaginatedResponse<CapitalTransaction>> {
        return api.get<PaginatedResponse<CapitalTransaction>>(TX_BASE, { params: { page, size } })
    },
    async getByType(transactionType: CapitalTransactionType): Promise<CapitalTransaction[]> {
        return api.get<CapitalTransaction[]>(`${TX_BASE}/type/${transactionType}`)
    },
    async getByPartner(partnerId: number): Promise<CapitalTransaction[]> {
        return api.get<CapitalTransaction[]>(`${TX_BASE}/partner/${partnerId}`)
    },
    async getByDateRange(startDate: string, endDate: string): Promise<CapitalTransaction[]> {
        return api.get<CapitalTransaction[]>(`${TX_BASE}/date-range`, { params: { startDate, endDate } })
    },
    async getMonthlySummary(year: number, month: number): Promise<CapitalMonthlySummary> {
        return api.get<CapitalMonthlySummary>(`${TX_BASE}/summary/monthly/${year}/${month}`)
    },
    async getPartnerSummary(partnerId: number, startDate: string, endDate: string): Promise<CapitalMonthlySummary> {
        return api.get<CapitalMonthlySummary>(`${TX_BASE}/summary/partner/${partnerId}`, { params: { startDate, endDate } })
    },
} as const

