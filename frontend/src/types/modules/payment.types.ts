import type { PaginationParams } from '@/types/common.types'

// ============================================================
// Payment Module — TypeScript Types
//
// Maps to backend controllers/DTOs:
//   • PaymentController          → Payment, PaymentSummary, PaymentRequest
//   • PaymentStatisticsService   → PaymentStatistics, DailyPaymentSummary
//   • PaymentDiscountService     → PaymentDiscountConfig
// ============================================================

// ────────────────────────────────────────────────────────────
// Enums (const objects — erasableSyntaxOnly)
// ────────────────────────────────────────────────────────────

export const PaymentStatus = { PENDING: 'PENDING', COMPLETED: 'COMPLETED', FAILED: 'FAILED', REFUNDED: 'REFUNDED', CANCELLED: 'CANCELLED' } as const
export type PaymentStatus = (typeof PaymentStatus)[keyof typeof PaymentStatus]

export const PaymentMethod = { CASH: 'CASH', VODAFONE_CASH: 'VODAFONE_CASH', BANK_TRANSFER: 'BANK_TRANSFER', OTHER: 'OTHER' } as const
export type PaymentMethod = (typeof PaymentMethod)[keyof typeof PaymentMethod]

export const DiscountType = { EARLY_PAYMENT: 'EARLY_PAYMENT', FINAL_INSTALLMENT: 'FINAL_INSTALLMENT', BULK_PAYMENT: 'BULK_PAYMENT', LOYALTY_DISCOUNT: 'LOYALTY_DISCOUNT', MANUAL: 'MANUAL' } as const
export type DiscountType = (typeof DiscountType)[keyof typeof DiscountType]

export const ReminderMethod = { SMS: 'SMS', PHONE_CALL: 'PHONE_CALL', WHATSAPP: 'WHATSAPP', VISIT: 'VISIT' } as const
export type ReminderMethod = (typeof ReminderMethod)[keyof typeof ReminderMethod]

export const ReminderStatus = { PENDING: 'PENDING', SENT: 'SENT', ACKNOWLEDGED: 'ACKNOWLEDGED', COMPLETED: 'COMPLETED', CANCELLED: 'CANCELLED', FAILED: 'FAILED' } as const
export type ReminderStatus = (typeof ReminderStatus)[keyof typeof ReminderStatus]

// ────────────────────────────────────────────────────────────
// Payment (mirrors PaymentResponse DTO)
// ────────────────────────────────────────────────────────────

export interface Payment {
    id: number
    idempotencyKey: string
    installmentScheduleId: number | null
    amount: number
    paymentMethod: PaymentMethod
    status: PaymentStatus
    paymentDate: string
    actualPaymentDate: string
    agreedPaymentMonth: string | null
    isEarlyPayment: boolean
    discountAmount: number
    netAmount: number
    notes: string | null
    createdAt: string
    receivedByName: string | null
    receivedById: number | null
    collectorName: string | null
    collectorId: number | null
}

// ────────────────────────────────────────────────────────────
// PaymentSummary (list view — lighter than full Payment)
// ────────────────────────────────────────────────────────────

export interface PaymentSummary {
    id: number
    amount: number
    netAmount: number
    paymentMethod: PaymentMethod
    status: PaymentStatus
    actualPaymentDate: string
    agreedPaymentMonth: string | null
    isEarlyPayment: boolean
    createdAt: string
}

// ────────────────────────────────────────────────────────────
// PaymentRequest (mirrors PaymentRequest DTO)
// ────────────────────────────────────────────────────────────

export interface PaymentRequest {
    idempotencyKey: string
    installmentScheduleId?: number
    amount: number
    paymentMethod: PaymentMethod
    actualPaymentDate: string
    receiptDocumentId?: number
    status?: PaymentStatus
    extraExpenses?: number
    agreedPaymentMonth?: string
    isEarlyPayment?: boolean
    discountAmount?: number
    notes?: string
    collectorId?: number
}

// ────────────────────────────────────────────────────────────
// PaymentFilters
// ────────────────────────────────────────────────────────────

export interface PaymentFilters extends PaginationParams {
    month?: string          // YYYY-MM format
    startDate?: string
    endDate?: string
    earlyOnly?: boolean
    search?: string
}

export interface PaymentSearchFilters extends PaginationParams {
    month?: string
    startDate?: string
    endDate?: string
    isEarlyPayment?: boolean
    status?: PaymentStatus
    paymentMethod?: PaymentMethod
    collectorId?: number
    contractId?: number
    customerName?: string
    minNetAmount?: number
}

export interface PaymentReportSummary {
    month?: string | null
    startDate?: string | null
    endDate?: string | null
    totalCount: number
    completedCount: number
    cancelledCount: number
    refundedCount: number
    earlyPaymentCount: number
    totalAmount: number
    totalNetAmount: number
    totalDiscounts: number
}

// ────────────────────────────────────────────────────────────
// PaymentStatistics
// ────────────────────────────────────────────────────────────

export interface PaymentStatistics {
    month: string
    expectedPayments: number
    actualPayments: number
    collectionRate: number
    shortfall: number
    overduePaid: number
    earlyPayments: number
    totalDiscounts: number
    totalPaymentCount: number
    earlyPaymentCount: number
    overdueCount: number
}

// ────────────────────────────────────────────────────────────
// DailyPaymentSummary
// ────────────────────────────────────────────────────────────

export interface DailyPaymentSummary {
    paymentDate: string
    paymentCount: number
    totalAmount: number
    averagePayment: number
}

// ────────────────────────────────────────────────────────────
// PaymentDiscountConfig
// ────────────────────────────────────────────────────────────

export interface PaymentDiscountConfig {
    id: number
    discountType: DiscountType
    earlyPaymentDaysThreshold: number
    earlyPaymentDiscountPercentage: number
    finalInstallmentDiscountPercentage?: number
    minimumDiscountAmount: number
    maximumDiscountAmount: number
    isActive: boolean
    description: string | null
    createdAt: string
    createdByName: string | null
}

export interface PaymentDiscountConfigRequest {
    discountType: DiscountType
    earlyPaymentDaysThreshold?: number
    earlyPaymentDiscountPercentage?: number
    finalInstallmentDiscountPercentage?: number
    minimumDiscountAmount?: number
    maximumDiscountAmount?: number
    description?: string
}
