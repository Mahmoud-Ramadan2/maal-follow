import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { vendorApi } from '@services/api/modules/vendor.api'
import type { VendorDetails } from '@/types/modules/vendor.types'

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

interface UseVendorReturn {
    /** The fetched vendor, or `null` while loading / on error */
    vendor: VendorDetails | null
    /** `true` while the API request is in flight */
    loading: boolean
    /** Error message if the fetch failed, otherwise `null` */
    error: string | null
    /** Re-fetch the vendor */
    refetch: () => void
}

// ────────────────────────────────────────────────────────────
// Hook
// ────────────────────────────────────────────────────────────

/**
 * Fetches a single vendor by ID.
 *
 * Automatically fetches on mount and whenever `id` changes.
 *
 * @param id - Vendor primary key.
 *
 * @example
 * ```tsx
 * const { id } = useParams()
 * const { vendor, loading, error } = useVendor(Number(id))
 *
 * if (loading) return <LoadingSpinner />
 * if (!vendor) return <p>Not found</p>
 * return <h1>{vendor.name}</h1>
 * ```
 */
export function useVendor(id: number): UseVendorReturn {
    const { t } = useTranslation('vendor')

    const [vendor, setVendor] = useState<VendorDetails | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetchVendor = useCallback(async () => {
        if (!id) return
        setLoading(true)
        setError(null)

        try {
            const data = await vendorApi.getById(id)
            setVendor(data)
        } catch (err) {
            const message = err instanceof Error ? err.message : t('fetchOneError')
            setError(message)
            toast.error(t('fetchOneError'))
        } finally {
            setLoading(false)
        }
    }, [id, t])

    useEffect(() => {
        fetchVendor()
    }, [fetchVendor])

    return {
        vendor,
        loading,
        error,
        refetch: fetchVendor,
    }
}

