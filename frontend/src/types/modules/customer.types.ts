import type { PaginationParams } from '@/types/common.types'

// ============================================================
// Customer Module — TypeScript Types
//
// Maps to Spring Boot DTOs:
//   • CustomerSummary                →  Customer
//   • CustomerResponse              →  CustomerResponse
//   • CustomerWithContarctsResponse  →  CustomerDetails
//   • CustomerRequest               →  CustomerRequest
//   • CustomerAccountLinkResponse   →  CustomerAccountLink
//   • CustomerRelationshipType enum →  CustomerRelationshipType
// ============================================================

// ────────────────────────────────────────────────────────────
// 1. CustomerRelationshipType (mirrors Java enum)
// ────────────────────────────────────────────────────────────

/**
 * Types of relationships between linked customer accounts.
 *
 * Maps to the Spring Boot `CustomerRelationshipType` enum.
 */
export const CustomerRelationshipType = {
    SAME_PERSON: 'SAME_PERSON',
    FAMILY_MEMBER: 'FAMILY_MEMBER',
    BUSINESS_PARTNER: 'BUSINESS_PARTNER',
    GUARANTOR: 'GUARANTOR',
    OTHER: 'OTHER',
} as const
export type CustomerRelationshipType = (typeof CustomerRelationshipType)[keyof typeof CustomerRelationshipType]

// ────────────────────────────────────────────────────────────
// 2. Customer (mirrors CustomerSummary DTO — used in list views)
// ────────────────────────────────────────────────────────────

/**
 * Customer summary returned by `GET /customers` (paginated list).
 *
 * Maps to the Spring Boot `CustomerSummary` DTO.
 *
 * @example
 * ```ts
 * const page = await api.get<PaginatedResponse<Customer>>('/customers')
 * ```
 */
export interface Customer {
    /**
     * Unique customer ID.
     *
     * it maps here. Without it, list → detail navigation won't work.
     */
    id: number
    /** Customer name */
    name: string
    /** Phone number */
    phone: string
    /** Address */
    address: string
    /** National ID */
    nationalId: string
    active: boolean

    /** Optional notes */
    notes: string | null
    /** Creation timestamp (ISO string) */
    createdAt: string
}

// ────────────────────────────────────────────────────────────
// 3. CustomerResponse (mirrors CustomerResponse DTO — single view)
// ────────────────────────────────────────────────────────────

/**
 * Full customer info returned by `GET /customers/{id}`.
 *
 * Includes `active` status and `updatedAt` beyond the summary.
 */
export interface CustomerResponse {

    id: number
    /** Customer name */
    name: string
    /** Phone number */
    phone: string
    /** Address */
    address: string
    /** National ID */
    nationalId: string
    /** Optional notes */
    notes: string | null
    /** Whether the customer is active */
    active: boolean
    /** Creation timestamp (ISO) */
    createdAt: string
    /** Last update timestamp (ISO) */
    updatedAt: string
}

// ────────────────────────────────────────────────────────────
// 4. CustomerAccountLink (mirrors CustomerAccountLinkResponse DTO)
// ────────────────────────────────────────────────────────────

/**
 * Linked customer account info.
 *
 * Returned by `GET /customers/{id}/linked-accounts`.
 */
export interface CustomerAccountLink {
   /** Unique link ID */
    id: number
    /** Name of the source customer */
    // customerName: string
    /** Name of the linked customer */
    linkedCustomerName: string
    /** Phone number */
    phone: string
    /** Type of relationship */
    relationshipType: CustomerRelationshipType
    /** Optional description of the relationship */
    relationshipDescription: string | null
    /** Whether this linked customer is active */
    active: boolean
    /** Who created this link */
    // createdBy: string | null
    /** Creation timestamp (ISO) */
    createdAt: string
    /** Last update timestamp (ISO) */
    // updatedAt: string
}

// ────────────────────────────────────────────────────────────
// 5. CustomerContract (mirrors ContractResponse for customer view)
// ────────────────────────────────────────────────────────────

/**
 * Contract info embedded in the customer detail view.
 *
 * Maps to ContractResponse DTO when fetched via CustomerWithContractsResponse.
 */
export interface CustomerContract {
    /** Contract number (unique identifier) */
    contractNumber: string
    /** Contract status */
    status: 'ACTIVE' | 'COMPLETED' | 'LATE' | 'CANCELLED'
    /** Product name */
    productName: string
    /** Vendor name */
    vendorName: string
    /** Partner name (if any) */
    partnerName: string | null
    /** Final price after all adjustments */
    finalPrice: number
    /** Down payment amount */
    downPayment: number
    /** Remaining amount to be paid */
    remainingAmount: number
    /** Number of months for installment */
    months: number
    /** Monthly payment amount */
    monthlyAmount: number
    /** Contract start date */
    startDate: string
    /** Contract completion date (if completed) */
    completionDate: string | null
    /** Creation timestamp */
    createdAt: string
    /** Notes */
    notes: string | null
}

// ────────────────────────────────────────────────────────────
// 6. CustomerDetails (mirrors CustomerWithContarctsResponse DTO)
// ────────────────────────────────────────────────────────────

/**
 * Full customer detail returned by `GET /customers/{id}/with-contracts`.
 *
 * Includes the customer ID, all base fields, active status,
 * and a list of associated contracts.
 */
export interface CustomerDetails {
    /** Unique customer ID */
    id: number
    /** Customer name */
    name: string
    /** Phone number */
    phone: string
    /** Address */
    address: string
    /** National ID */
    nationalId: string
    /** Optional notes */
    notes: string | null
    /** Whether the customer is active */
    active: boolean
    /** Creation timestamp (ISO) */
    createdAt: string
    /** Last update timestamp (ISO) */
    updatedAt: string
        /** Who created this customer record */
    createdBy: string | null
    /** Associated contracts */
    contracts: CustomerContract[]
}

// ────────────────────────────────────────────────────────────
// 7. CustomerRequest (mirrors CustomerRequest DTO)
// ────────────────────────────────────────────────────────────

/**
 * Payload sent to `POST /customers` or `PUT /customers/:id`.
 *
 * Maps to the Spring Boot `CustomerRequest` DTO.
 *
 * @example
 * ```ts
 * const payload: CustomerRequest = {
 *     name: 'Ahmed Ali',
 *     phone: '+9647801234567',
 *     address: '123 Baghdad St',
 *     nationalId: '12345678901234',
 * }
 * await api.post<CustomerResponse>('/customers', payload)
 * ```
 */
export interface CustomerRequest {
    /** Customer name (4–50 characters) */
    name: string
    /** Phone number (10–15 digits, optional leading +) */
    phone: string
    /** Address (max 100 characters) */
    address: string
    /** National ID (6–14 digits only) */
    nationalId: string
    /** Optional free-text notes (max 500 characters) */
    notes?: string
}

// ────────────────────────────────────────────────────────────
// 8. CustomerFilters (extends PaginationParams)
// ────────────────────────────────────────────────────────────

/**
 * Query parameters for the paginated customer list endpoint.
 *
 * @example
 * ```ts
 * const filters: CustomerFilters = {
 *     page: 0,
 *     size: 10,
 *     search: 'Ahmed',
 * }
 * ```
 */
export interface CustomerFilters extends PaginationParams {
    /** Free-text search (matched against name, phone, nationalId) */
    search?: string
    status?: 'active' | 'inactive' | 'all'}

// ────────────────────────────────────────────────────────────
// 9. LinkCustomerRequest — for linking accounts
// ────────────────────────────────────────────────────────────

/**
 * Params for `POST /customers/{customerId}/link/{linkedCustomerId}`.
 */
export interface LinkCustomerRequest {
    /** Type of relationship between the two customers */
    relationshipType: CustomerRelationshipType
    /** Optional description */
    description?: string
}

// ────────────────────────────────────────────────────────────
// 10. CustomerStats — stats endpoint response
// ────────────────────────────────────────────────────────────

/**
 * Response from `GET /customers/stats/count`.
 *
 * Backend returns a Map<String, Long> with localized keys.
 * We use Record<string, number> on the frontend.
 */
export type CustomerStats = Record<string, number>

