import type { PaginationParams } from '@/types/common.types'

// ============================================================
// Partner Module — TypeScript Types
//
// Maps to 5 backend controllers:
//   • PartnerController           → Partner, PartnerRequest
//   • PartnerInvestmentController → PartnerInvestment
//   • PartnerWithdrawalController → PartnerWithdrawal
//   • PartnerCommissionController → PartnerCommission
//   • PartnerCustomerAcquisitionController → PartnerCustomerAcquisition
// ============================================================

// ────────────────────────────────────────────────────────────
// Enums (const objects — erasableSyntaxOnly)
// ────────────────────────────────────────────────────────────

export const PartnerStatus = { ACTIVE: 'ACTIVE', INACTIVE: 'INACTIVE' } as const
export type PartnerStatus = (typeof PartnerStatus)[keyof typeof PartnerStatus]

export const PartnershipType = { INVESTOR: 'INVESTOR', AFFILIATE: 'AFFILIATE', DISTRIBUTOR: 'DISTRIBUTOR', OTHER: 'OTHER' } as const
export type PartnershipType = (typeof PartnershipType)[keyof typeof PartnershipType]

export const InvestmentType = { INITIAL: 'INITIAL', ADDITIONAL: 'ADDITIONAL' } as const
export type InvestmentType = (typeof InvestmentType)[keyof typeof InvestmentType]

export const InvestmentStatus = { CONFIRMED: 'CONFIRMED', PENDING: 'PENDING', RETURNED: 'RETURNED' } as const
export type InvestmentStatus = (typeof InvestmentStatus)[keyof typeof InvestmentStatus]

export const WithdrawalType = { FROM_PRINCIPAL: 'FROM_PRINCIPAL', FROM_PROFIT: 'FROM_PROFIT', FROM_BOTH: 'FROM_BOTH' } as const
export type WithdrawalType = (typeof WithdrawalType)[keyof typeof WithdrawalType]

export const WithdrawalStatus = { PENDING: 'PENDING', APPROVED: 'APPROVED', COMPLETED: 'COMPLETED', CANCELLED: 'CANCELLED' } as const
export type WithdrawalStatus = (typeof WithdrawalStatus)[keyof typeof WithdrawalStatus]

export const CommissionType = { CUSTOMER_ACQUISITION: 'CUSTOMER_ACQUISITION', SALES_COMMISSION: 'SALES_COMMISSION', REFERRAL_BONUS: 'REFERRAL_BONUS', PERFORMANCE_BONUS: 'PERFORMANCE_BONUS' } as const
export type CommissionType = (typeof CommissionType)[keyof typeof CommissionType]

export const CommissionStatus = { PENDING: 'PENDING', PAID: 'PAID', CANCELLED: 'CANCELLED' } as const
export type CommissionStatus = (typeof CommissionStatus)[keyof typeof CommissionStatus]

export const CustomerAcquisitionStatus = { PENDING: 'PENDING', ACTIVE: 'ACTIVE', INACTIVE: 'INACTIVE', TRANSFERRED: 'TRANSFERRED', TERMINATED: 'TERMINATED' } as const
export type CustomerAcquisitionStatus = (typeof CustomerAcquisitionStatus)[keyof typeof CustomerAcquisitionStatus]

export const ProfitStatus = { CALCULATED: 'CALCULATED', PAID: 'PAID', DEFERRED: 'DEFERRED' } as const
export type ProfitStatus = (typeof ProfitStatus)[keyof typeof ProfitStatus]

// ────────────────────────────────────────────────────────────
// Partner (mirrors PartnerResponse DTO)
// ────────────────────────────────────────────────────────────

export interface Partner {
    id: number
    name: string
    phone: string
    address: string | null
    partnershipType: PartnershipType
    sharePercentage: number
    status: PartnerStatus
    investmentStartDate: string
    profitCalculationStartMonth: string | null
    totalInvestment: number
    totalWithdrawals: number
    currentBalance: number
    profitSharingActive: boolean
    notes: string | null
    createdAt: string
    updatedAt: string
    createdByName: string | null
}

export interface PartnerRequest {
    name: string
    phone: string
    address?: string
    partnershipType: PartnershipType
    sharePercentage?: number
    status?: PartnerStatus
    investmentStartDate: string
    profitCalculationStartMonth?: string
    totalInvestment?: number
    totalWithdrawals?: number
    currentBalance?: number
    profitSharingActive?: boolean
    notes?: string
    createdBy: number
}

export interface PartnerFilters extends PaginationParams {
    status?: PartnerStatus
    search?: string
}

// ────────────────────────────────────────────────────────────
// PartnerInvestment
// ────────────────────────────────────────────────────────────

export interface PartnerInvestment {
    id: number
    partnerName: string
    amount: number
    investmentType: InvestmentType
    status: InvestmentStatus
    investedAt: string
    returnedAt: string | null
    notes: string | null
}

export interface PartnerInvestmentRequest {
    partnerId: number
    amount: number
    status?: InvestmentStatus
    notes?: string
}

// ────────────────────────────────────────────────────────────
// PartnerWithdrawal
// ────────────────────────────────────────────────────────────

export interface PartnerWithdrawal {
    id: number
    partnerName: string
    amount: number
    principalAmount: number
    profitAmount: number
    withdrawalType: WithdrawalType
    status: WithdrawalStatus
    requestReason: string | null
    requestedAt: string
    approvedAt: string | null
    processedAt: string | null
    notes: string | null
    processedByName: string | null
    approvedByName: string | null
}

export interface PartnerWithdrawalRequest {
    partnerId: number
    amount: number
    withdrawalType: WithdrawalType
    requestReason?: string
    notes?: string
}

// ────────────────────────────────────────────────────────────
// PartnerCommission
// ────────────────────────────────────────────────────────────

export interface PartnerCommission {
    id: number
    partnerName: string
    commissionAmount: number
    commissionType: CommissionType
    status: CommissionStatus
    calculatedAt: string
    paidAt: string | null
    notes: string | null
    purchaseProductName: string | null
    contractCustomerName: string | null
}

export interface PartnerCommissionRequest {
    partnerId: number
    amount: number
    commissionType: CommissionType
    notes?: string
    purchaseId?: number
    contractId?: number
}

// ────────────────────────────────────────────────────────────
// PartnerCustomerAcquisition
// ────────────────────────────────────────────────────────────

export interface PartnerCustomerAcquisition {
    id: number
    partnerId: number
    partnerName: string
    customerId: number
    customerName: string
    customerPhone: string
    status: CustomerAcquisitionStatus
    commissionPercentage: number
    totalCommissionEarned: number
    acquisitionNotes: string | null
    acquiredAt: string
    deactivatedAt: string | null
}

export interface PartnerCustomerAcquisitionRequest {
    partnerId: number
    customerId: number
    commissionPercentage: number
    acquisitionNotes?: string
}

export interface PartnerPerformanceMetrics {
    [key: string]: unknown
}

