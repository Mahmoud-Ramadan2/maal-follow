import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { customerApi } from '@services/api/modules/customer.api'
import type { CustomerResponse, CustomerRequest } from '@/types/modules/customer.types'

interface UseCustomerCreateReturn {
    createCustomer: (data: CustomerRequest) => Promise<CustomerResponse | null>
    loading: boolean
    error: string | null
}

/**
 * Provides a `createCustomer` function that posts a new
 * customer and handles loading / error / toast state.
 */
export function useCustomerCreate(): UseCustomerCreateReturn {
    const { t } = useTranslation('customer')

    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const createCustomer = useCallback(
        async (data: CustomerRequest): Promise<CustomerResponse | null> => {
            setLoading(true)
            setError(null)

            try {
                const created = await customerApi.create(data)
                toast.success(t('created'))
                return created
            } catch (err) {
                const message = err instanceof Error ? err.message : t('createError')
                setError(message)
                toast.error(t('createError'))
                return null
            } finally {
                setLoading(false)
            }
        },
        [t],
    )

    return { createCustomer, loading, error }
}

