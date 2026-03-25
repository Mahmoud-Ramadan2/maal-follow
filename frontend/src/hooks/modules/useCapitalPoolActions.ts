import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { capitalPoolApi } from '@services/api/modules/capital.api'
import type { CapitalPool, CapitalPoolRequest } from '@/types/modules/capital.types'

export function useCapitalPoolActions() {
    const { t } = useTranslation('capital')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const createPool = useCallback(async (data: CapitalPoolRequest): Promise<CapitalPool | null> => {
        setLoading(true); setError(null)
        try {
            const created = await capitalPoolApi.create(data)
            toast.success(t('pool.created'))
            return created
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('pool.createError')
            setError(msg); toast.error(t('pool.createError'))
            return null
        } finally { setLoading(false) }
    }, [t])

    const updatePool = useCallback(async (data: CapitalPoolRequest): Promise<CapitalPool | null> => {
        setLoading(true); setError(null)
        try {
            const updated = await capitalPoolApi.update(data)
            toast.success(t('pool.updated'))
            return updated
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('pool.updateError')
            setError(msg); toast.error(t('pool.updateError'))
            return null
        } finally { setLoading(false) }
    }, [t])

    const recalculate = useCallback(async (): Promise<CapitalPool | null> => {
        setLoading(true); setError(null)
        try {
            const result = await capitalPoolApi.recalculate()
            toast.success(t('pool.recalculated'))
            return result
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('pool.recalculateError')
            setError(msg); toast.error(t('pool.recalculateError'))
            return null
        } finally { setLoading(false) }
    }, [t])

    return { createPool, updatePool, recalculate, loading, error }
}

