import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { customerApi } from '@services/api/modules/customer.api'
import type { CustomerResponse, CustomerRequest } from '@/types/modules/customer.types'

interface UseCustomerUpdateReturn {
    updateCustomer: (id: number, data: CustomerRequest) => Promise<CustomerResponse | null>
    loading: boolean
    error: string | null
}

/**
 * Provides an `updateCustomer` function that sends a PUT
 * request and handles loading / error / toast state.
 */
export function useCustomerUpdate(): UseCustomerUpdateReturn {
    const { t } = useTranslation('customer')

    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const updateCustomer = useCallback(
        async (id: number, data: CustomerRequest): Promise<CustomerResponse | null> => {
            setLoading(true)
            setError(null)

            try {
                const updated = await customerApi.update(id, data)
                toast.success(t('updated'))
                return updated
            } catch (err) {
                const message = err instanceof Error ? err.message : t('updateError')
                setError(message)
                toast.error(t('updateError'))
                return null
            } finally {
                setLoading(false)
            }
        },
        [t],
    )

    return { updateCustomer, loading, error }
}

