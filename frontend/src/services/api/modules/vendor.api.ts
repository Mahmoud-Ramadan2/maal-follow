import { api } from '@services/api'
import type { PaginatedResponse } from '@/types/common.types'
import type { Vendor, VendorDetails, VendorRequest, VendorFilters } from '@/types/modules/vendor.types'

// ────────────────────────────────────────────────────────────
// Endpoint base path
// ────────────────────────────────────────────────────────────
const BASE = '/vendors'

// ────────────────────────────────────────────────────────────
// API methods
// ────────────────────────────────────────────────────────────

export const vendorApi = {
    /**
     * Fetch a paginated, filtered list of vendors.
     *
     * Maps to `GET /api/vendors?page=0&size=10&search=Tech`
     *
     * @param filters - Optional pagination + search params.
     * @returns A Spring Data `Page<VendorSummary>` shape.
     *
     * @example
     * ```ts
     * const page = await vendorApi.getAll({ page: 0, size: 20, search: 'Tech' })
     * console.log(page.content)       // Vendor[]
     * console.log(page.totalElements) // 42
     * ```
     */
    async getAll(filters?: VendorFilters): Promise<PaginatedResponse<Vendor>> {
        return api.get<PaginatedResponse<Vendor>>(BASE, {
            params: filters,
        })
    },

    /**
     * Fetch a single vendor by ID (full details).
     *
     * Maps to `GET /api/vendors/{id}`
     *
     * @param id - Vendor primary key.
     * @returns The vendor detail record (may include purchases).
     *
     * @example
     * ```ts
     * const vendor = await vendorApi.getById(3)
     * ```
     */
    async getById(id: number): Promise<VendorDetails> {
        return api.get<VendorDetails>(`${BASE}/${id}`)
    },

    /**
     * Create a new vendor.
     *
     * Maps to `POST /api/vendors`
     *
     * @param data - Request payload matching `VendorRequest`.
     * @returns The newly created vendor summary.
     *
     * @example
     * ```ts
     * const created = await vendorApi.create({
     *     name: 'TechVendor Inc.',
     *     phone: '+1234567890',
     *     address: '123 Main St',
     * })
     * ```
     */
    async create(data: VendorRequest): Promise<Vendor> {
        return api.post<Vendor>(BASE, data)
    },

    /**
     * Partially update an existing vendor.
     *
     * Maps to `PATCH /api/vendors/{id}`
     *
     * @param id   - Vendor primary key.
     * @param data - Updated payload.
     * @returns The updated vendor summary.
     *
     * @example
     * ```ts
     * const updated = await vendorApi.update(3, {
     *     name: 'TechVendor Updated',
     *     phone: '+1234567890',
     *     address: '456 New St',
     * })
     * ```
     */
    async update(id: number, data: VendorRequest): Promise<Vendor> {
        return api.patch<Vendor>(`${BASE}/${id}`, data)
    },

    /**
     * Soft-delete a vendor.
     *
     * Maps to `DELETE /api/vendors/{id}`
     *
     * @param id - Vendor primary key.
     * @returns The response message string.
     *
     * @example
     * ```ts
     * await vendorApi.delete(3)
     * ```
     */
    async delete(id: number): Promise<string> {
        return api.del<string>(`${BASE}/${id}`)
    },
} as const

