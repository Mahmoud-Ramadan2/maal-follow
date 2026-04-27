/**
 * useCollectionRouteCreate Hook
 *
 * Handles creating a new collection route.
 * Used in CollectionRouteCreatePage wizard form.
 */

import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { collectionApi } from '@services/api/modules/collection.api'
import type { CollectionRoute, CollectionRouteRequest } from '@/types/modules/collection.types'

interface UseCollectionRouteCreateReturn {
    /** Whether creation request is pending */
    loading: boolean

    /** Error message if creation failed */
    error: string | null

    /** Create a new collection route */
    create: (data: CollectionRouteRequest) => Promise<CollectionRoute>

    /** Clear error message */
    clearError: () => void
}

/**
 * Hook to create a new collection route.
 *
 * @returns Route creation controller functions
 *
 * @example
 * ```tsx
 * const { create, loading, error } = useCollectionRouteCreate()
 *
 * const handleSubmit = async (formData) => {
 *   try {
 *     const newRoute = await create({
 *       name: formData.name,
 *       routeType: formData.routeType,
 *       customerIds: selectedCustomerIds
 *     })
 *     navigate(`/collection-routes/${newRoute.id}`)
 *   } catch (err) {
 *     console.error(err)
 *   }
 * }
 *
 * return (
 *   <form onSubmit={handleSubmit}>
 *     {error && <Alert severity="error">{error}</Alert>}
 *     <TextField name="name" />
 *     <Select name="routeType">
 *       <option>BY_ADDRESS</option>
 *       <option>BY_DATE</option>
 *     </Select>
 *     <Button type="submit" disabled={loading}>
 *       {loading ? 'Creating...' : 'Create Route'}
 *     </Button>
 *   </form>
 * )
 * ```
 */
export function useCollectionRouteCreate(): UseCollectionRouteCreateReturn {
    const { t } = useTranslation('collection')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const create = async (data: CollectionRouteRequest): Promise<CollectionRoute> => {
        setLoading(true)
        setError(null)
        try {
            const newRoute = await collectionApi.create(data)
            toast.success(t('routeCreatedSuccess'))
            return newRoute
        } catch (err) {
            const message = err instanceof Error ? err.message : t('createRouteError')
            setError(message)
            toast.error(t('createRouteError'))
            console.error('Error creating collection route:', err)
            throw err
        } finally {
            setLoading(false)
        }
    }

    const clearError = () => setError(null)

    return {
        loading,
        error,
        create,
        clearError,
    }
}

