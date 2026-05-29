import { api } from '@services/api'
import { compactParams } from '@services/api/shared/compactParams'
import type { PaginatedResponse } from '@/types/common.types'
import type {
    DailyLedgerSummary,
    LedgerFilters,
    LedgerRequest,
    LedgerResponse,
    LedgerSource,
    LedgerStatistics,
    LedgerSummary,
    LedgerType,
} from '@/types/modules/ledger.types'

const BASE = '/ledger'

export const ledgerApi = {
    async create(data: LedgerRequest): Promise<LedgerResponse> {
        return api.post<LedgerResponse>(BASE, data)
    },
    async getById(id: number): Promise<LedgerResponse> {
        return api.get<LedgerResponse>(`${BASE}/${id}`)
    },
    async getByIdempotencyKey(idempotencyKey: string): Promise<LedgerResponse> {
        return api.get<LedgerResponse>(`${BASE}/by-idempotency-key/${encodeURIComponent(idempotencyKey)}`)
    },
    async update(id: number, data: LedgerRequest): Promise<LedgerResponse> {
        return api.put<LedgerResponse>(`${BASE}/${id}`, data)
    },
    async getAll(filters: LedgerFilters = {}): Promise<PaginatedResponse<LedgerSummary>> {
        const { search, ...rest } = filters
        return api.get<PaginatedResponse<LedgerSummary>>(BASE, {
            params: compactParams({ search, ...rest }),
        })
    },
    async getByDate(date: string, page = 0, size = 20): Promise<PaginatedResponse<LedgerSummary>> {
        return api.get<PaginatedResponse<LedgerSummary>>(`${BASE}/by-date/${encodeURIComponent(date)}`, { params: { page, size } })
    },
    async getByType(type: LedgerType, page = 0, size = 20): Promise<PaginatedResponse<LedgerSummary>> {
        return api.get<PaginatedResponse<LedgerSummary>>(`${BASE}/by-type/${type}`, { params: { page, size } })
    },
    async getBySource(source: LedgerSource, page = 0, size = 20): Promise<PaginatedResponse<LedgerSummary>> {
        return api.get<PaginatedResponse<LedgerSummary>>(`${BASE}/by-source/${source}`, { params: { page, size } })
    },
    async getByDateRange(startDate: string, endDate: string, page = 0, size = 20): Promise<PaginatedResponse<LedgerSummary>> {
        return api.get<PaginatedResponse<LedgerSummary>>(`${BASE}/by-date-range`, { params: { startDate, endDate, page, size } })
    },
    async getByPartner(partnerId: number, page = 0, size = 20): Promise<PaginatedResponse<LedgerSummary>> {
        return api.get<PaginatedResponse<LedgerSummary>>(`${BASE}/by-partner/${partnerId}`, { params: { page, size } })
    },
    async getDailySummary(startDate: string, endDate: string): Promise<DailyLedgerSummary[]> {
        return api.get<DailyLedgerSummary[]>(`${BASE}/daily-summary`, { params: compactParams({ startDate, endDate }) })
    },
    async getStatistics(): Promise<LedgerStatistics> {
        return api.get<LedgerStatistics>(`${BASE}/stats`)
    },
    async delete(id: number): Promise<string> {
        return api.del<string>(`${BASE}/${id}`)
    },
} as const

