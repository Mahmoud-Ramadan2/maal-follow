import type { PaginatedResponse } from '@/types/common.types'
import type { CustomerStats } from '@/types/modules/customer.types'
import type { MonthlyCollectionSummary, InstallmentSchedule } from '@/types/modules/schedule.types'
import type { PaymentStatistics, DailyPaymentSummary } from '@/types/modules/payment.types'
import type { PurchaseStatistics } from '@/types/modules/purchase.types'
import type { CapitalPool, CapitalMonthlySummary } from '@/types/modules/capital.types'
import type { PartnerCommissionSummary, PartnerProfitConfig } from '@/types/modules/partner.types'
import type { ProfitDistributionLifecycleStatus } from '@/types/modules/profit.types'
import type { LedgerStatistics, DailyLedgerSummary } from '@/types/modules/ledger.types'

export interface DashboardOverviewParams {
    year: number
    month: number
    daysAhead?: number
    startDate?: string
    endDate?: string
    customerId?: number
    partnerId?: number
    profitDistributionId?: number
}

export interface DashboardResolvedParams extends Required<Pick<DashboardOverviewParams, 'year' | 'month' | 'daysAhead'>> {
    monthKey: string
    startDate?: string
    endDate?: string
    customerId?: number
    partnerId?: number
    profitDistributionId?: number
}

export interface DashboardFinanceSnapshot {
    customerStats: CustomerStats
    monthlyExpectedContractTotal: number
    netProfitTotal: number
    paymentStatistics: PaymentStatistics
    ledgerStatistics: LedgerStatistics
    purchaseStatistics: PurchaseStatistics
    currentCapitalPool: CapitalPool | null
    monthlyCapitalSummary: CapitalMonthlySummary
    partnerProfitConfig: PartnerProfitConfig | null
}

export interface DashboardTrendSnapshot {
    monthlyCollectionSummary: MonthlyCollectionSummary
    dailyPaymentSummaries: DailyPaymentSummary[]
    dailyLedgerSummaries: DailyLedgerSummary[]
}

export interface DashboardOperationsSnapshot {
    overdueSchedules: InstallmentSchedule[]
    dueSoonSchedules: InstallmentSchedule[]
    overduePaymentStatistics: PaymentStatistics
}

export interface DashboardOptionalSnapshot {
    ytdPaymentStatistics?: PaymentStatistics
    unreadNotificationCount?: number
    partnerCommissionSummary?: PartnerCommissionSummary
    profitDistributionStatus?: ProfitDistributionLifecycleStatus
    capitalHistory?: PaginatedResponse<CapitalPool>
}

export interface DashboardOverview {
    generatedAt: string
    params: DashboardResolvedParams
    finance: DashboardFinanceSnapshot
    trends: DashboardTrendSnapshot
    operations: DashboardOperationsSnapshot
    optional: DashboardOptionalSnapshot
    errors?: string[]
}
