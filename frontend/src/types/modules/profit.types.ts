export const ProfitDistributionStatus = {
    PENDING: 'PENDING',
    CALCULATED: 'CALCULATED',
    DISTRIBUTED: 'DISTRIBUTED',
    LOCKED: 'LOCKED',
} as const

export type ProfitDistributionStatus =
    (typeof ProfitDistributionStatus)[keyof typeof ProfitDistributionStatus]

export interface MonthlyProfitDistribution {
    id: number
    monthYear: string
    totalProfit: number
    managementFeePercentage: number
    zakatPercentage: number
    managementFeeAmount: number
    zakatAmount: number
    contractExpensesAmount: number
    distributableProfit: number
    ownerProfit: number
    partnersTotalProfit: number
    status: ProfitDistributionStatus
    calculationNotes: string | null
    createdAt: string
    updatedAt: string
}

export interface MonthlyProfitDistributionRequest {
    monthYear: string
    totalProfit: number
    managementFeePercentage: number
    zakatPercentage: number
    calculationNotes?: string
}

export interface ProfitDistributionLifecycleStatus {
    distributionId: number
    monthYear: string
    status: ProfitDistributionStatus
}

export interface ProfitDistributionFilters {
    status?: ProfitDistributionStatus
    startMonth?: string
    endMonth?: string
}

