import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { purchaseApi } from '@services/api/modules/purchase.api'
import type { Purchase, PurchaseFilters } from '@/types/modules/purchase.types'
import {IS_DEV} from "@/config";

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

interface UsePurchasesReturn {
    /** Array of purchases on the current page */
    purchases: Purchase[]
    /** `true` while the API request is in flight */
    loading: boolean
    /** Error message if the last fetch failed, otherwise `null` */
    error: string | null
    /** Total pages available (from Spring Data `Page`) */
    totalPages: number
    /** Total number of purchases across all pages */
    totalElements: number
    /** Re-fetch with the current filters */
    refetch: () => void
}

// ────────────────────────────────────────────────────────────
// Hook
// ────────────────────────────────────────────────────────────

/**
 * Fetches a paginated list of purchases and re-fetches
 * automatically whenever `filters` change.
 *
 * @param filters - Optional pagination, sort, and filter params.
 *
 * @example
 * ```tsx
 * const { page, size } = usePagination()
 * const { purchases, loading, totalPages, refetch } = usePurchases({ page, size })
 *
 * <Table columns={columns} data={purchases} loading={loading} />
 * ```
 */
export function usePurchases(filters?: PurchaseFilters): UsePurchasesReturn {
    const { t } = useTranslation('purchase')

    const [purchases, setPurchases] = useState<Purchase[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [totalPages, setTotalPages] = useState(0)
    const [totalElements, setTotalElements] = useState(0)


    const fetchPurchases = useCallback(
        async () => {
        setLoading(true)
        setError(null)

        try {
            const page =
                await purchaseApi.getAll(filters)

            setPurchases(page.content)
            setTotalPages(page.totalPages)
            setTotalElements(page.totalElements)
        } catch (err) {

            const message = err instanceof Error ? (IS_DEV ? err.message : t('fetchError')) : t('fetchError')
            // const message = IS_DEV ? err.message : t('fetchError')
            setError(message)
            toast.error(t('fetchError'))
        } finally {
            setLoading(false)
        }
    }, [filters, t])

    // Auto-fetch when filters change
    useEffect(() => {
        fetchPurchases()
    }, [fetchPurchases])

    return {
        purchases,
        loading,
        error,
        totalPages,
        totalElements,
        refetch: fetchPurchases,
    }
}

