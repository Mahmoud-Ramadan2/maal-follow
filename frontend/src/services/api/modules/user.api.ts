import { api } from '@services/api'
import type { PaginatedResponse } from '@/types/common.types'
import type { AppUser, UserFilters, UserRequest, UserRole } from '@/types/modules/user.types'

const BASE = '/users'

export const userApi = {
    async getAll(filters?: UserFilters): Promise<PaginatedResponse<AppUser>> {
        return api.get<PaginatedResponse<AppUser>>(BASE, { params: filters })
    },

    async getById(id: number): Promise<AppUser> {
        return api.get<AppUser>(`${BASE}/${id}`)
    },

    async create(data: UserRequest): Promise<AppUser> {
        return api.post<AppUser>(BASE, data)
    },

    async update(id: number, data: UserRequest): Promise<AppUser> {
        return api.put<AppUser>(`${BASE}/${id}`, data)
    },

    async delete(id: number): Promise<void> {
        await api.del(`${BASE}/${id}`)
    },

    async getCollectors(roles?: UserRole[]): Promise<AppUser[]> {
        return api.get<AppUser[]>(`${BASE}/collectors`, {
            params: roles && roles.length > 0 ? { roles } : undefined,
        })
    },
} as const


