import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { capitalPoolApi } from '@services/api/modules/capital.api'
import type { CapitalPool } from '@/types/modules/capital.types'

export function useCapitalPool() {
    const { t } = useTranslation('capital')
    const [pool, setPool] = useState<CapitalPool | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetch = useCallback(async () => {
        setLoading(true); setError(null)
        try {
            setPool(await capitalPoolApi.getCurrent())
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('pool.fetchError')
            setError(msg); toast.error(t('pool.fetchError'))
        } finally { setLoading(false) }
    }, [t])

    useEffect(() => { fetch() }, [fetch])
    return { pool, loading, error, refetch: fetch }
}

