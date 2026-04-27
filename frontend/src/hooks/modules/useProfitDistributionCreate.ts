import { useCallback, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { profitApi } from '@services/api/modules/profit.api'
import type { MonthlyProfitDistribution, MonthlyProfitDistributionRequest } from '@/types/modules/profit.types'

export function useProfitDistributionCreate() {
    const { t } = useTranslation('profit')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const createDistribution = useCallback(async (data: MonthlyProfitDistributionRequest): Promise<MonthlyProfitDistribution | null> => {
        setLoading(true)
        setError(null)
        try {
            const created = await profitApi.create(data)
            toast.success(t('created'))
            return created
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('createError')
            setError(msg)
            toast.error(t('createError'))
            return null
        } finally {
            setLoading(false)
        }
    }, [t])

    return { createDistribution, loading, error }
}

