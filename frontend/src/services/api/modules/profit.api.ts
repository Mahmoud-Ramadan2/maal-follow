import { api } from '@services/api'
import type {
    MonthlyProfitDistribution,
    MonthlyProfitDistributionRequest,
    ProfitDistributionFilters,
    ProfitDistributionLifecycleStatus,
} from '@/types/modules/profit.types'

const BASE = '/profit-distributions'

export const profitApi = {
    async create(data: MonthlyProfitDistributionRequest): Promise<MonthlyProfitDistribution> {
        return api.post<MonthlyProfitDistribution>(BASE, data)
    },
    async getById(id: number): Promise<MonthlyProfitDistribution> {
        return api.get<MonthlyProfitDistribution>(`${BASE}/${id}`)
    },
    async getByMonth(monthYear: string): Promise<MonthlyProfitDistribution> {
        return api.get<MonthlyProfitDistribution>(`${BASE}/month/${encodeURIComponent(monthYear)}`)
    },
    async getAll(filters?: ProfitDistributionFilters): Promise<MonthlyProfitDistribution[]> {
        return api.get<MonthlyProfitDistribution[]>(BASE, { params: filters })
    },
    async update(id: number, data: MonthlyProfitDistributionRequest): Promise<MonthlyProfitDistribution> {
        return api.put<MonthlyProfitDistribution>(`${BASE}/${id}`, data)
    },
    async calculate(id: number): Promise<MonthlyProfitDistribution> {
        return api.post<MonthlyProfitDistribution>(`${BASE}/${id}/calculate`)
    },
    async recalculate(id: number): Promise<MonthlyProfitDistribution> {
        return api.post<MonthlyProfitDistribution>(`${BASE}/${id}/recalculate`)
    },
    async distribute(id: number): Promise<MonthlyProfitDistribution> {
        return api.post<MonthlyProfitDistribution>(`${BASE}/${id}/distribute`)
    },
    async lock(id: number): Promise<MonthlyProfitDistribution> {
        return api.post<MonthlyProfitDistribution>(`${BASE}/${id}/lock`)
    },
    async getStatus(id: number): Promise<ProfitDistributionLifecycleStatus> {
        return api.get<ProfitDistributionLifecycleStatus>(`${BASE}/${id}/status`)
    },
    async delete(id: number): Promise<void> {
        await api.del<void>(`${BASE}/${id}`)
    },
} as const

