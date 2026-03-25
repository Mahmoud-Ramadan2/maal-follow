import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { contractApi } from '@services/api/modules/contract.api'
import type { Contract } from '@/types/modules/contract.types'

interface UseContractCompleteReturn {
    /** Marks a contract as completed (backend delete is disabled) */
    completeContract: (id: number) => Promise<Contract | null>
    loading: boolean
    error: string | null
}

/**
 * The backend has the DELETE endpoint commented out.
 * Instead we expose `markAsCompleted` as the primary action.
 */
export function useContractComplete(): UseContractCompleteReturn {
    const { t } = useTranslation('contract')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const completeContract = useCallback(async (id: number): Promise<Contract | null> => {
        const confirmed = window.confirm(t('completeConfirm'))
        if (!confirmed) return null

        setLoading(true)
        setError(null)
        try {
            const updated = await contractApi.markAsCompleted(id)
            toast.success(t('completed'))
            return updated
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('completeError')
            setError(msg)
            toast.error(t('completeError'))
            return null
        } finally {
            setLoading(false)
        }
    }, [t])

    return { completeContract, loading, error }
}

