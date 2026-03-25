import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { contractApi } from '@services/api/modules/contract.api'
import type { Contract, ContractRequest } from '@/types/modules/contract.types'

interface UseContractCreateReturn {
    createContract: (data: ContractRequest) => Promise<Contract | null>
    loading: boolean
    error: string | null
}

export function useContractCreate(): UseContractCreateReturn {
    const { t } = useTranslation('contract')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const createContract = useCallback(async (data: ContractRequest): Promise<Contract | null> => {
        setLoading(true)
        setError(null)
        try {
            const created = await contractApi.create(data)
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

    return { createContract, loading, error }
}

