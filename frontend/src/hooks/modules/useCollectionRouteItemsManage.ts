/**
 * useCollectionRouteItemsManage Hook
 *
 * Handles managing route items: add, remove, reorder, and update status.
 * Used in CollectionRouteViewPage for item operations.
 */

import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { collectionApi } from '@services/api/modules/collection.api'
import type {
    CollectionRoute,
    CollectionRouteItem,
    CollectionRouteItemStatusUpdateRequest,
} from '@/types/modules/collection.types'

interface UseCollectionRouteItemsManageReturn {
    /** Whether any operation is pending */
    loading: boolean

    /** Error message if operation failed */
    error: string | null

    /** Add a customer to a route */
    addCustomer: (
        routeId: number,
        customerId: number,
        sequenceOrder?: number,
        notes?: string,
    ) => Promise<CollectionRouteItem>

    /** Remove an item from a route */
    removeItem: (itemId: number) => Promise<void>

    /** Batch reorder items in a route */
    reorderItems: (routeId: number, itemIds: number[]) => Promise<CollectionRoute>

    /** Update collection status of an item */
    updateItemStatus: (
        itemId: number,
        data: CollectionRouteItemStatusUpdateRequest,
    ) => Promise<CollectionRouteItem>

    /** Clear error message */
    clearError: () => void
}

/**
 * Hook to manage collection route items (add, remove, reorder, update status).
 *
 * @returns Item management controller functions
 *
 * @example
 * ```tsx
 * const {
 *   addCustomer,
 *   removeItem,
 *   reorderItems,
 *   updateItemStatus,
 *   loading,
 *   error
 * } = useCollectionRouteItemsManage()
 *
 * // Add customer to route
 * const handleAddCustomer = async (customerId) => {
 *   const newItem = await addCustomer(routeId, customerId, 5, 'High priority')
 *   refetchRoute()
 * }
 *
 * // Mark as collected
 * const handleMarkCollected = async (itemId, amount) => {
 *   await updateItemStatus(itemId, {
 *     status: 'COLLECTED',
 *     collectedAmount: amount,
 *     notes: 'Paid with check'
 *   })
 *   refetchRoute()
 * }
 *
 * // Reorder route items
 * const handleReorder = async (newItemIds) => {
 *   const updatedRoute = await reorderItems(routeId, newItemIds)
 *   setItems(updatedRoute.routeItems)
 * }
 *
 * return (
 *   <div>
 *     {error && <Alert severity="error">{error}</Alert>}
 *     Item management UI
 *   </div>
 * )
 * ```
 */
export function useCollectionRouteItemsManage(): UseCollectionRouteItemsManageReturn {
    const { t } = useTranslation('collection')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const addCustomer = async (
        routeId: number,
        customerId: number,
        sequenceOrder?: number,
        notes?: string,
    ): Promise<CollectionRouteItem> => {
        setLoading(true)
        setError(null)
        try {
            const item = await collectionApi.addCustomerToRoute(routeId, customerId, {
                sequenceOrder,
                notes,
            })
            toast.success(t('customerAddedSuccess'))
            return item
        } catch (err) {
            const message = err instanceof Error ? err.message : t('addCustomerError')
            setError(message)
            toast.error(t('addCustomerError'))
            console.error('Error adding customer to route:', err)
            throw err
        } finally {
            setLoading(false)
        }
    }

    const removeItem = async (itemId: number): Promise<void> => {
        setLoading(true)
        setError(null)
        try {
            await collectionApi.removeItemFromRoute(itemId)
            toast.success(t('customerRemovedSuccess'))
        } catch (err) {
            const message = err instanceof Error ? err.message : t('removeCustomerError')
            setError(message)
            toast.error(t('removeCustomerError'))
            console.error('Error removing customer from route:', err)
            throw err
        } finally {
            setLoading(false)
        }
    }

    const reorderItems = async (
        routeId: number,
        itemIds: number[],
    ): Promise<CollectionRoute> => {
        setLoading(true)
        setError(null)
        try {
            const updatedRoute = await collectionApi.reorderItems(routeId, {
                itemIds,
                autoOptimize: false,
            })
            toast.success(t('itemsReorderedSuccess'))
            return updatedRoute
        } catch (err) {
            const message = err instanceof Error ? err.message : t('reorderItemsError')
            setError(message)
            toast.error(t('reorderItemsError'))
            console.error('Error reordering items:', err)
            throw err
        } finally {
            setLoading(false)
        }
    }

    const updateItemStatus = async (
        itemId: number,
        data: CollectionRouteItemStatusUpdateRequest,
    ): Promise<CollectionRouteItem> => {
        setLoading(true)
        setError(null)
        try {
            const updatedItem = await collectionApi.updateItemStatus(itemId, data)
            toast.success(t('itemStatusUpdatedSuccess'))
            return updatedItem
        } catch (err) {
            const message = err instanceof Error ? err.message : t('updateItemStatusError')
            setError(message)
            toast.error(t('updateItemStatusError'))
            console.error('Error updating item status:', err)
            throw err
        } finally {
            setLoading(false)
        }
    }

    const clearError = () => setError(null)

    return {
        loading,
        error,
        addCustomer,
        removeItem,
        reorderItems,
        updateItemStatus,
        clearError,
    }
}

