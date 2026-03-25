import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { purchaseApi } from '@services/api/modules/purchase.api'
import type { Purchase, PurchaseRequest } from '@/types/modules/purchase.types'

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

interface UsePurchaseCreateReturn {
    /**
     * Submit a new purchase to the backend.
     * Resolves with the created `Purchase` on success,
     * or `null` if the request fails.
     */
    createPurchase: (data: PurchaseRequest) => Promise<Purchase | null>
    /** `true` while the POST is in flight */
    loading: boolean
    /** Error message from the last attempt, or `null` */
    error: string | null
}

// ────────────────────────────────────────────────────────────
// Hook
// ────────────────────────────────────────────────────────────

/**
 * Provides a `createPurchase` function that posts a new
 * purchase and handles loading / error / toast state.
 *
 * @example
 * ```tsx
 * const { createPurchase, loading } = usePurchaseCreate()
 *
 * const handleSubmit = async (data: PurchaseRequest) => {
 *     const created = await createPurchase(data)
 *     if (created) navigate(APP_ROUTES.PURCHASES.LIST)
 * }
 * ```
 */
export function usePurchaseCreate(): UsePurchaseCreateReturn {
    const { t } = useTranslation('purchase')

    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const createPurchase = useCallback(
        async (data: PurchaseRequest): Promise<Purchase | null> => {
            setLoading(true)
            setError(null)

            try {
                const created = await purchaseApi.create(data)
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

    return { createPurchase, loading, error }
}

