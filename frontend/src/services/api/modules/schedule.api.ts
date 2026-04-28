import { api } from '@services/api'
import type { PaginatedResponse } from '@/types/common.types'
import type {
    InstallmentSchedule,
    InstallmentScheduleRequest,
    GenerateScheduleParams,
    RescheduleParams,
    PaymentStatus,
    MonthlyCollectionSummary,
    ScheduleFilters,
    ScheduleEndpointDefinition,
    ScheduleEndpointKey,
    ScheduleMetadataUpdateRequest,
} from '@/types/modules/schedule.types'

const BASE = '/installment-schedules'

// const ROADMAP_BASE = '/installment-schedules'

export const scheduleEndpointCatalog: ScheduleEndpointDefinition[] = [
    { key: 'GENERATE', method: 'POST', path: `${BASE}/generate/{contractId}`, available: true, description: 'Auto-generate schedules for contract defaults.' },
    { key: 'GENERATE_CUSTOM', method: 'POST', path: `${BASE}/generate/{contractId}/custom`, available: true, description: 'Generate schedules with custom months/amount/remainder mode.' },
    { key: 'SWAP_REMAINDER', method: 'PUT', path: `${BASE}/swap-remainder/{contractId}`, available: true, description: 'Move remainder between first and last installment.' },
    { key: 'DELETE_UNPAID', method: 'DELETE', path: `${BASE}/unpaid/{contractId}`, available: true, description: 'Delete unpaid rows to regenerate schedule.' },
    { key: 'RESCHEDULE', method: 'PUT', path: `${BASE}/reschedule/{contractId}`, available: true, description: 'Reschedule unpaid installments with new parameters.' },
    { key: 'SKIP_MONTH', method: 'PUT', path: `${BASE}/skip-month/{contractId}`, available: true, description: 'Skip a month and shift unpaid installments.' },
    { key: 'CREATE', method: 'POST', path: `${BASE}`, available: true, description: 'Create one schedule row manually.' },
    { key: 'UPDATE', method: 'PUT', path: `${BASE}/{id}`, available: true, description: 'Update one schedule row.' },
    { key: 'UPDATE_METADATA', method: 'PATCH', path: `${BASE}/{id}/metadata`, available: true, description: 'Safe metadata-only update (due date, notes, collector).' },
    { key: 'GET_BY_CONTRACT', method: 'GET', path: `${BASE}/contract/{contractId}`, available: true, description: 'Paginated schedules for one contract.' },
    { key: 'GET_OVERDUE', method: 'GET', path: `${BASE}/overdue`, available: true, description: 'All overdue schedules.' },
    { key: 'GET_DUE_SOON', method: 'GET', path: `${BASE}/due-soon`, available: true, description: 'Schedules due in upcoming N days.' },
    { key: 'GET_BY_PAYMENT_DAY', method: 'GET', path: `${BASE}/by-payment-day/{day}`, available: true, description: 'Schedules by agreed payment day.' },
    { key: 'GET_BY_NAME', method: 'GET', path: `${BASE}/by-name`, available: true, description: 'Schedules by customer name.' },
    { key: 'GET_BY_STATUS', method: 'GET', path: `${BASE}/by-status/{status}`, available: true, description: 'Paginated schedules by status.' },
    { key: 'GET_MONTHLY_SUMMARY', method: 'GET', path: `${BASE}/monthly-summary`, available: true, description: 'Monthly collection summary.' },
    { key: 'GET_BY_ID', method: 'GET', path: `${BASE}/{id}`, available: true, description: 'Direct schedule details endpoint to avoid client-side searching.' },
    { key: 'SEARCH_PAGINATED', method: 'GET', path: `${BASE}/search`, available: true, description: 'Single paginated search endpoint (contract/name/status/date/day).' },
    { key: 'PAY_INSTALLMENT', method: 'POST', path: '/payments/schedule/{scheduleId}', available: false, description: 'One-click payment endpoint dedicated to a schedule row.' },
    { key: 'REMIND_BY_SCHEDULE', method: 'POST', path: '/payments/reminders/schedule/{scheduleId}', available: false, description: 'Create/send reminder for one installment schedule.' },
]

function applyClientDateFilters(rows: InstallmentSchedule[], filters: ScheduleFilters): InstallmentSchedule[] {
    if (!filters.startDate && !filters.endDate) {
        return rows
    }

    return rows.filter((row) => {
        const due = new Date(row.dueDate).getTime()
        if (Number.isNaN(due)) return true

        if (filters.startDate) {
            const start = new Date(filters.startDate).getTime()
            if (!Number.isNaN(start) && due < start) return false
        }

        if (filters.endDate) {
            const end = new Date(filters.endDate).getTime()
            if (!Number.isNaN(end) && due > end) return false
        }

        return true
    })
}

function paginateRows(rows: InstallmentSchedule[], page = 0, size = 10): PaginatedResponse<InstallmentSchedule> {
    const safePage = Math.max(0, page)
    const safeSize = Math.max(1, size)
    const start = safePage * safeSize
    const end = start + safeSize
    const content = rows.slice(start, end)
    const totalElements = rows.length
    const totalPages = totalElements === 0 ? 0 : Math.ceil(totalElements / safeSize)

    return {
        content,
        totalElements,
        totalPages,
        size: safeSize,
        number: safePage,
        first: safePage === 0,
        last: safePage + 1 >= totalPages,
        empty: content.length === 0,
    }
}

export const scheduleApi = {
    // Generation
    async generate(contractId: number): Promise<InstallmentSchedule[]> {
        return api.post<InstallmentSchedule[]>(`${BASE}/generate/${contractId}`)
    },
    async generateCustom(contractId: number, params: GenerateScheduleParams): Promise<InstallmentSchedule[]> {
        return api.post<InstallmentSchedule[]>(`${BASE}/generate/${contractId}/custom`, null, { params })
    },
    async swapRemainder(contractId: number): Promise<InstallmentSchedule[]> {
        return api.put<InstallmentSchedule[]>(`${BASE}/swap-remainder/${contractId}`)
    },
    async deleteUnpaid(contractId: number): Promise<void> {
        await api.del(`${BASE}/unpaid/${contractId}`)
    },

    // Reschedule
    async rescheduleUnpaidInstallments(contractId: number, params: RescheduleParams): Promise<InstallmentSchedule[]> {
        return api.put<InstallmentSchedule[]>(`${BASE}/reschedule/${contractId}`, null, { params })
    },
    async skipMonthPayment(contractId: number, reason: string): Promise<void> {
        await api.put(`${BASE}/skip-month/${contractId}`, null, { params: { reason } })
    },

    // CRUD
    async create(data: InstallmentScheduleRequest): Promise<InstallmentSchedule> {
        return api.post<InstallmentSchedule>(BASE, data)
    },
    async update(id: number, data: InstallmentScheduleRequest): Promise<InstallmentSchedule> {
        return api.put<InstallmentSchedule>(`${BASE}/${id}`, data)
    },
    async updateScheduleMetadata(id: number, data: ScheduleMetadataUpdateRequest): Promise<InstallmentSchedule> {
        return api.patch<InstallmentSchedule>(`${BASE}/${id}/metadata`, data)
    },

    // Backward-compatible aliases
    async reschedule(contractId: number, params: RescheduleParams): Promise<InstallmentSchedule[]> {
        return this.rescheduleUnpaidInstallments(contractId, params)
    },
    async skipMonth(contractId: number, reason: string): Promise<void> {
        await this.skipMonthPayment(contractId, reason)
    },
    async updateMetadata(id: number, data: ScheduleMetadataUpdateRequest): Promise<InstallmentSchedule> {
        return this.updateScheduleMetadata(id, data)
    },

    // Queries
    async getByContract(contractId: number, page = 0, size = 10, sort?: string): Promise<PaginatedResponse<InstallmentSchedule>> {
        return api.get<PaginatedResponse<InstallmentSchedule>>(`${BASE}/contract/${contractId}`, {
            params: {
                page,
                size,
                ...(sort ? { sort } : {}),
            },
        })
    },
    async getOverdue(): Promise<InstallmentSchedule[]> {
        return api.get<InstallmentSchedule[]>(`${BASE}/overdue`)
    },
    async getDueSoon(daysAhead = 5): Promise<InstallmentSchedule[]> {
        return api.get<InstallmentSchedule[]>(`${BASE}/due-soon`, { params: { daysAhead } })
    },
    async getByPaymentDay(day: number): Promise<InstallmentSchedule[]> {
        return api.get<InstallmentSchedule[]>(`${BASE}/by-payment-day/${day}`)
    },
    async getByName(name: string): Promise<InstallmentSchedule[]> {
        return api.get<InstallmentSchedule[]>(`${BASE}/by-name`, { params: { name } })
    },
    async getById(id: number): Promise<InstallmentSchedule> {
        return api.get<InstallmentSchedule>(`${BASE}/${id}`)
    },
    async getByStatus(status: PaymentStatus, page = 0, size = 20): Promise<PaginatedResponse<InstallmentSchedule>> {
        return api.get<PaginatedResponse<InstallmentSchedule>>(`${BASE}/by-status/${status}`, { params: { page, size } })
    },
    async getMonthlySummary(month: string): Promise<MonthlyCollectionSummary> {
        return api.get<MonthlyCollectionSummary>(`${BASE}/monthly-summary`, { params: { month } })
    },

    async searchSchedules(filters: ScheduleFilters = {}): Promise<PaginatedResponse<InstallmentSchedule>> {
        return api.get<PaginatedResponse<InstallmentSchedule>>(`${BASE}/search`, {
            params: {
                page: filters.page ?? 0,
                size: filters.size ?? 10,
                ...(filters.sort ? { sort: filters.sort } : {}),
                ...(filters.contractId ? { contractId: filters.contractId } : {}),
                ...(filters.status ? { status: filters.status } : {}),
                ...(filters.name?.trim() ? { name: filters.name.trim() } : {}),
                ...(typeof filters.paymentDay === 'number' ? { paymentDay: filters.paymentDay } : {}),
                ...(filters.startDate ? { startDate: filters.startDate } : {}),
                ...(filters.endDate ? { endDate: filters.endDate } : {}),
                ...(filters.overdueOnly ? { overdueOnly: true } : {}),
                ...(typeof filters.dueSoonDays === 'number' ? { dueSoonDays: filters.dueSoonDays } : {}),
            },
        })
    },

    // Backward-compatible alias
    async search(filters: ScheduleFilters = {}): Promise<PaginatedResponse<InstallmentSchedule>> {
        return this.searchSchedules(filters)
    },

    /**
     * Unified schedule query strategy used by list and dashboards.
     */
    async query(filters: ScheduleFilters = {}): Promise<PaginatedResponse<InstallmentSchedule>> {
        try {
            return await this.searchSchedules(filters)
        } catch {
            // Backward-compatible fallback while backend search endpoint rolls out.
            const page = filters.page ?? 0
            const size = filters.size ?? 10
            const status = filters.status ?? 'PENDING'

            if (filters.contractId) {
                return this.getByContract(filters.contractId, page, size, filters.sort)
            }

            if (filters.overdueOnly) {
                const rows = await this.getOverdue()
                return paginateRows(applyClientDateFilters(rows, filters), page, size)
            }

            if (typeof filters.dueSoonDays === 'number' && filters.dueSoonDays > 0) {
                const rows = await this.getDueSoon(filters.dueSoonDays)
                return paginateRows(applyClientDateFilters(rows, filters), page, size)
            }

            if (typeof filters.paymentDay === 'number' && filters.paymentDay >= 1 && filters.paymentDay <= 31) {
                const rows = await this.getByPaymentDay(filters.paymentDay)
                return paginateRows(applyClientDateFilters(rows, filters), page, size)
            }

            if (filters.name && filters.name.trim()) {
                const rows = await this.getByName(filters.name.trim())
                return paginateRows(applyClientDateFilters(rows, filters), page, size)
            }

            return this.getByStatus(status, page, size)
        }
    },

    getEndpointCatalog(includeComingSoon = true): ScheduleEndpointDefinition[] {
        return includeComingSoon
            ? scheduleEndpointCatalog
            : scheduleEndpointCatalog.filter((entry) => entry.available)
    },

    isEndpointAvailable(key: ScheduleEndpointKey): boolean {
        return scheduleEndpointCatalog.find((entry) => entry.key === key)?.available === true
    },
} as const

