import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { profitApi } from '@services/api/modules/profit.api'
import type {
    MonthlyProfitDistribution,
    ProfitDistributionLifecycleStatus,
} from '@/types/modules/profit.types'

interface UseProfitDistributionReturn {
    distribution: MonthlyProfitDistribution | null
    lifecycleStatus: ProfitDistributionLifecycleStatus | null
    loading: boolean
    error: string | null
    refetch: () => void
}

export function useProfitDistribution(id: number): UseProfitDistributionReturn {
    const { t } = useTranslation('profit')
    const [distribution, setDistribution] = useState<MonthlyProfitDistribution | null>(null)
    const [lifecycleStatus, setLifecycleStatus] = useState<ProfitDistributionLifecycleStatus | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetch = useCallback(async () => {
        if (!id) return
        setLoading(true)
        setError(null)
        try {
            const [one, status] = await Promise.all([
                profitApi.getById(id),
                profitApi.getStatus(id).catch(() => null as ProfitDistributionLifecycleStatus | null),
            ])
            setDistribution(one)
            setLifecycleStatus(status)
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('fetchOneError')
            setError(msg)
            toast.error(t('fetchOneError'))
        } finally {
            setLoading(false)
        }
    }, [id, t])

    useEffect(() => {
        void fetch()
    }, [fetch])

    return { distribution, lifecycleStatus, loading, error, refetch: fetch }
}

