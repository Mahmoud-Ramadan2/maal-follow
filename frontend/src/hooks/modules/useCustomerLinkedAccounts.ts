import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { customerApi } from '@services/api/modules/customer.api'
import type {
    CustomerAccountLink,
    CustomerRelationshipType,
} from '@/types/modules/customer.types'

// ────────────────────────────────────────────────────────────
// useCustomerLinkedAccounts — fetch linked accounts for a customer
// ────────────────────────────────────────────────────────────

interface UseCustomerLinkedAccountsReturn {
    linkedAccounts: CustomerAccountLink[]
    loading: boolean
    error: string | null
    refetch: () => void
}

/**
 * Fetches all linked accounts for a specific customer.
 *
 * Maps to `GET /api/v1/customers/{customerId}/linked-accounts`.
 */
export function useCustomerLinkedAccounts(customerId: number): UseCustomerLinkedAccountsReturn {
    const { t } = useTranslation('customer')

    const [linkedAccounts, setLinkedAccounts] = useState<CustomerAccountLink[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetchLinkedAccounts = useCallback(async () => {
        if (!customerId) return
        setLoading(true)
        setError(null)

        try {
            const data = await customerApi.getLinkedAccounts(customerId)
            setLinkedAccounts(data)
        } catch (err) {
            const message = err instanceof Error ? err.message : t('linkedAccounts.fetchError')
            setError(message)
            toast.error(t('linkedAccounts.fetchError'))
        } finally {
            setLoading(false)
        }
    }, [customerId, t])

    useEffect(() => {
        fetchLinkedAccounts()
    }, [fetchLinkedAccounts])

    return { linkedAccounts, loading, error, refetch: fetchLinkedAccounts }
}

// ────────────────────────────────────────────────────────────
// useLinkedAccountsByType — fetch by relationship type
// ────────────────────────────────────────────────────────────

interface UseLinkedAccountsByTypeReturn {
    linkedAccounts: CustomerAccountLink[]
    loading: boolean
    error: string | null
    refetch: () => void
}

/**
 * Fetches linked accounts filtered by relationship type.
 *
 * Maps to `GET /api/v1/customers/linked-accounts/by-relation-type?relationshipType=...`
 */
export function useLinkedAccountsByType(
    relationshipType: CustomerRelationshipType | null,
): UseLinkedAccountsByTypeReturn {
    const { t } = useTranslation('customer')

    const [linkedAccounts, setLinkedAccounts] = useState<CustomerAccountLink[]>([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const fetchByType = useCallback(async () => {
        if (!relationshipType) {
            setLinkedAccounts([])
            return
        }
        setLoading(true)
        setError(null)

        try {
            const data = await customerApi.getLinkedAccountsByType(relationshipType)
            setLinkedAccounts(data)
        } catch (err) {
            const message = err instanceof Error ? err.message : t('linkedAccounts.fetchError')
            setError(message)
            toast.error(t('linkedAccounts.fetchError'))
        } finally {
            setLoading(false)
        }
    }, [relationshipType, t])

    useEffect(() => {
        fetchByType()
    }, [fetchByType])

    return { linkedAccounts, loading, error, refetch: fetchByType }
}

