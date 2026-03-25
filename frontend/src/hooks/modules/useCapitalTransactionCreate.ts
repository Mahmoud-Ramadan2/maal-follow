import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { capitalTransactionApi } from '@services/api/modules/capital.api'
import type { CapitalTransaction, CapitalTransactionRequest } from '@/types/modules/capital.types'

export function useCapitalTransactionCreate() {
    const { t } = useTranslation('capital')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const createTransaction = useCallback(async (data: CapitalTransactionRequest): Promise<CapitalTransaction | null> => {
        setLoading(true); setError(null)
        try {
            const created = await capitalTransactionApi.create(data)
            toast.success(t('transaction.created'))
            return created
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('transaction.createError')
            setError(msg); toast.error(t('transaction.createError'))
            return null
        } finally { setLoading(false) }
    }, [t])

    return { createTransaction, loading, error }
}

