import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { purchaseApi } from '@services/api/modules/purchase.api'
import type { Purchase } from '@/types/modules/purchase.types'

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

interface UsePurchaseReturn {
    /** The fetched purchase, or `null` while loading / on error */
    purchase: Purchase | null
    /** `true` while the API request is in flight */
    loading: boolean
    /** Error message if the fetch failed, otherwise `null` */
    error: string | null
    /** Re-fetch the purchase */
    refetch: () => void
}

// ────────────────────────────────────────────────────────────
// Hook
// ────────────────────────────────────────────────────────────

/**
 * Fetches a single purchase by ID.
 *
 * Automatically fetches on mount and whenever `id` changes.
 *
 * @param id - Purchase primary key.
 *
 * @example
 * ```tsx
 * const { id } = useParams()
 * const { purchase, loading, error } = usePurchase(Number(id))
 *
 * if (loading) return <LoadingSpinner />
 * if (!purchase) return <p>Not found</p>
 * return <h1>{purchase.productName}</h1>
 * ```
 */
export function usePurchase(id: number): UsePurchaseReturn {

    const {t} = useTranslation('purchase')
    const [purchase, setPurchase] = useState<Purchase | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetchPurchase = useCallback(async () => {
        // Skip fetch if id is invalid (create mode)
        if (id === undefined || id <= 0) {
            setPurchase(null)
            setLoading(false)
            setError(null)
            return
        }
        setLoading(true)
        setError(null)

        try {
            const data = await purchaseApi.getById(id)
            setPurchase(data)
        } catch (err) {
            const message = err instanceof Error ? err.message : t('fetchOneError')
            setError(message)
            toast.error(t('fetchOneError'))
        } finally {
            setLoading(false)
        }

    }, [id, t])

    useEffect(() => {
        fetchPurchase()
    }, [fetchPurchase])

    return {
        purchase,
        loading,
        error,
        refetch: fetchPurchase,
    }
}
