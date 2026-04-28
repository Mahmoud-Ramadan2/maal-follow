import { api } from '@services/api'
import type { PaginatedResponse } from '@/types/common.types'
import type {
    Contract, ContractRequest, ContractStatus, ContractMetadataUpdateRequest,
} from '@/types/modules/contract.types'

const BASE = '/contracts'

export const contractApi = {
    /** POST /api/v1/contracts */
    async create(data: ContractRequest): Promise<Contract> {
        return api.post<Contract>(BASE, data)
    },

    /** PUT /api/v1/contracts/{id} */
    async update(id: number, data: ContractRequest): Promise<Contract> {
        return api.put<Contract>(`${BASE}/${id}`, data)
    },

    /** PATCH /api/v1/contracts/{id}/metadata */
    async updateMetadata(id: number, data: ContractMetadataUpdateRequest): Promise<Contract> {
        return api.patch<Contract>(`${BASE}/${id}/metadata`, data)
    },

    /** GET /api/v1/contracts/{id} */
    async getById(id: number): Promise<Contract> {
        return api.get<Contract>(`${BASE}/${id}`)
    },

    /** GET /api/v1/contracts/contract-number/{contractNumber} */
    async getByContractNumber(contractNumber: string): Promise<Contract> {
        return api.get<Contract>(`${BASE}/contract-number/${contractNumber}`)
    },

    /** GET /api/v1/contracts/by-status/{status}?page=&size= */
    async getByStatus(
        status: ContractStatus,
        page = 0,
        size = 20,
    ): Promise<PaginatedResponse<Contract>> {
        return api.get<PaginatedResponse<Contract>>(`${BASE}/by-status/${status}`, {
            params: { page, size },
        })
    },

    /** GET /api/v1/contracts/customer/{customerId}?page=&size= */
    async getCustomerContracts(
        customerId: number,
        page = 0,
        size = 20,
    ): Promise<PaginatedResponse<Contract>> {
        return api.get<PaginatedResponse<Contract>>(`${BASE}/customer/${customerId}`, {
            params: { page, size },
        })
    },

    /** GET /api/v1/contracts/customer/{customerId}/all-linked */
    async getAllLinkedCustomerContracts(customerId: number): Promise<Contract[]> {
        return api.get<Contract[]>(`${BASE}/customer/${customerId}/all-linked`)
    },

    /** GET /api/v1/contracts/by-payment-day/{day} */
    async getByPaymentDay(day: number): Promise<Contract[]> {
        return api.get<Contract[]>(`${BASE}/by-payment-day/${day}`)
    },

    /** GET /api/v1/contracts/by-address?address= */
    async getByAddress(address: string): Promise<Contract[]> {
        return api.get<Contract[]>(`${BASE}/by-address`, { params: { address } })
    },

    /** GET /api/v1/contracts/total-monthly-expected */
    async getTotalMonthlyExpected(): Promise<number> {
        return api.get<number>(`${BASE}/total-monthly-expected`)
    },

    /** GET /api/v1/contracts/total-net-profit */
    async getTotalNetProfit(): Promise<number> {
        return api.get<number>(`${BASE}/total-net-profit`)
    },

    /** GET /api/v1/contracts/{id}/early-payment-discount?remainingAmount= */
    async calculateEarlyPaymentDiscount(id: number, remainingAmount: number): Promise<number> {
        return api.get<number>(`${BASE}/${id}/early-payment-discount`, {
            params: { remainingAmount },
        })
    },

    /** GET /api/v1/contracts/{id}/cash-discount */
    async calculateCashDiscount(id: number): Promise<number> {
        return api.get<number>(`${BASE}/${id}/cash-discount`)
    },

    /** PUT /api/v1/contracts/{id}/complete */
    async markAsCompleted(id: number): Promise<Contract> {
        return api.put<Contract>(`${BASE}/${id}/complete`)
    },
} as const

