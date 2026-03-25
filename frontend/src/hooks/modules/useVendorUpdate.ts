import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { vendorApi } from '@services/api/modules/vendor.api'
import type { Vendor, VendorRequest } from '@/types/modules/vendor.types'

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

interface UseVendorUpdateReturn {
    /**
     * Submit updated data for an existing vendor.
     * Resolves with the updated `Vendor` on success,
     * or `null` if the request fails.
     */
    updateVendor: (id: number, data: VendorRequest) => Promise<Vendor | null>
    /** `true` while the PATCH is in flight */
    loading: boolean
    /** Error message from the last attempt, or `null` */
    error: string | null
}

// ────────────────────────────────────────────────────────────
// Hook
// ────────────────────────────────────────────────────────────

/**
 * Provides an `updateVendor` function that sends a PATCH
 * request and handles loading / error / toast state.
 *
 * @example
 * ```tsx
 * const { updateVendor, loading } = useVendorUpdate()
 *
 * const handleSubmit = async (data: VendorRequest) => {
 *     const updated = await updateVendor(vendorId, data)
 *     if (updated) navigate(APP_ROUTES.VENDORS.LIST)
 * }
 * ```
 */
export function useVendorUpdate(): UseVendorUpdateReturn {
    const { t } = useTranslation('vendor')

    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const updateVendor = useCallback(
        async (id: number, data: VendorRequest): Promise<Vendor | null> => {
            setLoading(true)
            setError(null)

            try {
                const updated = await vendorApi.update(id, data)
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

    return { updateVendor, loading, error }
}

