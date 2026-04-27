/**
 * useCollectionRoutes Hook
 *
 * Fetches paginated list of active collection routes.
 * Used in CollectionRouteListPage for browsing and filtering routes.
 */

import { useState, useEffect, useCallback, useMemo } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { collectionApi } from '@services/api/modules/collection.api'
import type { CollectionRoute } from '@/types/modules/collection.types'

interface UseCollectionRoutesReturn {
    /** Array of collection routes for current page */
    routes: CollectionRoute[]

    /** Current page number (0-indexed) */
    page: number

    /** Items per page */
    size: number

    /** Total number of pages available */
    totalPages: number

    /** Total number of routes */
    totalElements: number

    /** Whether data is being fetched */
    loading: boolean

    /** Error message if fetch failed */
    error: string | null

    /** Re-fetch routes from backend */
    refetch: () => void

    /** Navigate to specific page */
    setPage: (page: number) => void

    /** Change page size */
    setSize: (size: number) => void
}

interface UseCollectionRoutesOptions {
    /** Initial page (default: 0) */
    initialPage?: number

    /** Initial page size (default: 10) */
    initialSize?: number

    /** Auto-fetch on mount (default: true) */
    autoFetch?: boolean
}

/**
 * Hook to fetch collection routes with pagination.
 *
 * @param options Configuration options
 * @returns Routes data and control functions
 *
 * @example
 * ```tsx
 * const { routes, page, setPage, loading, error } = useCollectionRoutes({
 *   initialPage: 0,
 *   initialSize: 20
 * })
 *
 * if (loading) return <PageLoader />
 * return (
 *   <table>
 *     {routes.map(route => (
 *       <tr key={route.id}>
 *         <td>{route.name}</td>
 *         <td>{route.routeType}</td>
 *       </tr>
 *     ))}
 *   </table>
 * )
 * ```
 */
export function useCollectionRoutes(
    options: UseCollectionRoutesOptions = {},
): UseCollectionRoutesReturn {
    const { t } = useTranslation('collection')
    const { initialPage = 0, initialSize = 10, autoFetch = true } = options

    const [allRoutes, setAllRoutes] = useState<CollectionRoute[]>([])
    const [page, setPage] = useState(initialPage)
    const [size, setSize] = useState(initialSize)
    const [totalPages, setTotalPages] = useState(0)
    const [totalElements, setTotalElements] = useState(0)
    const [loading, setLoading] = useState(autoFetch)
    const [error, setError] = useState<string | null>(null)

    const fetch = useCallback(async () => {
        setLoading(true)
        setError(null)
        try {
            const response = await collectionApi.getActiveRoutes()
            setAllRoutes(response)
            setTotalElements(response.length)
            setTotalPages(response.length === 0 ? 0 : Math.ceil(response.length / size))
        } catch (err) {
            const message = err instanceof Error ? err.message : t('fetchRoutesError')
            setError(message)
            toast.error(t('fetchRoutesError'))
            console.error('Error fetching collection routes:', err)
        } finally {
            setLoading(false)
        }
    }, [page, size, t])

    // Fetch on mount and when page/size changes
    useEffect(() => {
        if (autoFetch) {
            fetch()
        }
    }, [page, size, fetch, autoFetch])

    const routes = useMemo(() => {
        const start = page * size
        const end = start + size
        return allRoutes.slice(start, end)
    }, [allRoutes, page, size])

    return {
        routes,
        page,
        size,
        totalPages,
        totalElements,
        loading,
        error,
        refetch: fetch,
        setPage,
        setSize,
    }
}

