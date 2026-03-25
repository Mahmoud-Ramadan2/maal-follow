import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { customerApi } from '@services/api/modules/customer.api'
import type { Customer, CustomerFilters } from '@/types/modules/customer.types'
import {IS_DEV} from "@/config";

interface UseCustomersReturn {
    customers: Customer[]
    loading: boolean
    error: string | null
    totalPages: number
    totalElements: number
    refetch: () => void
}

/**
 * Fetches a paginated list of customers and re-fetches
 * automatically whenever `filters` change.
 */
export function useCustomers(filters?: CustomerFilters): UseCustomersReturn {
    const { t } = useTranslation('customer')

    const [customers, setCustomers] = useState<Customer[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [totalPages, setTotalPages] = useState(0)
    const [totalElements, setTotalElements] = useState(0)

    const fetchCustomers = useCallback(async () => {
        setLoading(true)
        setError(null)

        try {
            const page =
                await customerApi.getAll(filters)
            setCustomers(page.content)
            setTotalPages(page.totalPages)
            setTotalElements(page.totalElements)
            console.log("Fetched customers:", page.content)

        } catch (err) {
            // const message = err instanceof Error ? err.message : t('fetchError')
            const message = err instanceof Error ? IS_DEV ? err.message : t('fetchError') : t('fetchError')

            setError(message)
            toast.error(t('fetchError'))
        } finally {
            setLoading(false)
        }
    }, [filters, t])

    useEffect(() => {
        fetchCustomers()
    }, [fetchCustomers])

    return {
        customers,
        loading,
        error,
        totalPages,
        totalElements,
        refetch: fetchCustomers,
    }
}

