import { api } from '@services/api'
import type { PaginatedResponse } from '@/types/common.types'
import type {
    Payment, PaymentSummary, PaymentRequest, PaymentStatistics,
    DailyPaymentSummary, PaymentDiscountConfig,
    DiscountType, PaymentSearchFilters, PaymentReportSummary, PaymentDiscountConfigRequest,
} from '@/types/modules/payment.types'

const BASE = '/payments'

const searchPayments = async (filters: PaymentSearchFilters = {}): Promise<PaginatedResponse<PaymentSummary>> => {
    const { page = 0, size = 20, ...rest } = filters
    return api.get<PaginatedResponse<PaymentSummary>>(`${BASE}/search`, { params: { ...rest, page, size } })
}

export const paymentApi = {
    // ── CRUD ────────────────────────────────────────────────
    async create(data: PaymentRequest): Promise<Payment> {
        return api.post<Payment>(BASE, data)
    },
    async getById(id: number): Promise<Payment> {
        return api.get<Payment>(`${BASE}/${id}`)
    },
    async getByIdempotencyKey(idempotencyKey: string): Promise<Payment> {
        return api.get<Payment>(`${BASE}/by-key/${encodeURIComponent(idempotencyKey)}`)
    },
    async getAll(page = 0, size = 20): Promise<PaginatedResponse<PaymentSummary>> {
        return api.get<PaginatedResponse<PaymentSummary>>(BASE, { params: { page, size } })
    },
    async getByMonth(month: string, page = 0, size = 20): Promise<PaginatedResponse<PaymentSummary>> {
        return api.get<PaginatedResponse<PaymentSummary>>(`${BASE}/month/${encodeURIComponent(month)}`, { params: { page, size } })
    },
    async getByDateRange(startDate: string, endDate: string, page = 0, size = 20): Promise<PaginatedResponse<PaymentSummary>> {
        return api.get<PaginatedResponse<PaymentSummary>>(`${BASE}/date-range`, { params: { startDate, endDate, page, size } })
    },
    async getEarlyPayments(page = 0, size = 20): Promise<PaginatedResponse<PaymentSummary>> {
        return api.get<PaginatedResponse<PaymentSummary>>(`${BASE}/early-payments`, { params: { page, size } })
    },
    async search(filters: PaymentSearchFilters = {}): Promise<PaginatedResponse<PaymentSummary>> {
        return searchPayments(filters)
    },
    async getReport(filters: Omit<PaymentSearchFilters, 'page' | 'size' | 'sort'> = {}): Promise<PaymentReportSummary> {
        return api.get<PaymentReportSummary>(`${BASE}/reports/summary`, { params: filters })
    },

    // ── Actions ─────────────────────────────────────────────
    async cancel(id: number, reason?: string): Promise<Payment> {
        return api.put<Payment>(`${BASE}/${id}/cancel`, null, { params: reason ? { reason } : undefined })
    },
    async refund(id: number, reason?: string): Promise<Payment> {
        return api.put<Payment>(`${BASE}/${id}/refund`, null, { params: reason ? { reason } : undefined })
    },

    // ── Reminder Operations ───────────────────────────────
    async createReminders(): Promise<void> {
        await api.post<void>(`${BASE}/reminders/create`)
    },
    async sendPendingReminders(): Promise<void> {
        await api.post<void>(`${BASE}/reminders/send`)
    },

    // ── Discount Calculations ───────────────────────────────
    async calculateEarlyDiscount(amount: number, paymentDate: string, dueDate: string): Promise<number> {
        return api.get<number>(`${BASE}/calculate-discount/early`, { params: { amount, paymentDate, dueDate } })
    },
    async calculateFinalDiscount(amount: number, isFinalInstallment: boolean): Promise<number> {
        return api.get<number>(`${BASE}/calculate-discount/final`, { params: { amount, isFinalInstallment } })
    },

    // ── Statistics ───────────────────────────────────────────
    async getMonthlyStatistics(year: number, month: number): Promise<PaymentStatistics> {
        return api.get<PaymentStatistics>(`${BASE}/statistics/monthly/${year}/${month}`)
    },
    async getYearToDateStatistics(year: number): Promise<PaymentStatistics> {
        return api.get<PaymentStatistics>(`${BASE}/statistics/ytd/${year}`)
    },
    async getOverdueStatistics(): Promise<PaymentStatistics> {
        return api.get<PaymentStatistics>(`${BASE}/statistics/overdue`)
    },
    async getDailySummaries(year: number, month: number): Promise<DailyPaymentSummary[]> {
        return api.get<DailyPaymentSummary[]>(`${BASE}/statistics/daily/${year}/${month}`)
    },

    // ── Discount Config ─────────────────────────────────────
    async getDiscountConfig(discountType: DiscountType): Promise<PaymentDiscountConfig> {
        return api.get<PaymentDiscountConfig>(`${BASE}/discount-config/${discountType}`)
    },
    async getAllDiscountConfigs(): Promise<PaymentDiscountConfig[]> {
        return api.get<PaymentDiscountConfig[]>(`${BASE}/discount-config`)
    },
    async saveDiscountConfig(data: PaymentDiscountConfigRequest): Promise<PaymentDiscountConfig> {
        return api.post<PaymentDiscountConfig>(`${BASE}/discount-config`, data)
    },
    async activateDiscountConfig(id: number): Promise<PaymentDiscountConfig> {
        return api.put<PaymentDiscountConfig>(`${BASE}/discount-config/${id}/activate`)
    },
    async deactivateDiscountConfig(discountType: DiscountType): Promise<void> {
        await api.del<void>(`${BASE}/discount-config/${discountType}`)
    },

    // ── Convenience helper for schedule-driven payment flow ─
    buildSchedulePaymentPayload(
        scheduleId: number,
        amount: number,
        overrides: Partial<PaymentRequest> = {},
    ): PaymentRequest {
        return {
            idempotencyKey: overrides.idempotencyKey ?? `PAY-SCH-${scheduleId}-${Date.now()}`,
            installmentScheduleId: scheduleId,
            amount,
            paymentMethod: overrides.paymentMethod ?? 'CASH',
            actualPaymentDate: overrides.actualPaymentDate ?? new Date().toISOString().split('T')[0],
            ...overrides,
        }
    },
} as const


