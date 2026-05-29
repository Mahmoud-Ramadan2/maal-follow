import apiClient from '../client/interceptors'
import type { AuthRequestConfig } from '../client/auth-request.config'
import { AUTH_ENDPOINTS } from '@utils/constants/auth.constants'
import { AUTH_ROLES } from '@/types/auth.types'
import type {
    AuthCredentials,
    AuthLoginResponse,
    AuthSession,
    AuthRefreshResponse,
    AuthTokenResponse,
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

    const id = payload.id ?? payload.userId
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

const resolveUserPayload = (payload: AuthTokenResponse): unknown => {
    if (payload.user && isRecord(payload.user)) {
        return payload.user
    }

    // Backend returns a flat AuthResponse (userId/name/email/role)
    if (payload.userId !== undefined || payload.id !== undefined || payload.email || payload.role) {
        return payload
    }

    return undefined
}

const normalizeTokenResponse = (payload: AuthTokenResponse, fallbackRefreshToken?: string) => {
    const accessToken = payload.accessToken ?? payload.token
    if (!accessToken) {
        throw new Error('Auth response did not include an access token')
    }

    const userPayload = resolveUserPayload(payload)

    return {
        accessToken,
        refreshToken: payload.refreshToken ?? fallbackRefreshToken,
        user: userPayload ? normalizeUser(userPayload) : undefined,
    }
}

export const authApi = {
    async login(credentials: AuthCredentials): Promise<AuthSession> {
        const response = await apiClient.post<AuthLoginResponse>(AUTH_ENDPOINTS.LOGIN, credentials)
        const normalized = normalizeTokenResponse(response.data)

        if (!normalized.user) {
            throw new Error('Auth login response did not include a user')
        }

        return {
            accessToken: normalized.accessToken,
            refreshToken: normalized.refreshToken,
            user: normalized.user,
        }
    },


    async refresh(refreshToken: string): Promise<Omit<AuthSession, 'user'> & { user?: AuthUser }> {
        const config = { skipAuthRefresh: true } as AuthRequestConfig
        const response = await apiClient.post<AuthRefreshResponse>(AUTH_ENDPOINTS.REFRESH, { refreshToken }, config)
        return normalizeTokenResponse(response.data, refreshToken)
    },

    async logout(refreshToken?: string): Promise<void> {
        const config = { skipAuthRefresh: true } as AuthRequestConfig
        await apiClient.post(
            AUTH_ENDPOINTS.LOGOUT,
            refreshToken ? { refreshToken } : undefined,
            config,
        )
    },

    async logoutAll(refreshToken: string): Promise<void> {
        const config = { skipAuthRefresh: true } as AuthRequestConfig
        await apiClient.post(AUTH_ENDPOINTS.LOGOUT_ALL, { refreshToken }, config)
    },
} as const

