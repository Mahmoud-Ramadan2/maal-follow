import { useCallback, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { profitApi } from '@services/api/modules/profit.api'

export function useProfitDistributionDelete() {
    const { t } = useTranslation('profit')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const deleteDistribution = useCallback(async (id: number): Promise<boolean> => {
        const confirmed = window.confirm(t('deleteConfirm'))
        if (!confirmed) return false
        setLoading(true)
        setError(null)
        try {
            await profitApi.delete(id)
            toast.success(t('deleted'))
            return true
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('deleteError')
            setError(msg)
            toast.error(t('deleteError'))
            return false
        } finally {
            setLoading(false)
        }
    }, [t])

    return { deleteDistribution, loading, error }
}

