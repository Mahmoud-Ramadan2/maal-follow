import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { purchaseApi } from '@services/api/modules/purchase.api'
import type { Purchase, PurchaseRequest } from '@/types/modules/purchase.types'

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

interface UsePurchaseUpdateReturn {
    /**
     * Submit updated data for an existing purchase.
     * Resolves with the updated `Purchase` on success,
     * or `null` if the request fails.
     */
    updatePurchase:
        (id: number, data: PurchaseRequest) => Promise<Purchase | null>
    /** `true` while the PUT is in flight */
    loading: boolean
    /** Error message from the last attempt, or `null` */
    error: string | null
}

// ────────────────────────────────────────────────────────────
// Hook
// ────────────────────────────────────────────────────────────

/**
 * Provides an `updatePurchase` function that sends a PUT
 * request and handles loading / error / toast state.
 *
 * @example
 * ```tsx
 * const { updatePurchase, loading } = usePurchaseUpdate()
 *
 * const handleSubmit = async (data: PurchaseRequest) => {
 *     const updated = await updatePurchase(purchaseId, data)
 *     if (updated) navigate(APP_ROUTES.PURCHASES.LIST)
 * }
 * ```
 */
export function usePurchaseUpdate(): UsePurchaseUpdateReturn {
    const { t } = useTranslation('purchase')

    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const updatePurchase = useCallback(
        async (id: number, data: PurchaseRequest): Promise<Purchase | null> => {
            setLoading(true)
            setError(null)

            try {
                const updated = await purchaseApi.update(id, data)
                // console.log("Updated purchase from use:", updated)
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

    return { updatePurchase, loading, error }
}

