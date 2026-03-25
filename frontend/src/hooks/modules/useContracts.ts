import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { contractApi } from '@services/api/modules/contract.api'
import type { Contract, ContractStatus } from '@/types/modules/contract.types'
import {IS_DEV} from "@/config";

interface UseContractsReturn {
    contracts: Contract[]
    loading: boolean
    error: string | null
    totalPages: number
    totalElements: number
    refetch: () => void
}

/**
 * Fetches a paginated list of contracts filtered by status.
 * Re-fetches when status, page, or size change.
 */
export function useContracts(
    status: ContractStatus,
    page = 0,
    size = 20,
): UseContractsReturn {
    const { t } = useTranslation('contract')
    const [contracts, setContracts] = useState<Contract[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [totalPages, setTotalPages] = useState(0)
    const [totalElements, setTotalElements] = useState(0)

    const fetch = useCallback(async () => {
        setLoading(true)
        setError(null)
        try {
            const data = await contractApi.getByStatus(status, page, size)
            setContracts(data.content)
            setTotalPages(data.totalPages)
            setTotalElements(data.totalElements)
        } catch (err) {
            // const msg = err instanceof Error ? err.message : t('fetchError')
            const msg = err instanceof Error ? IS_DEV ? err.message : t(  'fetchError') : t('fetchError')

            setError(msg)
            toast.error(t('fetchError'))
        } finally {
            setLoading(false)
        }
    }, [status, page, size, t])

    useEffect(() => { fetch() }, [fetch])

    return { contracts, loading, error, totalPages, totalElements, refetch: fetch }
}

