import { useCallback, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { profitApi } from '@services/api/modules/profit.api'
import type { MonthlyProfitDistribution, ProfitDistributionLifecycleStatus } from '@/types/modules/profit.types'

export function useProfitDistributionActions() {
    const { t } = useTranslation('profit')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const runAction = useCallback(async (
        action: 'calculate' | 'recalculate' | 'distribute' | 'lock',
        id: number,
    ): Promise<MonthlyProfitDistribution | null> => {
        setLoading(true)
        setError(null)
        try {
            const handlers = {
                calculate: profitApi.calculate,
                recalculate: profitApi.recalculate,
                distribute: profitApi.distribute,
                lock: profitApi.lock,
            }
            const result = await handlers[action](id)
            toast.success(t(`actions.${action}.success`))
            return result
        } catch (err) {
            const msg = err instanceof Error ? err.message : t(`actions.${action}.error`)
            setError(msg)
            toast.error(t(`actions.${action}.error`))
            return null
        } finally {
            setLoading(false)
        }
    }, [t])

    const getStatus = useCallback(async (id: number): Promise<ProfitDistributionLifecycleStatus | null> => {
        setLoading(true)
        setError(null)
        try {
            return await profitApi.getStatus(id)
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('actions.status.error')
            setError(msg)
            toast.error(t('actions.status.error'))
            return null
        } finally {
            setLoading(false)
        }
    }, [t])

    return {
        loading,
        error,
        calculate: (id: number) => runAction('calculate', id),
        recalculate: (id: number) => runAction('recalculate', id),
        distribute: (id: number) => runAction('distribute', id),
        lock: (id: number) => runAction('lock', id),
        getStatus,
    }
}

