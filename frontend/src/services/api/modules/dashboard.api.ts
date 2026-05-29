import { api } from '@services/api'
import { compactParams } from '@services/api/shared/compactParams'
import { customerApi } from './customer.api'
import { contractApi } from './contract.api'
import { scheduleApi } from './schedule.api'
import { paymentApi } from './payment.api'
import { purchaseApi } from './purchase.api'
import { capitalPoolApi, capitalTransactionApi } from './capital.api'
import { ledgerApi } from './ledger.api'
import { partnerProfitControlApi } from './partnerProfitControl.api'
import { partnerCommissionApi } from './partnerCommission.api'
import { profitApi } from './profit.api'
import type {
    DashboardFinanceSnapshot,
    DashboardOperationsSnapshot,
    DashboardOverview,
    DashboardOverviewParams,
    DashboardResolvedParams,
    DashboardTrendSnapshot,
} from '@/types/modules/dashboard.types'
import type { CapitalMonthlySummary } from '@/types/modules/capital.types'
import type { DailyPaymentSummary, PaymentStatistics } from '@/types/modules/payment.types'
import type { DailyLedgerSummary, LedgerStatistics } from '@/types/modules/ledger.types'
import type { PartnerCommissionSummary, PartnerProfitConfig } from '@/types/modules/partner.types'
import type { ProfitDistributionLifecycleStatus } from '@/types/modules/profit.types'
import type { InstallmentSchedule, MonthlyCollectionSummary } from '@/types/modules/schedule.types'
import type { CustomerStats } from '@/types/modules/customer.types'
import type { PurchaseStatistics } from '@/types/modules/purchase.types'
import type { CapitalPool } from '@/types/modules/capital.types'

const DAYS_AHEAD_DEFAULT = 5

type ErrorSeverity = 'critical' | 'secondary'
interface CategorizedErrors {
    critical: string[]
    secondary: string[]
}

function safeFetch<T>(
    label: string,
    errors: CategorizedErrors,
    fn: () => Promise<T>,
    fallback: () => T,
    severity: ErrorSeverity = 'secondary',
): Promise<T> {
    return fn().catch((err) => {
        console.error(`[Dashboard] "${label}" failed:`, err)
        errors[severity].push(label)
        return fallback()
    })
}

const buildMonthKey = (year: number, month: number): string => {
    const safeMonth = Math.min(Math.max(month, 1), 12)
    return `${year}-${String(safeMonth).padStart(2, '0')}`
}

const getMonthBounds = (year: number, month: number): { startDate: string; endDate: string } => {
    const safeMonth = Math.min(Math.max(month, 1), 12)
    const startDate = new Date(Date.UTC(year, safeMonth - 1, 1)).toISOString().slice(0, 10)
    const endDate = new Date(Date.UTC(year, safeMonth, 0)).toISOString().slice(0, 10)
    return { startDate, endDate }
}

const resolveParams = (params: DashboardOverviewParams): DashboardResolvedParams => ({
    year: params.year,
    month: params.month,
    daysAhead: params.daysAhead ?? DAYS_AHEAD_DEFAULT,
    monthKey: buildMonthKey(params.year, params.month),
    startDate: params.startDate,
    endDate: params.endDate,
    customerId: params.customerId,
    partnerId: params.partnerId,
    profitDistributionId: params.profitDistributionId,
})

const buildOverviewParams = (params: DashboardOverviewParams) => {
    const resolved = resolveParams(params)
    return {
        resolved,
        monthKey: resolved.monthKey,
        daysAhead: resolved.daysAhead,
    }
}

const fallbackCollectionSummary = (): MonthlyCollectionSummary => ({
    month: '',
    expectedAmount: 0,
    actualAmount: 0,
    shortfall: 0,
})

const fallbackPaymentStats = (): PaymentStatistics => ({
    month: '',
    expectedPayments: 0,
    actualPayments: 0,
    collectionRate: 0,
    shortfall: 0,
    overduePaid: 0,
    earlyPayments: 0,
    totalDiscounts: 0,
    totalPaymentCount: 0,
    earlyPaymentCount: 0,
    overdueCount: 0,
})

const fallbackLedgerStats = (): LedgerStatistics => ({
    totalEntries: 0,
    totalIncome: 0,
    totalExpenses: 0,
    netBalance: 0,
    incomeEntries: 0,
    expenseEntries: 0,
    incomeThisMonth: 0,
    expensesThisMonth: 0,
    netBalanceThisMonth: 0,
})


// TODO(backend): Introduce a dedicated backend aggregate endpoint
// (/dashboard/overview) that returns all critical + secondary data in a
// single response. The current approach composes the overview from 18+
// individual API calls, which is convenient for iteration but expensive
// and introduces N+1 latency on every dashboard load.
export const dashboardApi = {
    async getCustomerStats() {
        return customerApi.getStats()
    },
    async getContractTotals(): Promise<{ monthlyExpectedContractTotal: number; netProfitTotal: number }> {
        const [monthlyExpectedContractTotal, netProfitTotal] = await Promise.all([
            contractApi.getTotalMonthlyExpected(),
            contractApi.getTotalNetProfit(),
        ])

        return { monthlyExpectedContractTotal, netProfitTotal }
    },
    async getMonthlyCollectionSummary(monthKey: string): Promise<MonthlyCollectionSummary> {
        return scheduleApi.getMonthlySummary(monthKey)
    },
    async getOverdueSchedules(): Promise<InstallmentSchedule[]> {
        return scheduleApi.getOverdue()
    },
    async getDueSoonSchedules(daysAhead = DAYS_AHEAD_DEFAULT): Promise<InstallmentSchedule[]> {
        return scheduleApi.getDueSoon(daysAhead)
    },
    async getPaymentStatistics(year: number, month: number): Promise<PaymentStatistics> {
        return paymentApi.getMonthlyStatistics(year, month)
    },
    async getYearToDatePaymentStatistics(year: number): Promise<PaymentStatistics> {
        return paymentApi.getYearToDateStatistics(year)
    },
    async getOverduePaymentStatistics(): Promise<PaymentStatistics> {
        return paymentApi.getOverdueStatistics()
    },
    async getDailyPaymentSummaries(year: number, month: number): Promise<DailyPaymentSummary[]> {
        return paymentApi.getDailySummaries(year, month)
    },
    async getLedgerStatistics(): Promise<LedgerStatistics> {
        return ledgerApi.getStatistics()
    },
    async getDailyLedgerSummaries(startDate: string, endDate: string): Promise<DailyLedgerSummary[]> {
        return ledgerApi.getDailySummary(startDate, endDate)
    },
    async getPurchaseStatistics(): Promise<PurchaseStatistics> {
        return purchaseApi.getStatistics()
    },
    async getCurrentCapitalPool(): Promise<CapitalPool | null> {
        return capitalPoolApi.getCurrent()
    },
    async getMonthlyCapitalSummary(year: number, month: number): Promise<CapitalMonthlySummary> {
        return capitalTransactionApi.getMonthlySummary(year, month)
    },
    async getPartnerProfitConfig(): Promise<PartnerProfitConfig | null> {
        return partnerProfitControlApi.getCurrentConfig()
    },
    async getPartnerCommissionSummary(partnerId: number): Promise<PartnerCommissionSummary> {
        return partnerCommissionApi.getSummary(partnerId)
    },
    async getProfitDistributionStatus(profitDistributionId: number): Promise<ProfitDistributionLifecycleStatus> {
        return profitApi.getStatus(profitDistributionId)
    },
    async getUnreadNotificationCount(customerId: number): Promise<number> {
        return api.get<number>(`/notifications/customer/${customerId}/unread-count`)
    },

    async getOverview(
        params: DashboardOverviewParams,
        options?: { criticalOnly?: boolean },
    ): Promise<DashboardOverview> {
        const { resolved, monthKey, daysAhead } = buildOverviewParams(params)
        const { startDate, endDate } = params.startDate && params.endDate
            ? { startDate: params.startDate, endDate: params.endDate }
            : getMonthBounds(resolved.year, resolved.month)

        const errors: CategorizedErrors = { critical: [], secondary: [] }
        const criticalOnly = options?.criticalOnly === true

        // ── Phase 1: Critical data (KPIs, schedules) — always fetched ──
        const [
            customerStats,
            contractTotals,
            monthlyCollectionSummary,
            overdueSchedules,
            dueSoonSchedules,
            paymentStatistics,
            currentCapitalPool,
        ] = await Promise.all([
            safeFetch('customerStats', errors, () => dashboardApi.getCustomerStats(), () => ({} as CustomerStats), 'critical'),
            safeFetch('contractTotals', errors, () => dashboardApi.getContractTotals(), () => ({ monthlyExpectedContractTotal: 0, netProfitTotal: 0 }), 'critical'),
            safeFetch('monthlyCollectionSummary', errors, () => dashboardApi.getMonthlyCollectionSummary(monthKey), fallbackCollectionSummary, 'critical'),
            safeFetch('overdueSchedules', errors, () => dashboardApi.getOverdueSchedules(), () => [], 'critical'),
            safeFetch('dueSoonSchedules', errors, () => dashboardApi.getDueSoonSchedules(daysAhead), () => [], 'critical'),
            safeFetch('paymentStatistics', errors, () => dashboardApi.getPaymentStatistics(resolved.year, resolved.month), fallbackPaymentStats, 'critical'),
            safeFetch('currentCapitalPool', errors, () => dashboardApi.getCurrentCapitalPool(), () => null, 'critical'),
        ])

        // ── Secondary data (charts, partner, etc.) — skipped for critical-only ──
        let ytdPaymentStatistics: PaymentStatistics | undefined
        let overduePaymentStatistics: PaymentStatistics | undefined
        let dailyPaymentSummaries: DailyPaymentSummary[] | undefined
        let ledgerStatistics: LedgerStatistics | undefined
        let dailyLedgerSummaries: DailyLedgerSummary[] | undefined
        let purchaseStatistics: PurchaseStatistics | undefined
        let monthlyCapitalSummary: CapitalMonthlySummary | undefined
        let partnerProfitConfig: PartnerProfitConfig | null | undefined
        let partnerCommissionSummary: PartnerCommissionSummary | undefined
        let profitDistributionStatus: ProfitDistributionLifecycleStatus | undefined
        let unreadNotificationCount: number | undefined

        if (!criticalOnly) {
            [
                ytdPaymentStatistics,
                overduePaymentStatistics,
                dailyPaymentSummaries,
                ledgerStatistics,
                dailyLedgerSummaries,
                purchaseStatistics,
                monthlyCapitalSummary,
                partnerProfitConfig,
                partnerCommissionSummary,
                profitDistributionStatus,
                unreadNotificationCount,
            ] = await Promise.all([
                safeFetch('ytdPaymentStatistics', errors, () => dashboardApi.getYearToDatePaymentStatistics(resolved.year), fallbackPaymentStats),
                safeFetch('overduePaymentStatistics', errors, () => dashboardApi.getOverduePaymentStatistics(), fallbackPaymentStats),
                safeFetch('dailyPaymentSummaries', errors, () => dashboardApi.getDailyPaymentSummaries(resolved.year, resolved.month), () => []),
                safeFetch('ledgerStatistics', errors, () => dashboardApi.getLedgerStatistics(), fallbackLedgerStats),
                safeFetch('dailyLedgerSummaries', errors, () => dashboardApi.getDailyLedgerSummaries(startDate, endDate), () => []),
                safeFetch('purchaseStatistics', errors, () => dashboardApi.getPurchaseStatistics(), () => ({ totalCount: 0, totalAmount: 0, avgAmount: 0, countByVendor: {}, amountByVendor: {} })),
                safeFetch('monthlyCapitalSummary', errors, () => dashboardApi.getMonthlyCapitalSummary(resolved.year, resolved.month), () => ({})),
                safeFetch('partnerProfitConfig', errors, () => dashboardApi.getPartnerProfitConfig(), () => null),
                safeFetch('partnerCommissionSummary', errors, () => resolved.partnerId ? dashboardApi.getPartnerCommissionSummary(resolved.partnerId) : Promise.resolve(undefined), () => undefined),
                safeFetch('profitDistributionStatus', errors, () => resolved.profitDistributionId ? dashboardApi.getProfitDistributionStatus(resolved.profitDistributionId) : Promise.resolve(undefined), () => undefined),
                safeFetch('unreadNotificationCount', errors, () => resolved.customerId ? dashboardApi.getUnreadNotificationCount(resolved.customerId) : Promise.resolve(0), () => 0),
            ])
        }

        const fallbackPurchaseStats = (): PurchaseStatistics => ({ totalCount: 0, totalAmount: 0, avgAmount: 0, countByVendor: {}, amountByVendor: {} })

        const finance: DashboardFinanceSnapshot = {
            customerStats: customerStats ?? {},
            monthlyExpectedContractTotal: contractTotals?.monthlyExpectedContractTotal ?? 0,
            netProfitTotal: contractTotals?.netProfitTotal ?? 0,
            paymentStatistics: paymentStatistics ?? fallbackPaymentStats(),
            ledgerStatistics: ledgerStatistics ?? fallbackLedgerStats(),
            purchaseStatistics: purchaseStatistics ?? fallbackPurchaseStats(),
            currentCapitalPool: currentCapitalPool ?? null,
            monthlyCapitalSummary: monthlyCapitalSummary ?? {},
            partnerProfitConfig: partnerProfitConfig ?? null,
        }

        const trends: DashboardTrendSnapshot = {
            monthlyCollectionSummary: monthlyCollectionSummary ?? fallbackCollectionSummary(),
            dailyPaymentSummaries: dailyPaymentSummaries ?? [],
            dailyLedgerSummaries: dailyLedgerSummaries ?? [],
        }

        const operations: DashboardOperationsSnapshot = {
            overdueSchedules: overdueSchedules ?? [],
            dueSoonSchedules: dueSoonSchedules ?? [],
            overduePaymentStatistics: overduePaymentStatistics ?? fallbackPaymentStats(),
        }

        return {
            generatedAt: new Date().toISOString(),
            params: resolved,
            finance,
            trends,
            operations,
            optional: compactParams({
                ytdPaymentStatistics,
                unreadNotificationCount,
                partnerCommissionSummary,
                profitDistributionStatus,
            }),
            errors: [...errors.critical, ...errors.secondary].length > 0
                ? [...errors.critical, ...errors.secondary]
                : undefined,
        }
    },
} as const
