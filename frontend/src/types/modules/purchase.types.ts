import type { PaginationParams } from '@/types/common.types'

// ============================================================
// Purchase Module — TypeScript Types
//
// Maps to Spring Boot DTOs:
//   • PurchaseResponse  →  Purchase
//   • PurchaseRequest   →  PurchaseRequest
// ============================================================

export interface Purchase {
    /** Unique purchase ID */
    id: number
    /** Name of the purchased product */
    productName: string
    /** Purchase price (BigDecimal → number) */
    buyPrice: number
    /** Date of purchase (ISO `YYYY-MM-DD`) */
    purchaseDate: string
    /** Timestamp when the record was created (ISO `YYYY-MM-DDTHH:mm:ss`) */
    createdAt: string
    /** Optional notes attached to the purchase */
    notes: string | null
    /** Resolved vendor name (from the Vendor entity) */
    vendorName: string
}

// ────────────────────────────────────────────────────────────
// 2. PurchaseRequest (mirrors PurchaseRequest DTO)
// ────────────────────────────────────────────────────────────

/**
 * Payload sent to `POST /purchases` or `PUT /purchases/:id`.
 *
 * Maps to the Spring Boot `PurchaseRequest` DTO.
 *
 * @example
 * ```ts
 * const payload: PurchaseRequest = {
 *     vendorId: 3,
 *     productName: 'Laptop',
 *     buyPrice: 1200.00,
 *     purchaseDate: '2026-02-26',
 * }
 * await api.post<ApiResponse<Purchase>>('/purchases', payload)
 * ```
 */
export interface PurchaseRequest {
    /** FK → Vendor entity */
    vendorId: number
    /** Name of the product being purchased */
    productName: string
    /** Purchase price (BigDecimal → number) */
    buyPrice: number
    /** Date of purchase (ISO `YYYY-MM-DD`) */
    purchaseDate: string
    /** Optional free-text notes */
    notes?: string
}

// ────────────────────────────────────────────────────────────
// 3. PurchaseFilters (extends PaginationParams)
// ────────────────────────────────────────────────────────────

/**
 * Query parameters for the paginated purchase list endpoint.
 *
 * Extends `PaginationParams` (page, size, sort) with
 * purchase-specific filter fields.
 *
 * @example
 * ```ts
 * const filters: PurchaseFilters = {
 *     page: 0,
 *     size: 10,
 *     sort: 'purchaseDate,desc',
 *     vendorId: 3,
 *     startDate: '2026-01-01',
 *     endDate: '2026-12-31',
 *     searchTerm: 'Laptop',
 * }
 * const page = await api.get<PaginatedResponse<Purchase>>('/purchases', {
 *     params: filters,
 * })
 * ```
 */
export interface PurchaseFilters extends PaginationParams {
    /** Filter by vendor ID */
    vendorId?: number
    /** Filter purchases from this date (ISO `YYYY-MM-DD`) */
    startDate?: string
    /** Filter purchases up to this date (ISO `YYYY-MM-DD`) */
    endDate?: string
    /** Free-text search (matched against product name, vendor name, notes, price) */
    searchTerm?: string
}

// ────────────────────────────────────────────────────────────
// 4. PurchaseStatistics
// ────────────────────────────────────────────────────────────

/**
 * Aggregate statistics returned by `GET /purchases/statistics`.
 * The backend should return amountByVendor and avgAmount.
 */
export interface PurchaseStatistics {
    totalCount: number
    totalAmount: number
    avgAmount: number
    countByVendor: Record<string, number>
    amountByVendor: Record<string, number>
}

