import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { customerApi } from '@services/api/modules/customer.api'
import type { CustomerRelationshipType } from '@/types/modules/customer.types'

interface LinkAccountParams {
    customerId: number
    linkedCustomerId: number
    relationshipType: CustomerRelationshipType
    description?: string
}

interface UseCustomerLinkAccountReturn {
    /** Creates a new link between two customer accounts */
    linkAccounts: (params: LinkAccountParams) => Promise<boolean>
    loading: boolean
    error: string | null
}

/**
 * Hook for linking two customer accounts together.
 *
 * Maps to `POST /api/v1/customers/{customerId}/link/{linkedCustomerId}`.
 *
 * @example
 * ```tsx
 * const { linkAccounts, loading } = useCustomerLinkAccount()
 * await linkAccounts({
 *   customerId: 1,
 *   linkedCustomerId: 2,
 *   relationshipType: 'FAMILY_MEMBER',
 *   description: 'Brother',
 * })
 * ```
 */
export function useCustomerLinkAccount(): UseCustomerLinkAccountReturn {
    const { t } = useTranslation('customer')

    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const linkAccounts = useCallback(
        async (params: LinkAccountParams): Promise<boolean> => {
            setLoading(true)
            setError(null)

            try {
                await customerApi.linkAccounts(
                    params.customerId,
                    params.linkedCustomerId,
                    params.relationshipType,
                    params.description,
                )
                toast.success(t('linkedAccounts.linkSuccess'))
                return true
            } catch (err) {
                const message = err instanceof Error ? err.message : t('linkedAccounts.linkError')
                setError(message)
                toast.error(message)
                return false
            } finally {
                setLoading(false)
            }
        },
        [t],
    )

    return { linkAccounts, loading, error }
}

