import { api } from '@services/api'
import type { PaginatedResponse } from '@/types/common.types'
import type {
    Payment, PaymentSummary, PaymentRequest, PaymentStatistics,
    DailyPaymentSummary, PaymentDiscountConfig,
    DiscountType,
} from '@/types/modules/payment.types'

const BASE = '/v1/payments'

export const paymentApi = {
    // ── CRUD ────────────────────────────────────────────────
    async create(data: PaymentRequest): Promise<Payment> {
        return api.post<Payment>(BASE, data)
    },
    async getById(id: number): Promise<Payment> {
        return api.get<Payment>(`${BASE}/${id}`)
    },
    async getAll(page = 0, size = 20): Promise<PaginatedResponse<PaymentSummary>> {
        return api.get<PaginatedResponse<PaymentSummary>>(BASE, { params: { page, size } })
    },
    async getByMonth(month: string, page = 0, size = 20): Promise<PaginatedResponse<PaymentSummary>> {
        return api.get<PaginatedResponse<PaymentSummary>>(`${BASE}/month/${month}`, { params: { page, size } })
    },
    async getByDateRange(startDate: string, endDate: string, page = 0, size = 20): Promise<PaginatedResponse<PaymentSummary>> {
        return api.get<PaginatedResponse<PaymentSummary>>(`${BASE}/date-range`, { params: { startDate, endDate, page, size } })
    },
    async getEarlyPayments(page = 0, size = 20): Promise<PaginatedResponse<PaymentSummary>> {
        return api.get<PaginatedResponse<PaymentSummary>>(`${BASE}/early-payments`, { params: { page, size } })
    },

    // ── Actions ─────────────────────────────────────────────
    async cancel(id: number, reason?: string): Promise<Payment> {
        return api.put<Payment>(`${BASE}/${id}/cancel`, null, { params: reason ? { reason } : undefined })
    },
    async refund(id: number, reason?: string): Promise<Payment> {
        return api.put<Payment>(`${BASE}/${id}/refund`, null, { params: reason ? { reason } : undefined })
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
    async activateDiscountConfig(id: number): Promise<PaymentDiscountConfig> {
        return api.put<PaymentDiscountConfig>(`${BASE}/discount-config/${id}/activate`)
    },
} as const


