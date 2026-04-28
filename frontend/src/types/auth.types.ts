export const AUTH_ROLES = {
    ADMIN: 'ADMIN',
    USER: 'USER',
    MANAGER: 'MANAGER',
    COLLECTOR: 'COLLECTOR',
} as const

export type AuthRole = (typeof AUTH_ROLES)[keyof typeof AUTH_ROLES]

export interface AuthUser {
    id: number | string
    username: string
    email: string
    fullName: string
    role: AuthRole
    roles: AuthRole[]
}

export interface AuthCredentials {
    username: string
    password: string
}

export interface AuthLoginResponse {
    accessToken?: string
    token?: string
    refreshToken?: string
    user?: Partial<AuthUser> & {
        role?: AuthRole
        roles?: Array<AuthRole | string>
    }
}

export interface AuthSession {
    accessToken: string
    refreshToken?: string
    user: AuthUser
}

