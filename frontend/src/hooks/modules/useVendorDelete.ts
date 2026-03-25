import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { vendorApi } from '@services/api/modules/vendor.api'

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

interface UseVendorDeleteReturn {
    /**
     * Delete a vendor by ID.
     * Shows a confirmation dialog before sending the request.
     * Resolves `true` if deleted, `false` if cancelled or failed.
     */
    deleteVendor: (id: number) => Promise<boolean>
    /** `true` while the DELETE is in flight */
    loading: boolean
    /** Error message from the last attempt, or `null` */
    error: string | null
}

// ────────────────────────────────────────────────────────────
// Hook
// ────────────────────────────────────────────────────────────

/**
 * Provides a `deleteVendor` function that confirms with
 * the user, sends a DELETE request, and handles toasts.
 *
 * @example
 * ```tsx
 * const { deleteVendor, loading } = useVendorDelete()
 *
 * const handleDelete = async (id: number) => {
 *     const deleted = await deleteVendor(id)
 *     if (deleted) refetchList()
 * }
 * ```
 */
export function useVendorDelete(): UseVendorDeleteReturn {
    const { t } = useTranslation('vendor')

    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const deleteVendor = useCallback(
        async (id: number): Promise<boolean> => {
            // Confirmation dialog — will be replaced with a
            // proper Modal component later.
            const confirmed = window.confirm(t('deleteConfirm'))
            if (!confirmed) return false

            setLoading(true)
            setError(null)

            try {
                await vendorApi.delete(id)
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

    return { deleteVendor, loading, error }
}

