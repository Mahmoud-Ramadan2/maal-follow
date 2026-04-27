import type { PaginationParams } from '@/types/common.types'

// ============================================================
// User Module - TypeScript Types
//
// Maps to backend user endpoints under `/users`.
// ============================================================

// Runtime enum-like object (erasableSyntaxOnly-safe) + union type.
export const UserRole = {
    ADMIN: 'ADMIN',
    OWNER: 'OWNER',
    MANAGER: 'MANAGER',
    COLLECTOR: 'COLLECTOR',
    ACCOUNTANT: 'ACCOUNTANT',
    USER: 'USER',
} as const

export type UserRole = (typeof UserRole)[keyof typeof UserRole]

// Convenient ordered list for selects/filters.
export const USER_ROLES: UserRole[] = Object.values(UserRole)

export interface AppUser {
    id: number
    name: string
    email: string | null
    phone: string | null
    role: UserRole
    createdAt: string
}

/**
 * Request payload used by create/update user endpoints.
 *
 * Note: `password` is optional here to support update flows
 * where password is not being changed.
 */
export interface UserRequest {
    name: string
    email?: string
    password?: string
    phone?: string
    role: UserRole
}

export interface UserFilters extends PaginationParams {
    search?: string
    role?: UserRole
}
