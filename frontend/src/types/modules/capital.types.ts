import type { PaginationParams } from '@/types/common.types'

// ============================================================
// Capital Module — TypeScript Types
//
// Maps to 2 backend controllers:
//   • CapitalPoolController        → CapitalPool, CapitalPoolRequest
//   • CapitalTransactionController → CapitalTransaction, CapitalTransactionRequest
// ============================================================

// ────────────────────────────────────────────────────────────
// Enums (const objects — erasableSyntaxOnly)
// ────────────────────────────────────────────────────────────

export const CapitalTransactionType = {
    INVESTMENT: 'INVESTMENT',
    WITHDRAWAL: 'WITHDRAWAL',
    ALLOCATION: 'ALLOCATION',
    RETURN: 'RETURN',
    MANUAL: 'MANUAL',
} as const
export type CapitalTransactionType = (typeof CapitalTransactionType)[keyof typeof CapitalTransactionType]

export const CapitalSourceType = {
    PARTNER: 'PARTNER',
    OWNER: 'OWNER',
    SYSTEM: 'SYSTEM',
} as const
export type CapitalSourceType = (typeof CapitalSourceType)[keyof typeof CapitalSourceType]

export const CapitalReferenceType = {
    PAYMENT: 'PAYMENT',
    CONTRACT: 'CONTRACT',
    PARTNER_CONTRIBUTION: 'PARTNER_CONTRIBUTION',
    MANUAL: 'MANUAL',
} as const
export type CapitalReferenceType = (typeof CapitalReferenceType)[keyof typeof CapitalReferenceType]

// ────────────────────────────────────────────────────────────
// CapitalPool (mirrors CapitalPoolResponse DTO)
// ────────────────────────────────────────────────────────────

export interface CapitalPool {
    id: number
    totalAmount: number
    availableAmount: number
    lockedAmount: number
    returnedAmount: number
    ownerContribution: number
    partnerContributions: number
    description: string | null
    createdAt: string
    updatedAt: string
    /** Owner's share of the pool as a percentage */
    ownerSharePercentage: number
    /** Partners' share of the pool as a percentage */
    partnerSharePercentage: number
    /** locked / total — how much capital is currently in use */
    utilizationPercentage: number
}

export interface CapitalPoolRequest {
    totalAmount: number
    ownerContribution: number
    partnerContributions: number
    description?: string
}

// ────────────────────────────────────────────────────────────
// CapitalTransaction (mirrors CapitalTransactionResponse DTO)
// ────────────────────────────────────────────────────────────

export interface CapitalTransaction {
    id: number
    capitalPoolId: number
    transactionType: CapitalTransactionType
    amount: number
    availableBefore: number
    availableAfter: number
    lockedBefore: number
    lockedAfter: number
    referenceType: string | null
    referenceId: number | null
    contractId: number | null
    paymentId: number | null
    partnerId: number | null
    partnerName: string | null
    description: string | null
    transactionDate: string
    createdAt: string
    createdByUsername: string | null
}

export interface CapitalTransactionRequest {
    transactionType: CapitalTransactionType
    amount: number
    partnerId?: number
    contractId?: number
    description?: string
}

// ────────────────────────────────────────────────────────────
// Filters
// ────────────────────────────────────────────────────────────

export interface CapitalTransactionFilters extends PaginationParams {
    transactionType?: CapitalTransactionType
    partnerId?: number
    startDate?: string
    endDate?: string
}

// ────────────────────────────────────────────────────────────
// MonthlySummary (returned as Map<String, BigDecimal>)
// ────────────────────────────────────────────────────────────

export interface CapitalMonthlySummary {
    [key: string]: number
}

