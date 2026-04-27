import type { PaginationParams } from '@/types/common.types'

// ============================================================
// Collection Module — TypeScript Types
//
// Maps to Spring Boot DTOs & Enums:
//   • CollectionRoute              →  CollectionRoute Entity
//   • CollectionRouteItem          →  CollectionRouteItem Entity
//   • CollectionRouteRequest       →  Create/Update DTO
//   • Enums: RouteType, RouteStatus, CollectionStatus
// ============================================================

// ────────────────────────────────────────────────────────────
// 1. Enums (const objects)
// ────────────────────────────────────────────────────────────

/**
 * Enum representing types of collection routes.
 * Determines how customers are organized in the route.
 */
export const RouteType = {
    BY_ADDRESS: 'BY_ADDRESS',      // Organize by physical address/location
    BY_DATE: 'BY_DATE',            // Organize by payment due date
    BY_COLLECTOR: 'BY_COLLECTOR',  // Organize by assigned collector
    CUSTOM: 'CUSTOM',              // Manual custom order
} as const
export type RouteType = (typeof RouteType)[keyof typeof RouteType]

/**
 * Enum representing the lifecycle status of a collection route.
 */
export const RouteStatus = {
    DRAFT: 'DRAFT',                // Being created/edited, not yet active
    ACTIVE: 'ACTIVE',              // Published for field ops
    IN_PROGRESS: 'IN_PROGRESS',    // Currently being worked on
    COMPLETED: 'COMPLETED',        // All items have been processed
    CANCELLED: 'CANCELLED',        // Deactivated or cancelled
} as const
export type RouteStatus = (typeof RouteStatus)[keyof typeof RouteStatus]

/**
 * Enum representing the collection status of an individual route item.
 * Tracks the outcome of collection attempts for each customer.
 */
export const CollectionStatus = {
    PENDING: 'PENDING',            // Awaiting collection attempt
    COLLECTED: 'COLLECTED',        // Successfully collected
    FAILED: 'FAILED',              // Collection attempt failed
    SKIPPED: 'SKIPPED',            // Intentionally skipped
    RESCHEDULED: 'RESCHEDULED',   // Moved to another route
} as const
export type CollectionStatus = (typeof CollectionStatus)[keyof typeof CollectionStatus]

// ────────────────────────────────────────────────────────────
// 2. CollectionRoute (Parent Entity)
// ────────────────────────────────────────────────────────────

/**
 * CollectionRoute represents an organized group of customers
 * targeted for unpaid account collection in a field operation.
 *
 * Maps to the Spring Boot `CollectionRoute` entity.
 *
 * @example
 * ```ts
 * const route = await collectionApi.getById(1)
 * console.log(route.name)        // "Cairo Zone A - Monday"
 * console.log(route.routeType)   // "BY_ADDRESS"
 * console.log(route.routeItems.length)  // 12 customers
 * console.log(route.collectionPercentage)  // 67% collected
 * ```
 */
export interface CollectionRoute {
    /** Unique route ID */
    id: number

    /** Route name (e.g., "Cairo Zone A - Monday") */
    name: string

    /** Optional detailed description */
    description?: string

    /** Strategy used to organize customers in this route */
    routeType: RouteType

    /** Current lifecycle status of the route */
    routeStatus: RouteStatus

    /** Total outstanding amount across all customers in route */
    totalOutstanding: number

    /** Total amount successfully collected from this route */
    totalCollected: number

    /** Percentage of route that has been collected (0-100) */
    collectionPercentage: number

    /** Estimated duration in minutes to complete the entire route (Phase 2) */
    estimatedDurationMinutes?: number

    /** Whether route is active (soft delete support) */
    isActive: boolean

    /** User who created this route */
    createdBy: {
        id: number
        name: string
    }

    /** ISO 8601 timestamp of creation */
    createdAt: string

    /** ISO 8601 timestamp of last update */
    updatedAt: string

    /** List of customers assigned to this route */
    routeItems: CollectionRouteItem[]
}

// ────────────────────────────────────────────────────────────
// 3. CollectionRouteItem (Child Entity)
// ────────────────────────────────────────────────────────────

/**
 * CollectionRouteItem represents a single customer assigned to a route.
 * Tracks the customer's position in the route sequence and collection outcome.
 *
 * Maps to the Spring Boot `CollectionRouteItem` entity.
 *
 * @example
 * ```ts
 * const item = route.routeItems[0]
 * console.log(item.customer.name)  // "Ahmed Ali"
 * console.log(item.sequenceOrder)  // 1 (first in route)
 * console.log(item.collectionStatus)  // "PENDING"
 * ```
 */
export interface CollectionRouteItem {
    /** Unique item ID */
    id: number

    /** Reference to the parent collection route */
    collectionRoute?: {
        id: number
        name: string
    }

    /** Customer assigned to this route position */
    customer: {
        id: number
        name: string
        phone: string
        address: string
        outstandingAmount?: number  // Optional: cached outstanding balance
    }

    /** Order in which this customer appears in the route (1-indexed) */
    sequenceOrder: number

    /** Estimated time (HH:MM format) when collector should reach this customer (Phase 2) */
    estimatedCollectionTime?: string

    /** Current collection outcome for this customer */
    collectionStatus: CollectionStatus

    /** Amount actually collected from this customer (if COLLECTED) */
    collectedAmount?: number

    /** Collector's notes about this collection attempt */
    collectionNotes?: string

    /** Additional notes when adding customer to route */
    notes?: string

    /** Whether this item is active (soft delete support) */
    isActive: boolean

    /** ISO 8601 timestamp of when item was added to route */
    createdAt: string
}

// ────────────────────────────────────────────────────────────
// 4. Request/Response DTOs
// ────────────────────────────────────────────────────────────

/**
 * Request DTO for creating a new collection route.
 * Maps to the Spring Boot `CollectionRouteRequest` DTO.
 */
export interface CollectionRouteRequest {
    /** Route name */
    name: string

    /** Optional description */
    description?: string

    /** Strategy for organizing customers */
    routeType: RouteType

    /** Optional: list of customer IDs to add to route initially */
    customerIds?: number[]

    /** Optional: assign to specific collector */
    collectorAssignedTo?: number
}

/**
 * Request DTO for updating an existing collection route.
 */
export interface CollectionRouteUpdateRequest {
    /** Route name */
    name?: string

    /** Optional description */
    description?: string

    /** Optional route type update */
    routeType?: RouteType

    /** Current status of the route */
    routeStatus?: RouteStatus
}

/**
 * Request DTO for adding a customer to a route.
 * Maps to body of POST /collection-routes/{routeId}/customers/{customerId}
 */
export interface CollectionRouteItemAddRequest {
    /** Position in the route sequence (optional; auto-assigned if not provided) */
    sequenceOrder?: number

    /** Collector notes when adding this customer */
    notes?: string
}

/**
 * Request DTO for updating a route item's collection status.
 * Maps to PATCH /collection-routes/items/{itemId}/status
 */
export interface CollectionRouteItemStatusUpdateRequest {
    /** New collection status */
    status: CollectionStatus

    /** Collector notes (amount, reason for failure, etc.) */
    notes?: string

    /** Amount collected (if status is COLLECTED) */
    collectedAmount?: number
}

/**
 * Request DTO for batch reordering items within a route.
 * Maps to POST /collection-routes/{routeId}/items/reorder
 */
export interface CollectionRouteReorderRequest {
    /** Array of item IDs in desired sequence order */
    itemIds: number[]

    /** Optional: trigger automatic re-optimization by route type strategy */
    autoOptimize?: boolean
}

/**
 * Request DTO for searching uncollected customers.
 * Maps to POST /collection-routes/search-uncollected
 */
export interface CollectionSearchFilters {
    /** Free text search for customer name/phone/address */
    search?: string

    /** Filter by location/address */
    address?: string

    /** Minimum outstanding amount threshold */
    minOutstandingAmount?: number

    /** Maximum outstanding amount threshold */
    maxOutstandingAmount?: number

    /** Filter to only customers assigned to specific collector */
    collectorAssignedTo?: number

    /** Only include customers not attempted since this date */
    lastCollectionAttempt?: string

    /** Include only active customers */
    isActive?: boolean

    /** Pagination parameters */
    page?: number
    size?: number
}

/**
 * Response DTO for uncollected customer search results.
 * Lightweight summary used in route creation wizard.
 */
export interface CustomerUnpaidSummary {
    /** Customer ID */
    id: number

    /** Customer name */
    name: string

    /** Customer phone number */
    phone: string

    /** Customer address */
    address: string

    /** Total outstanding balance */
    outstandingAmount: number

    /** Date of last successful collection (if any) */
    lastPaymentDate?: string

    /** Number of days the account is overdue */
    daysOverdue?: number

    /** Currently assigned collector (if any) */
    assignedCollector?: {
        id: number
        name: string
    }
}

// ────────────────────────────────────────────────────────────
// 5. Query/Pagination Types
// ────────────────────────────────────────────────────────────

/**
 * Parameters for fetching collection routes with pagination.
 */
export interface CollectionRouteQueryParams extends PaginationParams {
    /** Filter by route type */
    routeType?: RouteType

    /** Filter by route status */
    routeStatus?: RouteStatus

    /** Filter by active status */
    isActive?: boolean

    /** Search by route name (partial match) */
    search?: string
}

/**
 * Parameters for fetching route items with filtering.
 */
export interface CollectionRouteItemQueryParams {
    /** Filter by collection status */
    collectionStatus?: CollectionStatus

    /** Sort by sequence order */
    sortBySequence?: 'ASC' | 'DESC'
}

// ────────────────────────────────────────────────────────────
// 6. UI State Types (For component logic)
// ────────────────────────────────────────────────────────────

/**
 * State for CollectionRouteCreatePage wizard.
 * Tracks multi-step form progress.
 */
export interface CollectionRouteCreateState {
    /** Step 1: Route metadata */
    metadata: {
        name: string
        description: string
        routeType: RouteType
        collectorAssignedTo?: number
    }

    /** Step 2: Selected customers */
    selectedCustomerIds: number[]

    /** Step 3: Auto-arranged sequence */
    arrangedSequence: CollectionRouteItem[]

    /** Current step in wizard (1-4) */
    currentStep: 1 | 2 | 3 | 4

    /** Whether wizard is in review/confirmation mode */
    isReviewing: boolean
}

/**
 * State for viewing and editing route items.
 */
export interface CollectionRouteViewState {
    /** Currently open item for editing (null if no modal open) */
    editingItemId: number | null

    /** Sort/filter options for items table */
    itemsFilter: {
        collectionStatus?: CollectionStatus
        sortBy: 'SEQUENCE' | 'COLLECTION_STATUS'
        sortOrder: 'ASC' | 'DESC'
    }

    /** Whether items are in reordering mode */
    isReordering: boolean
}

/**
 * Metrics summary for a collection route.
 * Computed from route items.
 */
export interface CollectionRouteMetrics {
    /** Total customers in route */
    totalCustomers: number

    /** Number of customers marked as COLLECTED */
    collectedCount: number

    /** Number of customers marked as FAILED */
    failedCount: number

    /** Number of customers marked as SKIPPED */
    skippedCount: number

    /** Number of customers still PENDING */
    pendingCount: number

    /** Percentage of route completed (collected + skipped) / total * 100 */
    completionPercentage: number

    /** Percentage of amount collected vs total outstanding */
    collectionPercentage: number

    /** Total outstanding amount */
    totalOutstanding: number

    /** Total collected amount */
    totalCollected: number

    /** Estimated time remaining (Phase 2 feature) */
    estimatedTimeRemaining?: number
}

