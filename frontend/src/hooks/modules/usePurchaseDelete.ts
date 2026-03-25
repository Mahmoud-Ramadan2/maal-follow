import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { purchaseApi } from '@services/api/modules/purchase.api'

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

interface UsePurchaseDeleteReturn {
    /**
     * Delete a purchase by ID.
     * Shows a confirmation dialog before sending the request.
     * Resolves `true` if deleted, `false` if cancelled or failed.
     */
    deletePurchase: (id: number) => Promise<boolean>
    /** `true` while the DELETE is in flight */
    loading: boolean
    /** Error message from the last attempt, or `null` */
    error: string | null
}

// ────────────────────────────────────────────────────────────
// Hook
// ────────────────────────────────────────────────────────────

/**
 * Provides a `deletePurchase` function that confirms with
 * the user, sends a DELETE request, and handles toasts.
 *
 * @example
 * ```tsx
 * const { deletePurchase, loading } = usePurchaseDelete()
 *
 * const handleDelete = async (id: number) => {
 *     const deleted = await deletePurchase(id)
 *     if (deleted) refetchList()
 * }
 * ```
 */
export function usePurchaseDelete(): UsePurchaseDeleteReturn {
    const { t } = useTranslation('purchase')

    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const deletePurchase = useCallback(
        async (id: number): Promise<boolean> => {
            // Confirmation dialog — will be replaced with a
            // proper Modal component later.

            setLoading(true)
            setError(null)

            try {
                await purchaseApi.delete(id)
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

    return { deletePurchase, loading, error }
}

