import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { vendorApi } from '@services/api/modules/vendor.api'
import type { Vendor, VendorFilters } from '@/types/modules/vendor.types'
import {IS_DEV} from "@/config";

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

interface UseVendorsReturn {
    /** Array of vendors on the current page */
    vendors: Vendor[]
    /** `true` while the API request is in flight */
    loading: boolean
    /** Error message if the last fetch failed, otherwise `null` */
    error: string | null
    /** Total pages available (from Spring Data `Page`) */
    totalPages: number
    /** Total number of vendors across all pages */
    totalElements: number
    /** Re-fetch with the current filters */
    refetch: () => void
}

// ────────────────────────────────────────────────────────────
// Hook
// ────────────────────────────────────────────────────────────

/**
 * Fetches a paginated list of vendors and re-fetches
 * automatically whenever `filters` change.
 *
 * @param filters - Optional pagination, sort, and filter params.
 *
 * @example
 * ```tsx
 * const { page, size } = usePagination()
 * const { vendors, loading, totalPages, refetch } = useVendors({ page, size })
 *
 * <Table columns={columns} data={vendors} loading={loading} />
 * ```
 */
export function useVendors(filters?: VendorFilters): UseVendorsReturn {
    // console.log("vendors Called")
    const { t } = useTranslation('vendor')

    const [vendors, setVendors] = useState<Vendor[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [totalPages, setTotalPages] = useState(0)
    const [totalElements, setTotalElements] = useState(0)

    const fetchVendors = useCallback(async () => {
        setLoading(true)
        setError(null)

        try {
            const page = await vendorApi.getAll(filters)

            setVendors(page.content)
            setTotalPages(page.totalPages)
            setTotalElements(page.totalElements)
        } catch (err) {
            const message =err instanceof Error ? IS_DEV ? err.message : t('fetchError') : t('fetchError')
            setError(message)
            toast.error(t('fetchError'))
        } finally {
            setLoading(false)
        }
    }, [filters, t])

    // Auto-fetch when filters change
    useEffect(() => {
        fetchVendors()
    }, [fetchVendors])

    return {
        vendors,
        loading,
        error,
        totalPages,
        totalElements,
        refetch: fetchVendors,
    }
}

