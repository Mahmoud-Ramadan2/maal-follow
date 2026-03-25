// ============================================================
// Common Type Definitions
//
// Shared types used across the entire application.
// These mirror the response shapes returned by the
// Spring Boot backend.
// ============================================================

// ────────────────────────────────────────────────────────────
// 1. ApiResponse<T> — Generic API response wrapper
// ────────────────────────────────────────────────────────────

/**
 * Standard JSON envelope returned by every Spring Boot endpoint.
 *
 * The backend wraps all responses in this shape via a
 * `ResponseEntity` helper so the frontend can rely on a
 * consistent structure.
 *
 * @typeParam T - The type of the `data` payload.
 *
 * @example
 * // Backend returns:
 * // { "success": true, "message": "Purchase created", "data": { "id": 1, ... } }
 *
 * const res = await api.post<ApiResponse<Purchase>>('/purchases', payload)
 * if (res.success) {
 *     console.log(res.data)       // Purchase
 *     console.log(res.message)    // "Purchase created"
 * }
 *
 * @example
 * // Validation failure (422):
 * // { "success": false, "message": "Validation failed", "data": null,
 * //   "errors": { "amount": "must be greater than 0", "date": "is required" } }
 */
export interface ApiResponse<T = null> {
    /** Whether the operation succeeded */
    success: boolean
    /** Human-readable message from the backend */
    message: string
    /** The response payload — `null` on failure */
    data: T
    /**
     * Field-level validation errors.
     * Keys are field names, values are error messages.
     * Keys are field names, values are error messages.
     * Only present when `success` is `false`.
     */
    errors?: Record<string, string>
}

// ────────────────────────────────────────────────────────────
// 2. PaginatedResponse<T> — Spring Data Page response
// ────────────────────────────────────────────────────────────

/**
 * Mirrors Spring Data's `Page<T>` JSON serialization.
 *
 * When a Spring Boot controller returns `Page<Entity>`,
 * Jackson serializes it into this exact shape.
 *
 * @typeParam T - The entity type contained in the page.
 *
 * @example
 * const page = await api.get<PaginatedResponse<Purchase>>('/purchases', {
 *     params: { page: 0, size: 10, sort: 'createdAt,desc' }
 * })
 *
 * console.log(page.content)        // Purchase[]
 * console.log(page.totalElements)  // 42
 * console.log(page.totalPages)     // 5
 * console.log(page.number)         // 0  (current page, zero-based)
 * console.log(page.first)          // true
 * console.log(page.last)           // false
 */
export interface PaginatedResponse<T> {
    /** Array of items on the current page */
    content: T[]
    /** Total number of items across all pages */
    totalElements: number
    /** Total number of pages */
    totalPages: number
    /** Requested page size */
    size: number
    /** Current page index (zero-based, as Spring uses) */
    number: number
    /** `true` if this is the first page */
    first: boolean
    /** `true` if this is the last page */
    last: boolean
    /** `true` if `content` is empty */
    empty: boolean
}

// ────────────────────────────────────────────────────────────
// 3. PaginationParams — Request query parameters
// ────────────────────────────────────────────────────────────

/**
 * Query parameters sent to paginated Spring Boot endpoints.
 *
 * Maps to Spring's `Pageable` — the backend reads these from
 * the query string automatically.
 *
 * @example
 * const params: PaginationParams = {
 *     page: 0,
 *     size: 20,
 *     sort: 'createdAt,desc',
 * }
 * const page = await api.get<PaginatedResponse<Purchase>>('/purchases', { params })
 */
export interface PaginationParams {
    /**
     * Zero-based page index.
     * @default 0
     */
    page?: number
    /**
     * Number of items per page.
     * @default 10  (matches VITE_DEFAULT_PAGE_SIZE)
     */
    size?: number
    /**
     * Sort expression: `property,direction`.
     * Supports multiple fields separated by `&sort=`.
     *
     * @example 'createdAt,desc'
     * @example 'name,asc'
     */
    sort?: string
}

// ────────────────────────────────────────────────────────────
// 4. SelectOption — For dropdown / select components
// ────────────────────────────────────────────────────────────

/**
 * Generic option shape used by `<Select>`, `<Dropdown>`,
 * `<Autocomplete>`, and any other list-based UI component.
 *
 * @example
 * const statusOptions: SelectOption[] = [
 *     { label: 'Active',   value: 'ACTIVE' },
 *     { label: 'Inactive', value: 'INACTIVE' },
 * ]
 *
 * @example
 * // Numeric IDs (e.g. from a partner lookup endpoint)
 * const partners: SelectOption[] = data.map(p => ({
 *     label: p.name,
 *     value: p.id,
 * }))
 */
export interface SelectOption {
    /** Display text shown to the user */
    label: string
    /** Underlying value submitted to the backend */
    value: string | number
}

// ────────────────────────────────────────────────────────────
// 5. DateRange — For date-range pickers / filters
// ────────────────────────────────────────────────────────────

/**
 * Represents a date range for filtering queries.
 *
 * Dates are ISO-8601 strings (`YYYY-MM-DD`) to match
 * Spring Boot's `@DateTimeFormat(iso = ISO.DATE)`.
 *
 * @example
 * const range: DateRange = {
 *     startDate: '2026-01-01',
 *     endDate: '2026-01-31',
 * }
 * const purchases = await api.get<PaginatedResponse<Purchase>>('/purchases', {
 *     params: { ...pagination, ...range },
 * })
 */
export interface DateRange {
    /** Start date in ISO format (YYYY-MM-DD) */
    startDate: string
    /** End date in ISO format (YYYY-MM-DD) */
    endDate: string
}
