import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { profitApi } from '@services/api/modules/profit.api'
import type { MonthlyProfitDistribution, ProfitDistributionFilters } from '@/types/modules/profit.types'

interface UseProfitDistributionsReturn {
    distributions: MonthlyProfitDistribution[]
    loading: boolean
    error: string | null
    refetch: () => void
}

export function useProfitDistributions(filters?: ProfitDistributionFilters): UseProfitDistributionsReturn {
    const { t } = useTranslation('profit')
    const [distributions, setDistributions] = useState<MonthlyProfitDistribution[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetch = useCallback(async () => {
        setLoading(true)
        setError(null)
        try {
            setDistributions(await profitApi.getAll(filters))
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('fetchError')
            setError(msg)
            toast.error(t('fetchError'))
        } finally {
            setLoading(false)
        }
    }, [filters, t])

    useEffect(() => {
        void fetch()
    }, [fetch])

    return { distributions, loading, error, refetch: fetch }
}

