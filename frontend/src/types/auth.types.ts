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
    email: string
    password: string
}


export interface AuthTokenResponse {
    accessToken?: string
    token?: string
    refreshToken?: string
    tokenType?: string
    userId?: number | string
    id?: number | string
    name?: string
    fullName?: string
    username?: string
    email?: string
    role?: AuthRole | string
    roles?: Array<AuthRole | string>
    user?: Partial<AuthUser> & Record<string, unknown>
}

export interface AuthLoginResponse extends AuthTokenResponse {
    user: NonNullable<AuthTokenResponse['user']>
}

// eslint-disable-next-line @typescript-eslint/no-empty-object-type
export interface AuthRefreshResponse extends AuthTokenResponse {}

export interface AuthSession {
    accessToken: string
    refreshToken?: string
    user: AuthUser
}



