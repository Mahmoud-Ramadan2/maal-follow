import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { customerApi } from '@services/api/modules/customer.api'
import type { Contract } from '@/types/modules/contract.types'

interface UseCustomerContractsReturn {
    /** Paginated list of contracts for this customer */
    contracts: Contract[]
    loading: boolean
    error: string | null
    totalPages: number
    totalElements: number
    refetch: () => void
}

/**
 * Fetches paginated contracts for a specific customer.
 *
 * Maps to `GET /api/v1/customers/{id}/contracts?page=0&size=10`.
 *
 * @param customerId - Customer ID
 * @param page - Zero-based page index
 * @param size - Page size
 */
export function useCustomerContracts(
    customerId: number,
    page: number = 0,
    size: number = 10,
): UseCustomerContractsReturn {
    const { t } = useTranslation('customer')

    const [contracts, setContracts] = useState<Contract[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [totalPages, setTotalPages] = useState(0)
    const [totalElements, setTotalElements] = useState(0)

    const fetchContracts = useCallback(async () => {
        if (!customerId) return
        setLoading(true)
        setError(null)

        try {
            const data = await customerApi.getCustomerContracts(customerId, page, size)
            setContracts(data.content)
            setTotalPages(data.totalPages)
            setTotalElements(data.totalElements)
        } catch (err) {
            const message = err instanceof Error ? err.message : t('details.contractsFetchError')
            setError(message)
            toast.error(t('details.contractsFetchError'))
        } finally {
            setLoading(false)
        }
    }, [customerId, page, size, t])

    useEffect(() => {
        fetchContracts()
    }, [fetchContracts])

    return {
        contracts,
        loading,
        error,
        totalPages,
        totalElements,
        refetch: fetchContracts,
    }
}

