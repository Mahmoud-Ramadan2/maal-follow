import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { customerApi } from '@services/api/modules/customer.api'
import type { CustomerDetails, CustomerAccountLink } from '@/types/modules/customer.types'

interface UseCustomerReturn {
    /** The fetched customer (with contracts), or `null` while loading / on error */
    customer: CustomerDetails | null
    /** Linked accounts for this customer */
    linkedAccounts: CustomerAccountLink[]
    /** `true` while the API request is in flight */
    loading: boolean
    /** Error message if the fetch failed, otherwise `null` */
    error: string | null
    /** Re-fetch the customer */
    refetch: () => void
}

/**
 * Fetches a single customer by ID (with contracts and linked accounts).
 *
 * Automatically fetches on mount and whenever `id` changes.
 */
export function useCustomer(id: number): UseCustomerReturn {
    const { t } = useTranslation('customer')

    const [customer, setCustomer] = useState<CustomerDetails | null>(null)
    const [linkedAccounts, setLinkedAccounts] = useState<CustomerAccountLink[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetchCustomer = useCallback(async () => {
        if (!id) return
        setLoading(true)
        setError(null)

        try {
            const [details, links] = await Promise.all([
                customerApi.getWithContracts(id),
                customerApi.getLinkedAccounts(id),
            ])

            setCustomer(details)
            setLinkedAccounts(links)
            console.log("Customer details:", details)
            console.log("Customer linked accounts:", links)
        } catch (err) {
            const message = err instanceof Error ? err.message : t('fetchOneError')
            setError(message)
            toast.error(t('fetchOneError'))
        } finally {
            setLoading(false)
        }
    }, [id, t])

    useEffect(() => {
        fetchCustomer()
    }, [fetchCustomer])

    return {
        customer,
        linkedAccounts,
        loading,
        error,
        refetch: fetchCustomer,
    }
}

