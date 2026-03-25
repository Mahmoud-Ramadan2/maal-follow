import type { PaginationParams } from '@/types/common.types'

// ============================================================
// Contract Module — TypeScript Types
//
// Maps to Spring Boot DTOs & Enums:
//   • ContractResponse       →  Contract
//   • ContractRequest        →  ContractRequest
//   • ContractExpenseResponse → ContractExpense
//   • ContractExpenseRequest  → ContractExpenseRequest
//   • InstallmentScheduleResponse → InstallmentSchedule
//   • InstallmentScheduleRequest  → InstallmentScheduleRequest
//   • Enums: ContractStatus, PaymentStatus, ExpenseType, PaidBy, DeductionType
// ============================================================

// ────────────────────────────────────────────────────────────
// Enums (const objects — erasableSyntaxOnly is enabled)
// ────────────────────────────────────────────────────────────

export const ContractStatus = {
    ACTIVE: 'ACTIVE',
    COMPLETED: 'COMPLETED',
    LATE: 'LATE',
    CANCELLED: 'CANCELLED',
    ALL: 'ALL',
} as const
export type ContractStatus =
    (typeof ContractStatus)[keyof typeof ContractStatus]

export const PaymentStatus = {
    PENDING: 'PENDING',
    PAID: 'PAID',
    LATE: 'LATE',
    PARTIALLY_PAID: 'PARTIALLY_PAID',
    CANCELLED: 'CANCELLED',
} as const
export type PaymentStatus = (typeof PaymentStatus)[keyof typeof PaymentStatus]

export const ExpenseType = {
    SHIPPING: 'SHIPPING',
    INSURANCE: 'INSURANCE',
    MAINTENANCE: 'MAINTENANCE',
    TAX: 'TAX',
    INSTALLMENT: 'INSTALLMENT',
    OTHER: 'OTHER',
} as const
export type ExpenseType = (typeof ExpenseType)[keyof typeof ExpenseType]

export const PaidBy = {
    OWNER: 'OWNER',
    PARTNER: 'PARTNER',
    CUSTOMER: 'CUSTOMER',
} as const
export type PaidBy = (typeof PaidBy)[keyof typeof PaidBy]

export const DeductionType = {
    MANAGEMENT_FEE: 'MANAGEMENT_FEE',
    ZAKAT: 'ZAKAT',
    TAX: 'TAX',
    COMMISSION: 'COMMISSION',
    OTHER: 'OTHER',
} as const
export type DeductionType = (typeof DeductionType)[keyof typeof DeductionType]

// ────────────────────────────────────────────────────────────
// Contract (mirrors ContractResponse DTO)
// ────────────────────────────────────────────────────────────

export interface Contract {
    // Identification
    id: number
    contractNumber: string
    status: ContractStatus

    // Foreign keys (may be omitted by some backend responses)
    customerId?: number
    purchaseId?: number
    partnerId?: number | null
    responsibleUserId?: number | null

    // Customer / User
    customerName: string
    responsibleUserName: string | null

    // Product / Supplier
    productName: string
    vendorName: string

    // Partner
    partnerName: string | null

    // Financial — Base
    originalPrice: number
    additionalCosts: number
    finalPrice: number
    downPayment: number
    remainingAmount: number

    // Financial — Discounts
    cashDiscountRate: number
    earlyPaymentDiscountRate: number

    // Payment Schedule
    months: number
    monthlyAmount: number
    agreedPaymentDay: number

    // Profit
    profitAmount: number
    totalExpenses: number
    netProfit: number

    // Dates
    startDate: string
    completionDate: string | null
    createdAt: string
    updatedAt: string

    // Notes
    notes: string | null
}

// ────────────────────────────────────────────────────────────
// ContractRequest (mirrors ContractRequest DTO)
// ────────────────────────────────────────────────────────────

export interface ContractRequest {
    finalPrice: number
    downPayment: number
    months?: number
    monthlyAmount?: number
    startDate: string
    status?: ContractStatus
    notes?: string
    customerId: number
    purchaseId: number
    additionalCosts?: number
    earlyPaymentDiscountRate?: number
    agreedPaymentDay?: number
    partnerId?: number
    contractNumber?: string
    responsibleUserId?: number
}

// ────────────────────────────────────────────────────────────
// ContractFilters
// ────────────────────────────────────────────────────────────

export interface ContractFilters extends PaginationParams {
    status?: ContractStatus
    search?: string
    customerId?: number
}

// ────────────────────────────────────────────────────────────
// ContractExpense (mirrors ContractExpenseResponse DTO)
// ────────────────────────────────────────────────────────────

export interface ContractExpense {
    contractNumber: string
    scheduleId: number | null
    expenseType: ExpenseType
    amount: number
    description: string | null
    expenseDate: string
    paidBy: PaidBy
    partnerName: string | null
    receiptNumber: string | null
    notes: string | null
    createdAt: string
    createdByName: string | null
}

// ────────────────────────────────────────────────────────────
// ContractExpenseRequest (mirrors ContractExpenseRequest DTO)
// ────────────────────────────────────────────────────────────

export interface ContractExpenseRequest {
    contractId?: number
    scheduleId?: number
    expenseType: ExpenseType
    amount: number
    description?: string
    expenseDate: string
    paidBy?: PaidBy
    partnerId?: number
    receiptNumber?: string
    notes?: string
}

// ────────────────────────────────────────────────────────────
// InstallmentSchedule (mirrors InstallmentScheduleResponse DTO)
// ────────────────────────────────────────────────────────────

export interface InstallmentSchedule {
    contractNumber: string
    sequenceNumber: number
    customerName: string
    customerPhone: string
    profitMonth: string
    isFinalPayment: boolean
    originalAmount: number
    principalAmount: number
    profitAmount: number
    dueDate: string
    amount: number
    status: PaymentStatus
    paidAmount: number
    paidDate: string | null
    collectorName: string | null
    notes: string | null
}

// ────────────────────────────────────────────────────────────
// InstallmentScheduleRequest (mirrors DTO)
// ────────────────────────────────────────────────────────────

export interface InstallmentScheduleRequest {
    contractId: number
    sequenceNumber: number
    profitMonth: string
    discountApplied?: number
    isFinalPayment?: boolean
    amount: number
    dueDate: string
    status?: PaymentStatus
    notes?: string
    collectorId?: number
}

// ────────────────────────────────────────────────────────────
// GenerateScheduleParams — for custom generation
// ────────────────────────────────────────────────────────────

export interface GenerateScheduleParams {
    numberOfMonths?: number
    monthlyAmount?: number
    putRemainderFirst?: boolean
}

// ────────────────────────────────────────────────────────────
// RescheduleParams
// ────────────────────────────────────────────────────────────

export interface RescheduleParams {
    newNumberOfMonths?: number
    newMonthlyAmount?: number
    newStartDate?: string
}

// ────────────────────────────────────────────────────────────
// MonthlyCollectionSummary (from backend service)
// ────────────────────────────────────────────────────────────

export interface MonthlyCollectionSummary {
    [key: string]: unknown
}

