import { api } from '@services/api'
import type { PaginatedResponse } from '@/types/common.types'
import type {
    Customer,
    CustomerResponse,
    CustomerDetails,
    CustomerRequest,
    CustomerFilters,
    CustomerAccountLink,
    CustomerRelationshipType,
    CustomerStats,
} from '@/types/modules/customer.types'
import type { Contract } from '@/types/modules/contract.types'

// ────────────────────────────────────────────────────────────
// Endpoint base path
// ────────────────────────────────────────────────────────────
const BASE = '/customers'

// ────────────────────────────────────────────────────────────
// API methods
// ────────────────────────────────────────────────────────────

export const customerApi = {
    // ── CRUD ────────────────────────────────────────────────

    /**
     * Fetch a paginated, filtered list of customers.
     *
     * Maps to `GET /api/v1/customers?pageable&search=Ahmed`
     */
    async getAll(filters?: CustomerFilters): Promise<PaginatedResponse<Customer>> {
        return api.get<PaginatedResponse<Customer>>(BASE, {
            params: filters,
        })
    },

    /**
     * Fetch a single customer by ID.
     *
     * Maps to `GET /api/v1/customers/{id}`
     */
    async getById(id: number): Promise<CustomerResponse> {
        return api.get<CustomerResponse>(`${BASE}/${id}`)
    },

    /**
     * Create a new customer.
     *
     * Maps to `POST /api/v1/customers`
     */
    async create(data: CustomerRequest): Promise<CustomerResponse> {
        return api.post<CustomerResponse>(BASE, data)
    },

    /**
     * Fully update an existing customer.
     *
     * Maps to `PUT /api/v1/customers/{id}`
     */
    async update(id: number, data: CustomerRequest): Promise<CustomerResponse> {
        return api.put<CustomerResponse>(`${BASE}/${id}`, data)
    },

    /**
     * Soft-delete a customer.
     *
     * Maps to `DELETE /api/v1/customers/{id}`
     */
    async delete(id: number): Promise<string> {
        return api.del<string>(`${BASE}/${id}`)
    },

    // ── Detail with contracts ───────────────────────────────

    /**
     * Fetch customer with all their contracts.
     *
     * Maps to `GET /api/v1/customers/{id}/with-contracts`
     */
    async getWithContracts(id: number): Promise<CustomerDetails> {
        return api.get<CustomerDetails>(`${BASE}/${id}/with-contracts`)
    },

    /**
     * Fetch paginated contracts for a customer.
     *
     * Maps to `GET /api/v1/customers/{id}/contracts?page=0&size=10`
     */
    async getCustomerContracts(
        customerId: number,
        page: number = 0,
        size: number = 10,
    ): Promise<PaginatedResponse<Contract>> {
        return api.get<PaginatedResponse<Contract>>(`${BASE}/${customerId}/contracts`, {
            params: { page, size },
        })
    },

    // ── Account Linking ─────────────────────────────────────

    /**
     * Link two customer accounts together.
     *
     * Maps to `POST /api/v1/customers/{customerId}/link/{linkedCustomerId}?relationshipType=...&description=...`
     */
    async linkAccounts(
        customerId: number,
        linkedCustomerId: number,
        relationshipType: CustomerRelationshipType,
        description?: string,
    ): Promise<void> {
        await api.post<void>(
            `${BASE}/${customerId}/link/${linkedCustomerId}`,
            null,
            { params: { relationshipType, description } },
        )
    },

    /**
     * Get all linked accounts for a customer.
     *
     * Maps to `GET /api/v1/customers/{customerId}/linked-accounts`
     */
    async getLinkedAccounts(customerId: number): Promise<CustomerAccountLink[]> {
        return api.get<CustomerAccountLink[]>(`${BASE}/${customerId}/linked-accounts`)
    },

    /**
     * Get linked accounts by relationship type.
     *
     * Maps to `GET /api/v1/customers/linked-accounts/by-relation-type?relationshipType=...`
     */
    async getLinkedAccountsByType(
        relationshipType: CustomerRelationshipType,
    ): Promise<CustomerAccountLink[]> {
        return api.get<CustomerAccountLink[]>(
            `${BASE}/linked-accounts/by-relation-type`,
            { params: { relationshipType } },
        )
    },

    // ── Statistics ───────────────────────────────────────────

    /**
     * Get active/inactive customer count statistics.
     *
     * Maps to `GET /api/v1/customers/stats/count`
     */
    async getStats(): Promise<CustomerStats> {
        return api.get<CustomerStats>(`${BASE}/stats/count`)
    },
} as const

