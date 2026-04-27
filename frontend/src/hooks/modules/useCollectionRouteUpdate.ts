/**
 * useCollectionRouteUpdate Hook
 *
 * Handles updating collection route metadata and lifecycle operations.
 * Used in CollectionRouteViewPage for editing route and deactivation.
 */

import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { collectionApi } from '@services/api/modules/collection.api'
import type { CollectionRoute, CollectionRouteUpdateRequest } from '@/types/modules/collection.types'

interface UseCollectionRouteUpdateReturn {
    /** Whether update request is pending */
    loading: boolean

    /** Error message if update failed */
    error: string | null

    /** Update route metadata (name, description, status) */
    update: (routeId: number, data: CollectionRouteUpdateRequest) => Promise<CollectionRoute>

    /** Deactivate a collection route */
    deactivate: (routeId: number) => Promise<void>

    /** Clear error message */
    clearError: () => void
}

/**
 * Hook to update collection route metadata and perform lifecycle operations.
 *
 * @returns Route update controller functions
 *
 * @example
 * ```tsx
 * const { update, deactivate, loading, error } = useCollectionRouteUpdate()
 *
 * const handleEditName = async () => {
 *   await update(routeId, { name: 'Cairo Zone B' })
 *   // Route updated successfully
 * }
 *
 * const handleDeactivate = async () => {
 *   if (confirm('Deactivate this route?')) {
 *     await deactivate(routeId)
 *     navigate('/collection-routes')
 *   }
 * }
 *
 * return (
 *   <div>
 *     {error && <Alert severity="error">{error}</Alert>}
 *     <TextField
 *       value={routeName}
 *       onChange={(e) => setRouteName(e.target.value)}
 *     />
 *     <Button onClick={handleEditName} disabled={loading}>
 *       Save Name
 *     </Button>
 *     <Button onClick={handleDeactivate} color="error" disabled={loading}>
 *       Deactivate Route
 *     </Button>
 *   </div>
 * )
 * ```
 */
export function useCollectionRouteUpdate(): UseCollectionRouteUpdateReturn {
    const { t } = useTranslation('collection')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const update = async (
        routeId: number,
        data: CollectionRouteUpdateRequest,
    ): Promise<CollectionRoute> => {
        setLoading(true)
        setError(null)
        try {
            const updatedRoute = await collectionApi.update(routeId, data)
            toast.success(t('routeUpdatedSuccess'))
            return updatedRoute
        } catch (err) {
            const message = err instanceof Error ? err.message : t('updateRouteError')
            setError(message)
            toast.error(t('updateRouteError'))
            console.error('Error updating collection route:', err)
            throw err
        } finally {
            setLoading(false)
        }
    }

    const deactivate = async (routeId: number): Promise<void> => {
        setLoading(true)
        setError(null)
        try {
            await collectionApi.deactivateRoute(routeId)
            toast.success(t('routeDeactivatedSuccess'))
        } catch (err) {
            const message = err instanceof Error ? err.message : t('deactivateRouteError')
            setError(message)
            toast.error(t('deactivateRouteError'))
            console.error('Error deactivating collection route:', err)
            throw err
        } finally {
            setLoading(false)
        }
    }

    const clearError = () => setError(null)

    return {
        loading,
        error,
        update,
        deactivate,
        clearError,
    }
}

