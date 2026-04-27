import { useCallback, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { profitApi } from '@services/api/modules/profit.api'
import type { MonthlyProfitDistribution, MonthlyProfitDistributionRequest } from '@/types/modules/profit.types'

export function useProfitDistributionUpdate() {
    const { t } = useTranslation('profit')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const updateDistribution = useCallback(async (id: number, data: MonthlyProfitDistributionRequest): Promise<MonthlyProfitDistribution | null> => {
        setLoading(true)
        setError(null)
        try {
            const updated = await profitApi.update(id, data)
            toast.success(t('updated'))
            return updated
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('updateError')
            setError(msg)
            toast.error(t('updateError'))
            return null
        } finally {
            setLoading(false)
        }
    }, [t])

    return { updateDistribution, loading, error }
}

