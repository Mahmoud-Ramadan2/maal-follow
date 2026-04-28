import type { AuthRole, AuthUser } from '@/types/auth.types'

export const hasRequiredRoles = (
    user: AuthUser | null | undefined,
    requiredRoles?: readonly AuthRole[],
): boolean => {
    if (!requiredRoles || requiredRoles.length === 0) {
        return true
    }

    if (!user) {
        return false
    }

    const userRoles = new Set<AuthRole>([user.role, ...(user.roles ?? [])])
    return requiredRoles.some((role) => userRoles.has(role))
}

