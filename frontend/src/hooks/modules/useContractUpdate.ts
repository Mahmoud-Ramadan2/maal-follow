import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { contractApi } from '@services/api/modules/contract.api'
import type { Contract, ContractRequest } from '@/types/modules/contract.types'

interface UseContractUpdateReturn {
    updateContract: (id: number, data: ContractRequest) => Promise<Contract | null>
    loading: boolean
    error: string | null
}

export function useContractUpdate(): UseContractUpdateReturn {
    const { t } = useTranslation('contract')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const updateContract = useCallback(async (id: number, data: ContractRequest): Promise<Contract | null> => {
        setLoading(true)
        setError(null)
        try {
            const updated = await contractApi.update(id, data)
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

    return { updateContract, loading, error }
}

