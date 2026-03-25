import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { customerApi } from '@services/api/modules/customer.api'

interface UseCustomerDeleteReturn {
    deleteCustomer: (id: number) => Promise<boolean>
    loading: boolean
    error: string | null
}

/**
 * Provides a `deleteCustomer` function that confirms with
 * the user, sends a DELETE request, and handles toasts.
 */
export function useCustomerDelete():
    UseCustomerDeleteReturn {
    const { t } = useTranslation('customer')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const deleteCustomer =
        useCallback(
        async (id: number): Promise<boolean> => {
            setLoading(true)
            setError(null)

            try {
                await customerApi.delete(id)
                toast.success(t('deleted'))
                return true
            } catch (err) {
                const message = err instanceof Error ? err.message : t('deleteError')
                setError(message)
                toast.error(t('deleteError'))
                return false
            } finally {
                setLoading(false)
            }
        },
        [t],
    )

    return { deleteCustomer, loading, error }
}

