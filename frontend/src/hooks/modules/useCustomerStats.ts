import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { customerApi } from '@services/api/modules/customer.api'
import type { CustomerStats } from '@/types/modules/customer.types'

interface UseCustomerStatsReturn {
    /** Stats map — keys are localized labels, values are counts */
    stats: CustomerStats | null
    /** `true` while the API request is in flight */
    loading: boolean
    /** Error message if the fetch failed */
    error: string | null
    /** Re-fetch stats */
    refetch: () => void
}

/**
 * Fetches customer count statistics (active / inactive).
 *
 * Maps to `GET /api/v1/customers/stats/count`.
 *
 * @example
 * ```tsx
 * const { stats, loading } = useCustomerStats()
 * // stats = { "Active Customers": 42, "Inactive Customers": 5,  }
 * ```
 */
export function useCustomerStats(): UseCustomerStatsReturn {
    const { t } = useTranslation('customer')

    const [stats, setStats] = useState<CustomerStats | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetchStats = useCallback(async () => {
        setLoading(true)
        setError(null)

        try {
            const data = await customerApi.getStats()
            setStats(data)
        } catch (err) {
            const message = err instanceof Error ? err.message : t('fetchError')
            setError(message)
            toast.error(t('stats.fetchError'))
        } finally {
            setLoading(false)
        }
    }, [t])

    useEffect(() => {
        fetchStats()
    }, [fetchStats])

    return { stats, loading, error, refetch: fetchStats }
}

