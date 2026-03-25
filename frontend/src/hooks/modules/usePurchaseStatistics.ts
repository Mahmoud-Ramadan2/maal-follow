import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { purchaseApi } from '@services/api/modules/purchase.api'
import type { PurchaseStatistics } from '@/types/modules/purchase.types'

interface UsePurchaseStatisticsReturn {
    statistics: PurchaseStatistics | null
    loading: boolean
    error: string | null
    refetch: () => void
}

/**
 * Fetches aggregate purchase statistics from GET /purchases/statistics.
 *
 * @example
 * ```tsx
 * const { statistics, loading } = usePurchaseStatistics()
 * ```
 */
export function usePurchaseStatistics(): UsePurchaseStatisticsReturn {
    const { t } = useTranslation('purchase')
    const [statistics, setStatistics] = useState<PurchaseStatistics | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetch = useCallback(async () => {
        setLoading(true)
        setError(null)
        try {
            const data = await purchaseApi.getStatistics()
            setStatistics(data)
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('statistics.fetchError')
            setError(msg)
            toast.error(t('statistics.fetchError'))
        } finally {
            setLoading(false)
        }
    }, [t])

    useEffect(() => { fetch() }, [fetch])

    return { statistics, loading, error, refetch: fetch }
}

