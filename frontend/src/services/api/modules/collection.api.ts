/**
 * Collection Module — API Service
 *
 * Provides typed API calls for collection route management.
 * All endpoints follow RESTful conventions under /api/v1/collection-routes
 *
 * Usage:
 *   import { collectionApi } from '@services/api/modules/collection.api'
 *   const routes = await collectionApi.getActiveRoutes(0, 10)
 */

import { api } from '@services/api'
import type {
    CollectionRoute,
    CollectionRouteItem,
    CollectionRouteRequest,
    CollectionRouteUpdateRequest,
    CollectionRouteItemAddRequest,
    CollectionRouteItemStatusUpdateRequest,
    CollectionRouteReorderRequest,
    CollectionSearchFilters,
    CustomerUnpaidSummary,
    RouteType,
    RouteStatus,
} from '@/types/modules/collection.types'
import type { PaginatedResponse } from '@/types/common.types'

const BASE = '/collection-routes'

/**
 * Collection API service object.
 * Bundles all HTTP calls for the collection module.
 */
export const collectionApi = {
    // ────────────────────────────────────────────────────────────
    // Route List Operations
    // ────────────────────────────────────────────────────────────

    /**
     * Fetch all active collection routes (paginated).
     *
     * GET /api/v1/collection-routes?page=&size=
     *
     * @param page Zero-indexed page number (default: 0)
     * @param size Items per page (default: 10)
     * @returns Paginated collection routes
     */
    async getActiveRoutes(): Promise<CollectionRoute[]> {
        const routes = await api.get<CollectionRoute[]>(BASE)
        return routes.map(route => this.normalizeRoute(route))
    },

    // ────────────────────────────────────────────────────────────
    // Route Detail Operations
    // ────────────────────────────────────────────────────────────

    /**
     * Fetch a single collection route by ID (includes all items).
     *
     * GET /api/v1/collection-routes/{routeId}
     *
     * @param routeId The ID of the collection route
     * @returns Full route object with nested items
     */
    async getById(routeId: number): Promise<CollectionRoute> {
        const route = await api.get<CollectionRoute>(`${BASE}/${routeId}`)
        return this.normalizeRoute(route)
    },

    /**
     * Create a new collection route.
     *
     * POST /api/v1/collection-routes
     *
     * @param data Route creation request (name, description, type, customer IDs)
     * @returns Newly created route (will be in DRAFT status)
     *
     * @example
     * ```ts
     * const newRoute = await collectionApi.create({
     *   name: 'Cairo Zone A',
     *   routeType: 'BY_ADDRESS',
     *   customerIds: [1, 2, 3, 5]
     * })
     * ```
     */
    async create(data: CollectionRouteRequest): Promise<CollectionRoute> {
        const created = await api.post<CollectionRoute>(BASE, undefined, {
            params: {
                name: data.name,
                description: data.description,
                routeType: data.routeType,
            },
        })
        return this.normalizeRoute(created)
    },

    async update(routeId: number, data: CollectionRouteUpdateRequest): Promise<CollectionRoute> {
        const updated = await api.put<CollectionRoute>(`${BASE}/${routeId}`, data)
        return this.normalizeRoute(updated)
    },

    /**
     * Deactivate a collection route.
     * Cascades to deactivate all route items.
     *
     * PUT /api/v1/collection-routes/{routeId}/deactivate
     *
     * @param routeId Route ID to deactivate
     */
    async deactivateRoute(routeId: number): Promise<void> {
        return api.put<void>(`${BASE}/${routeId}/deactivate`)
    },

    // ────────────────────────────────────────────────────────────
    // Route Items Operations
    // ────────────────────────────────────────────────────────────

    /**
     * Fetch all items in a collection route.
     *
     * GET /api/v1/collection-routes/{routeId}/items
     *
     * @param routeId The parent route ID
     * @returns Array of route items
     */
    async getRouteItems(routeId: number): Promise<CollectionRouteItem[]> {
        const items = await api.get<CollectionRouteItem[]>(`${BASE}/${routeId}/items`)
        return items.map(item => this.normalizeRouteItem(item))
    },

    /**
     * Add a customer to a collection route.
     * The customer will be appended to the end of the route sequence.
     *
     * POST /api/v1/collection-routes/{routeId}/customers/{customerId}
     *
     * @param routeId Parent route ID
     * @param customerId Customer ID to add
     * @param data Optional sequence order and notes
     * @returns Newly created route item
     *
     * @example
     * ```ts
     * const item = await collectionApi.addCustomerToRoute(
     *   5,      // route ID
     *   123,    // customer ID
     *   { sequenceOrder: 3, notes: 'Call ahead before visiting' }
     * )
     * ```
     */
    async addCustomerToRoute(
        routeId: number,
        customerId: number,
        data?: CollectionRouteItemAddRequest,
    ): Promise<CollectionRouteItem> {
        const item = await api.post<CollectionRouteItem>(
            `${BASE}/${routeId}/customers/${customerId}`,
            undefined,
            {
                params: {
                    sequenceOrder: data?.sequenceOrder,
                    notes: data?.notes,
                },
            },
        )
        return this.normalizeRouteItem(item)
    },

    /**
     * Remove a customer from a collection route.
     * Soft delete: marks item as inactive.
     *
     * DELETE /api/v1/collection-routes/items/{itemId}
     *
     * @param itemId Route item ID to remove
     */
    async removeItemFromRoute(itemId: number): Promise<void> {
        return api.del<void>(`${BASE}/items/${itemId}`)
    },

    // ────────────────────────────────────────────────────────────
    // Batch Operations
    // ────────────────────────────────────────────────────────────

    /**
     * Batch reorder items within a route.
     * Atomically updates the sequence order of multiple items.
     *
     * POST /api/v1/collection-routes/{routeId}/items/reorder
     *
     * @param routeId Parent route ID
     * @param data Reorder request (array of item IDs in new order, optional auto-optimize flag)
     * @returns Updated route with reordered items
     *
     * @example
     * ```ts
     * const updatedRoute = await collectionApi.reorderItems(5, {
     *   itemIds: [3, 1, 4, 2],  // new sequence
     *   autoOptimize: false
     * })
     * ```
     */
    async reorderItems(routeId: number, data: CollectionRouteReorderRequest): Promise<CollectionRoute> {
        const updated = await api.post<CollectionRoute>(`${BASE}/${routeId}/items/reorder`, data)
        return this.normalizeRoute(updated)
    },

    /**
     * Update the collection status of a route item.
     * Used to mark customers as COLLECTED, FAILED, SKIPPED, or RESCHEDULED.
     *
     * PATCH /api/v1/collection-routes/items/{itemId}/status
     *
     * @param itemId Route item ID to update
     * @param data Status update request (new status, optional notes, optional collected amount)
     * @returns Updated route item
     *
     * @example
     * ```ts
     * const item = await collectionApi.updateItemStatus(12, {
     *   status: 'COLLECTED',
     *   collectedAmount: 500,
     *   notes: 'Paid by cash, 50 discount applied'
     * })
     * ```
     */
    async updateItemStatus(itemId: number, data: CollectionRouteItemStatusUpdateRequest): Promise<CollectionRouteItem> {
        const updated = await api.patch<CollectionRouteItem>(`${BASE}/items/${itemId}/status`, data)
        return this.normalizeRouteItem(updated)
    },

    // ────────────────────────────────────────────────────────────
    // Search Operations
    // ────────────────────────────────────────────────────────────

    /**
     * Search for uncollected customers matching given filters.
     * Used in the route creation wizard to quickly find eligible customers.
     *
     * POST /api/v1/collection-routes/search-uncollected
     *
     * @param filters Search criteria (address, outstanding amount, collector, last attempt date, etc.)
     * @returns Paginated list of uncollected customer summaries
     *
     * @example
     * ```ts
     * const results = await collectionApi.searchUncollected({
     *   address: 'Cairo',
     *   minOutstandingAmount: 1000,
     *   page: 0,
     *   size: 20
     * })
     * ```
     */
    async searchUncollected(
        filters: CollectionSearchFilters = {},
    ): Promise<PaginatedResponse<CustomerUnpaidSummary>> {
        return api.post<PaginatedResponse<CustomerUnpaidSummary>>(`${BASE}/search-uncollected`, {
            searchTerm: filters.search,
            address: filters.address,
            page: filters.page,
            size: filters.size,
        })
    },

    normalizeRoute(route: Partial<CollectionRoute>): CollectionRoute {
        const isActive = route.isActive ?? true
        const routeItems = (route.routeItems ?? []).map(item => this.normalizeRouteItem(item))
        const totalCollected = route.totalCollected ?? 0
        const totalOutstanding = route.totalOutstanding ?? 0
        const collectionPercentage =
            totalOutstanding > 0 ? (totalCollected / totalOutstanding) * 100 : (route.collectionPercentage ?? 0)

        return {
            id: Number(route.id ?? 0),
            name: route.name ?? '',
            description: route.description,
            routeType: (route.routeType ?? 'CUSTOM') as RouteType,
            routeStatus: (route.routeStatus ?? (isActive ? 'ACTIVE' : 'CANCELLED')) as RouteStatus,
            totalOutstanding,
            totalCollected,
            collectionPercentage,
            estimatedDurationMinutes: route.estimatedDurationMinutes,
            isActive,
            createdBy: route.createdBy ?? { id: 0, name: '' },
            createdAt: route.createdAt ?? '',
            updatedAt: route.updatedAt ?? '',
            routeItems,
        }
    },

    normalizeRouteItem(item: Partial<CollectionRouteItem>): CollectionRouteItem {
        return {
            id: Number(item.id ?? 0),
            collectionRoute: item.collectionRoute,
            customer: {
                id: Number(item.customer?.id ?? 0),
                name: item.customer?.name ?? '',
                phone: item.customer?.phone ?? '',
                address: item.customer?.address ?? '',
                outstandingAmount: item.customer?.outstandingAmount,
            },
            sequenceOrder: item.sequenceOrder ?? 1,
            estimatedCollectionTime: item.estimatedCollectionTime,
            collectionStatus: item.collectionStatus ?? 'PENDING',
            collectedAmount: item.collectedAmount,
            collectionNotes: item.collectionNotes,
            notes: item.notes,
            isActive: item.isActive ?? true,
            createdAt: item.createdAt ?? '',
        }
    },
} as const

