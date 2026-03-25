import { api } from '@services/api'
import type { PaginatedResponse } from '@/types/common.types'
import type { Purchase, PurchaseRequest, PurchaseFilters, PurchaseStatistics } from '@/types/modules/purchase.types'

// ────────────────────────────────────────────────────────────
// Endpoint base path
// ────────────────────────────────────────────────────────────
const BASE = '/purchases'

// ────────────────────────────────────────────────────────────
// Re-export PurchaseStatistics so callers can import from the
// api module OR from types — both paths work.
// ────────────────────────────────────────────────────────────
export type { PurchaseStatistics }

// ────────────────────────────────────────────────────────────
// API methods
// ────────────────────────────────────────────────────────────

export const purchaseApi = {
    /**
     * Fetch a paginated, sorted, filtered list of purchases.
     *
     * Maps to `GET /api/purchases?page=0&size=10&sort=purchaseDate,desc&vendorId=3`
     *
     * @param filters - Optional pagination + filter params.
     *                  All fields are optional; omitted fields
     *                  are ignored by Spring's query binding.
     * @returns A Spring Data `Page<Purchase>` shape.
     *
     * @example
     * ```ts
     * const page = await purchaseApi.getAll({
     *     page: 0, size: 20, sort: 'purchaseDate,desc',
     *     vendorId: 3, searchTerm: 'Laptop',
     * })
     * console.log(page.content)       // Purchase[]
     * console.log(page.totalElements) // 42
     * ```
     */
    async getAll(filters?: PurchaseFilters):
        Promise<PaginatedResponse<Purchase>> {
        return api.get<PaginatedResponse<Purchase>>(BASE, {
            params: filters,
        })
    },

    /**
     * Fetch a single purchase by ID.
     *
     * Maps to `GET /api/purchases/{id}`
     *
     * @param id - Purchase primary key.
     * @returns The purchase record.
     *
     * @example
     * ```ts
     * const purchase = await purchaseApi.getById(42)
     * ```
     */
    async getById(id: number): Promise<Purchase> {
        return await api.get<Purchase>(`${BASE}/${id}`)
    },

    /**
     * Create a new purchase.
     *
     * Maps to `POST /api/purchases`
     *
     * @param data - Request payload matching `PurchaseRequest`.
     * @returns The newly created purchase.
     *
     * @example
     * ```ts
     * const created = await purchaseApi.create({
     *     vendorId: 3,
     *     productName: 'Laptop',
     *     buyPrice: 1200,
     *     purchaseDate: '2026-02-26',
     * })
     * ```
     */
    async create(data: PurchaseRequest): Promise<Purchase> {
        const res = await api.post<Purchase>(BASE, data)
        return res
    },

    /**
     * Fully update an existing purchase.
     *
     * Maps to `PUT /api/purchases/{id}`
     *
     * @param id   - Purchase primary key.
     * @param data - Updated payload.
     * @returns The updated purchase.
     *
     * @example
     * ```ts
     * const updated = await purchaseApi.update(42, {
     *     vendorId: 3,
     *     productName: 'Laptop Pro',
     *     buyPrice: 1500,
     *     purchaseDate: '2026-02-26',
     * })
     * ```
     */
    async update(id: number, data: PurchaseRequest): Promise<Purchase> {
        const res = await api.put<Purchase>(`${BASE}/${id}`, data)
        // console.log("response from update API:", res)
        return res
    },

    /**
     * Delete a purchase.
     *
     * Maps to `DELETE /api/purchases/{id}`
     *
     * @param id - Purchase primary key.
     *
     * @example
     * ```ts
     * await purchaseApi.delete(42)
     * ```
     */
    async delete(id: number): Promise<void> {
        await api.del(`${BASE}/${id}`)
    },

    /**
     * Fetch aggregate purchase statistics.
     *
     * Maps to `GET /api/purchases/statistics`
     *
     * @returns Summary counts and totals.
     *
     * @example
     * ```ts
     * const stats = await purchaseApi.getStatistics()
     * console.log(stats.totalCount)  // 128
     * console.log(stats.totalAmount) // 256000
     * ```
     */
    async getStatistics(): Promise<PurchaseStatistics> {
        return api.get<PurchaseStatistics>(`${BASE}/statistics`)
    },
} as const
