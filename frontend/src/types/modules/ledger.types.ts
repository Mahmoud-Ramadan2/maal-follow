import type { PaginationParams } from '@/types/common.types'

// ============================================================
// Ledger Module — TypeScript Types
//
// Maps to Spring Boot DTOs:
//   • LedgerRequest    → LedgerRequest
//   • LedgerResponse   → LedgerResponse
//   • LedgerSummary    → LedgerSummary
//   • LedgerStatistics → LedgerStatistics
//   • DailyLedgerSummary → DailyLedgerSummary
// ============================================================

export const LedgerType = {
    INCOME: 'INCOME',
    EXPENSE: 'EXPENSE',
} as const
export type LedgerType = (typeof LedgerType)[keyof typeof LedgerType]

export const LedgerSource = {
    COLLECTION: 'COLLECTION',
} as const
export type LedgerSource = (typeof LedgerSource)[keyof typeof LedgerSource]

export const LedgerReferenceType = {
    PAYMENT: 'PAYMENT',
} as const
export type LedgerReferenceType = (typeof LedgerReferenceType)[keyof typeof LedgerReferenceType]

export interface LedgerRequest {
    idempotencyKey: string
    type: LedgerType
    amount: number
    source: LedgerSource
    referenceType?: LedgerReferenceType
    referenceId?: number
    description?: string
    date: string
    partnerId?: number
}

export interface LedgerResponse {
    id: number
    idempotencyKey: string
    type: LedgerType
    amount: number
    source: LedgerSource
    referenceType: LedgerReferenceType | null
    referenceId: number | null
    description: string | null
    date: string
    createdAt: string
    userName: string | null
    userId: number | null
    partnerName: string | null
    partnerId: number | null
}

export interface LedgerSummary {
    id: number
    type: LedgerType
    amount: number
    source: LedgerSource
    description: string | null
    date: string
}

export interface LedgerStatistics {
    totalEntries: number
    totalIncome: number
    totalExpenses: number
    netBalance: number
    incomeEntries: number
    expenseEntries: number
    incomeThisMonth: number
    expensesThisMonth: number
    netBalanceThisMonth: number
}

export interface DailyLedgerSummary {
    date: string
    totalIncome: number
    totalExpense: number
    netAmount: number
    incomeCount: number
    expenseCount: number
}

export interface LedgerFilters extends PaginationParams {
    search?: string
    startDate?: string
    endDate?: string
    type?: LedgerType
    source?: LedgerSource
    partnerId?: number
}

