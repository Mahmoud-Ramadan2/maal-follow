import { api } from '@services/api'
import { AUTH_ENDPOINTS } from '@utils/constants/auth.constants'
import { AUTH_ROLES } from '@/types/auth.types'
import type {
    AuthCredentials,
    AuthLoginResponse,
    AuthSession,
    AuthUser,
} from '@/types/auth.types'

const isRecord = (value: unknown): value is Record<string, unknown> => {
    return typeof value === 'object' && value !== null
}

const isAuthRole = (value: unknown): value is AuthUser['role'] => {
    return typeof value === 'string' && Object.values(AUTH_ROLES).includes(value as AuthUser['role'])
}

const normalizeRoles = (roles: unknown, fallbackRole: AuthUser['role']): AuthUser['roles'] => {
    if (!Array.isArray(roles)) {
        return [fallbackRole]
    }

    const normalized = roles.filter((role): role is AuthUser['role'] => isAuthRole(role))
    return normalized.length > 0 ? normalized : [fallbackRole]
}

const normalizeUser = (payload: unknown): AuthUser => {
    if (!isRecord(payload)) {
        throw new Error('Invalid auth user payload')
    }

    const id = payload.id
    const email = typeof payload.email === 'string' ? payload.email : ''
    const username = typeof payload.username === 'string'
        ? payload.username
        : typeof payload.name === 'string'
            ? payload.name
            : email
    const fullName = typeof payload.fullName === 'string'
        ? payload.fullName
        : typeof payload.name === 'string'
            ? payload.name
            : username
    const fallbackRole = isAuthRole(payload.role) ? payload.role : AUTH_ROLES.USER
    const roles = normalizeRoles(payload.roles, fallbackRole)

    if ((typeof id === 'number' || typeof id === 'string') && email) {
        return {
            id,
            username,
            email,
            fullName,
            role: fallbackRole,
            roles,
        }
    }

    throw new Error('Invalid auth user payload')
}

const normalizeSession = (payload: AuthLoginResponse): AuthSession => {
    const accessToken = payload.accessToken ?? payload.token
    if (!accessToken) {
        throw new Error('Auth login response did not include an access token')
    }

    if (!payload.user) {
        throw new Error('Auth login response did not include a user')
    }

    return {
        accessToken,
        refreshToken: payload.refreshToken,
        user: normalizeUser(payload.user),
    }
}

export const authApi = {
    async login(credentials: AuthCredentials): Promise<AuthSession> {
        const response = await api.post<AuthLoginResponse>(AUTH_ENDPOINTS.LOGIN, credentials)
        return normalizeSession(response)
    },

    async me(): Promise<AuthUser> {
        const response = await api.get<AuthUser | { user?: AuthUser }>(AUTH_ENDPOINTS.ME)

        if (isRecord(response) && 'user' in response && response.user) {
            return normalizeUser(response.user)
        }

        return normalizeUser(response)
    },
} as const


