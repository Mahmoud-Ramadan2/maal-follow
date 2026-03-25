import { api } from '@services/api'
import type { PaginatedResponse } from '@/types/common.types'
import type {
    InstallmentSchedule, InstallmentScheduleRequest,
    GenerateScheduleParams, RescheduleParams,
    PaymentStatus, MonthlyCollectionSummary,
} from '@/types/modules/contract.types'

const BASE = '/v1/installment-schedules'

export const installmentScheduleApi = {
    // ── Generation ──────────────────────────────────────────
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

    // ── Reschedule ──────────────────────────────────────────
    async reschedule(contractId: number, params: RescheduleParams): Promise<InstallmentSchedule[]> {
        return api.put<InstallmentSchedule[]>(`${BASE}/reschedule/${contractId}`, null, { params })
    },
    async skipMonth(contractId: number, reason: string): Promise<void> {
        await api.put(`${BASE}/skip-month/${contractId}`, null, { params: { reason } })
    },

    // ── CRUD ────────────────────────────────────────────────
    async create(data: InstallmentScheduleRequest): Promise<InstallmentSchedule> {
        return api.post<InstallmentSchedule>(BASE, data)
    },
    async update(id: number, data: InstallmentScheduleRequest): Promise<InstallmentSchedule> {
        return api.put<InstallmentSchedule>(`${BASE}/${id}`, data)
    },

    // ── Queries ─────────────────────────────────────────────
    async getByContract(contractId: number): Promise<InstallmentSchedule[]> {
        return api.get<InstallmentSchedule[]>(`${BASE}/contract/${contractId}`)
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
    async getByStatus(status: PaymentStatus, page = 0, size = 20): Promise<PaginatedResponse<InstallmentSchedule>> {
        return api.get<PaginatedResponse<InstallmentSchedule>>(`${BASE}/by-status/${status}`, { params: { page, size } })
    },
    async getMonthlySummary(month: string): Promise<MonthlyCollectionSummary> {
        return api.get<MonthlyCollectionSummary>(`${BASE}/monthly-summary`, { params: { month } })
    },
} as const

