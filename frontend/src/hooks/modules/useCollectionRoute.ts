/**
 * useCollectionRoute Hook
 *
 * Fetches a single collection route by ID along with all its items.
 * Used in CollectionRouteViewPage for viewing and managing a specific route.
 */

import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { collectionApi } from '@services/api/modules/collection.api'
import type { CollectionRoute, CollectionRouteItem } from '@/types/modules/collection.types'

interface UseCollectionRouteReturn {
    /** The fetched collection route (null while loading) */
    route: CollectionRoute | null

    /** Array of route items (direct reference from route.routeItems) */
    items: CollectionRouteItem[]

    /** Whether data is being fetched */
    loading: boolean

    /** Error message if fetch failed */
    error: string | null

    /** Re-fetch the route from backend */
    refetch: () => void
}

interface UseCollectionRouteOptions {
    /** Auto-fetch on mount (default: true) */
    autoFetch?: boolean
}

/**
 * Hook to fetch a single collection route with all its items.
 *
 * @param routeId The ID of the collection route to fetch
 * @param options Configuration options
 * @returns Route data and control functions
 *
 * @example
 * ```tsx
 * const { route, items, loading, error } = useCollectionRoute(5)
 *
 * if (loading) return <PageLoader />
 * if (error) return <ErrorMessage error={error} />
 *
 * return (
 *   <div>
 *     <h1>{route.name}</h1>
 *     <p>Type: {route.routeType}</p>
 *     <table>
 *       {items.map((item, idx) => (
 *         <tr key={item.id}>
 *           <td>{item.sequenceOrder}</td>
 *           <td>{item.customer.name}</td>
 *           <td>{item.collectionStatus}</td>
 *         </tr>
 *       ))}
 *     </table>
 *   </div>
 * )
 * ```
 */
export function useCollectionRoute(
    routeId: number,
    options: UseCollectionRouteOptions = {},
): UseCollectionRouteReturn {
    const { t } = useTranslation('collection')
    const { autoFetch = true } = options

    const [route, setRoute] = useState<CollectionRoute | null>(null)
    const [items, setItems] = useState<CollectionRouteItem[]>([])
    const [loading, setLoading] = useState(autoFetch && !!routeId)
    const [error, setError] = useState<string | null>(null)

    const fetch = useCallback(async () => {
        if (!routeId) return

        setLoading(true)
        setError(null)
        try {
            const routeData = await collectionApi.getById(routeId)
            setRoute(routeData)
            setItems(routeData.routeItems || [])
        } catch (err) {
            const message = err instanceof Error ? err.message : t('fetchRouteError')
            setError(message)
            toast.error(t('fetchRouteError'))
            console.error('Error fetching collection route:', err)
        } finally {
            setLoading(false)
        }
    }, [routeId, t])

    // Fetch on mount and when routeId changes
    useEffect(() => {
        if (autoFetch && routeId) {
            fetch()
        }
    }, [routeId, autoFetch, fetch])

    return {
        route,
        items,
        loading,
        error,
        refetch: fetch,
    }
}

