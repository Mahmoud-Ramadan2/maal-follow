import type { PaginationParams } from '@/types/common.types'

// ============================================================
// Vendor Module — TypeScript Types
//
// Maps to Spring Boot DTOs:
//   • VendorSummary   →  Vendor
//   • VendorRequest   →  VendorRequest
//   • VendorResponse  →  VendorDetails (with purchases)
// ============================================================

// ────────────────────────────────────────────────────────────
// 1. VendorPurchase (mirrors PurchaseDTO embedded in VendorResponse)
// ────────────────────────────────────────────────────────────

/** Lightweight purchase info returned inside a vendor detail response. */
export interface VendorPurchase {
    /** Unique purchase ID */
    id: number
    /** Name of the purchased product */
    productName: string
    /** Purchase price (BigDecimal → number) */
    buyPrice: number
}

// ────────────────────────────────────────────────────────────
// 2. Vendor (mirrors VendorSummary DTO — used in list views)
// ────────────────────────────────────────────────────────────

/**
 * Vendor summary returned by `GET /vendors` (paginated list).
 *
 * Maps to the Spring Boot `VendorSummary` DTO.
 *
 * @example
 * ```ts
 * const page = await api.get<PaginatedResponse<Vendor>>('/vendors')
 * page.content.forEach(v => console.log(v.name))
 * ```
 */
export interface Vendor {
    /** Unique vendor ID */
    id: number
    /** Vendor name */
    name: string
    /** Phone number */
    phone: string
    /** Address */
    address: string
    /** Optional notes */
    notes: string | null
    /** Whether the vendor is active */
    active: boolean
}

// ────────────────────────────────────────────────────────────
// 3. VendorDetails (mirrors VendorResponse DTO — single view)
// ────────────────────────────────────────────────────────────

/**
 * Full vendor detail returned by `GET /vendors/{id}`.
 *
 * May include related purchases or additional fields
 * beyond what the summary provides.
 */
export interface VendorDetails extends Vendor {
    /** Purchases associated with this vendor */
    purchases?: VendorPurchase[]
}

// ────────────────────────────────────────────────────────────
// 4. VendorRequest (mirrors VendorRequest DTO)
// ────────────────────────────────────────────────────────────

/**
 * Payload sent to `POST /vendors` or `PATCH /vendors/:id`.
 *
 * Maps to the Spring Boot `VendorRequest` DTO.
 *
 * @example
 * ```ts
 * const payload: VendorRequest = {
 *     name: 'TechVendor Inc.',
 *     phone: '+1234567890',
 *     address: '123 Main St',
 * }
 * await api.post<Vendor>('/vendors', payload)
 * ```
 */
export interface VendorRequest {
    /** Vendor name (4–50 characters) */
    name: string
    /** Phone number (10–15 digits, optional leading +) */
    phone: string
    /** Address (max 100 characters) */
    address: string
    /** Optional free-text notes (max 500 characters) */
    notes?: string
}

// ────────────────────────────────────────────────────────────
// 5. VendorFilters (extends PaginationParams)
// ────────────────────────────────────────────────────────────

/**
 * Query parameters for the paginated vendor list endpoint.
 *
 * Extends `PaginationParams` (page, size, sort) with
 * vendor-specific filter fields.
 *
 * @example
 * ```ts
 * const filters: VendorFilters = {
 *     page: 0,
 *     size: 10,
 *     search: 'Tech',
 * }
 * const page = await api.get<PaginatedResponse<Vendor>>('/vendors', {
 *     params: filters,
 * })
 * ```
 */
export interface VendorFilters extends PaginationParams {
    /** Free-text search (matched against vendor name, phone, address) */
    search?: string
    /** Filter by active status: 'active' | 'inactive' | 'all' */
    status?: 'active' | 'inactive' | 'all'
}

