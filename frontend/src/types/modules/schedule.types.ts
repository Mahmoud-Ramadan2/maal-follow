import type { PaginationParams } from '@/types/common.types'

// ============================================================
// Schedule Module — TypeScript Types
//
// Maps to Spring Boot DTOs:
//   • InstallmentScheduleResponse  -> InstallmentSchedule
//   • InstallmentScheduleRequest   -> InstallmentScheduleRequest
//   • ScheduleParameters           -> ScheduleParameters
//   • MonthlyCollectionSummary     -> MonthlyCollectionSummary
// ============================================================

/** Payment status enum used by installment schedules. */
export const PaymentStatus = {
    PENDING: 'PENDING',
    PAID: 'PAID',
    LATE: 'LATE',
    PARTIALLY_PAID: 'PARTIALLY_PAID',
    CANCELLED: 'CANCELLED',
} as const

/** Union type of all supported payment statuses. */
export type PaymentStatus = (typeof PaymentStatus)[keyof typeof PaymentStatus]

/**
 * Installment schedule row returned by the backend.
 *
 * Dates are ISO strings (`YYYY-MM-DD`).
 * Monetary values are numbers (BigDecimal -> number).
 */
export interface InstallmentSchedule {
    id?: number
    contractId: number
    sequenceNumber: number
    customerName: string
    customerPhone: string
    profitMonth: string
    isFinalPayment: boolean
    originalAmount: number
    amount: number
    principalAmount: number
    profitAmount: number
    discountApplied: number
    paidAmount: number
    principalPaid: number
    profitPaid: number
    status: PaymentStatus
    dueDate: string
    paidDate: string | null
    collectorId?: number | null
    collectorName: string | null
    collectorRole?: string | null
    notes: string | null
    createdAt?: string
    updatedAt?: string
}

/** Request payload for creating/updating a single installment schedule. */
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

/** Request payload for safe non-financial schedule updates. */
export interface ScheduleMetadataUpdateRequest {
    dueDate?: string
    notes?: string
    collectorId?: number
    clearCollector?: boolean
}

/** Custom generation parameters for bulk schedule generation. */
export interface GenerateScheduleParams {
    numberOfMonths?: number
    monthlyAmount?: number
    putRemainderFirst?: boolean
}

/** Parameters used by the backend during internal schedule calculations. */
export interface ScheduleParameters {
    months: number
    monthlyAmount: number
}

/** Parameters for rescheduling unpaid installments. */
export interface RescheduleParams {
    newNumberOfMonths?: number
    newMonthlyAmount?: number
    newStartDate?: string
}

/** Monthly aggregate returned by `/installment-schedules/monthly-summary`. */
export interface MonthlyCollectionSummary {
    month: string
    expectedAmount: number
    actualAmount: number
    shortfall: number
}

/**
 * Query filters for schedule list endpoints.
 *
 * - `contractId` for contract-scoped listing
 * - `status` for status-scoped listing
 * - `name` for customer name search endpoint
 */
export interface ScheduleFilters extends PaginationParams {
    contractId?: number
    status?: PaymentStatus
    name?: string
    paymentDay?: number
    overdueOnly?: boolean
    dueSoonDays?: number
    startDate?: string
    endDate?: string
}

/**
 * Canonical keys for all schedule-related endpoints and roadmap items.
 */
export const ScheduleEndpointKey = {
    GENERATE: 'GENERATE',
    GENERATE_CUSTOM: 'GENERATE_CUSTOM',
    SWAP_REMAINDER: 'SWAP_REMAINDER',
    DELETE_UNPAID: 'DELETE_UNPAID',
    RESCHEDULE: 'RESCHEDULE',
    SKIP_MONTH: 'SKIP_MONTH',
    CREATE: 'CREATE',
    UPDATE: 'UPDATE',
    UPDATE_METADATA: 'UPDATE_METADATA',
    GET_BY_CONTRACT: 'GET_BY_CONTRACT',
    GET_OVERDUE: 'GET_OVERDUE',
    GET_DUE_SOON: 'GET_DUE_SOON',
    GET_BY_PAYMENT_DAY: 'GET_BY_PAYMENT_DAY',
    GET_BY_NAME: 'GET_BY_NAME',
    GET_BY_STATUS: 'GET_BY_STATUS',
    GET_MONTHLY_SUMMARY: 'GET_MONTHLY_SUMMARY',
    GET_BY_ID: 'GET_BY_ID',
    // Recommended additions
    SEARCH_PAGINATED: 'SEARCH_PAGINATED',
    PAY_INSTALLMENT: 'PAY_INSTALLMENT',
    REMIND_BY_SCHEDULE: 'REMIND_BY_SCHEDULE',
} as const

export type ScheduleEndpointKey = (typeof ScheduleEndpointKey)[keyof typeof ScheduleEndpointKey]

/** Endpoint registry entry used by UI/docs to mark available vs roadmap APIs. */
export interface ScheduleEndpointDefinition {
    key: ScheduleEndpointKey
    method: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
    path: string
    available: boolean
    description: string
}

