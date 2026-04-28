import { api } from '@services/api'
import type {
    ContractExpense, ContractExpenseRequest, ExpenseType,
} from '@/types/modules/contract.types'

const BASE = '/contract-expenses'

export const contractExpenseApi = {
    /** POST /api/v1/contract-expenses */
    async create(data: ContractExpenseRequest): Promise<ContractExpense> {
        return api.post<ContractExpense>(BASE, data)
    },

    /** PUT /api/v1/contract-expenses/{id} */
    async update(id: number, data: ContractExpenseRequest): Promise<ContractExpense> {
        return api.put<ContractExpense>(`${BASE}/${id}`, data)
    },

    /** GET /api/v1/contract-expenses/{id} */
    async getById(id: number): Promise<ContractExpense> {
        return api.get<ContractExpense>(`${BASE}/${id}`)
    },

    /** GET /api/v1/contract-expenses/contract/{contractId} */
    async getByContract(contractId: number): Promise<ContractExpense[]> {
        return api.get<ContractExpense[]>(`${BASE}/contract/${contractId}`)
    },

    /** GET /api/v1/contract-expenses/by-type/{type} */
    async getByType(type: ExpenseType): Promise<ContractExpense[]> {
        return api.get<ContractExpense[]>(`${BASE}/by-type/${type}`)
    },

    /** GET /api/v1/contract-expenses/date-range?startDate=&endDate= */
    async getByDateRange(startDate: string, endDate: string): Promise<ContractExpense[]> {
        return api.get<ContractExpense[]>(`${BASE}/date-range`, {
            params: { startDate, endDate },
        })
    },

    /** GET /api/v1/contract-expenses/contract/{contractId}/total */
    async getTotalForContract(contractId: number): Promise<number> {
        return api.get<number>(`${BASE}/contract/${contractId}/total`)
    },

    /** GET /api/v1/contract-expenses/partner/{partnerId}/total */
    async getTotalForPartner(partnerId: number): Promise<number> {
        return api.get<number>(`${BASE}/partner/${partnerId}/total`)
    },

    /** DELETE /api/v1/contract-expenses/{id} */
    async delete(id: number): Promise<void> {
        await api.del(`${BASE}/${id}`)
    },
} as const

