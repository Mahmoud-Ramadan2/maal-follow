import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { vendorApi } from '@services/api/modules/vendor.api'
import type { Vendor, VendorRequest } from '@/types/modules/vendor.types'

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

interface UseVendorCreateReturn {
    /**
     * Submit a new vendor to the backend.
     * Resolves with the created `Vendor` on success,
     * or `null` if the request fails.
     */
    createVendor: (data: VendorRequest) => Promise<Vendor | null>
    /** `true` while the POST is in flight */
    loading: boolean
    /** Error message from the last attempt, or `null` */
    error: string | null
}

// ────────────────────────────────────────────────────────────
// Hook
// ────────────────────────────────────────────────────────────

/**
 * Provides a `createVendor` function that posts a new
 * vendor and handles loading / error / toast state.
 *
 * @example
 * ```tsx
 * const { createVendor, loading } = useVendorCreate()
 *
 * const handleSubmit = async (data: VendorRequest) => {
 *     const created = await createVendor(data)
 *     if (created) navigate(APP_ROUTES.VENDORS.LIST)
 * }
 * ```
 */
export function useVendorCreate(): UseVendorCreateReturn {
    const { t } = useTranslation('vendor')

    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const createVendor = useCallback(
        async (data: VendorRequest): Promise<Vendor | null> => {
            setLoading(true)
            setError(null)

            try {
                const created = await vendorApi.create(data)
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

    return { createVendor, loading, error }
}

