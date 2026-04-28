export const AUTH_STORAGE_KEYS = {
    ACCESS_TOKEN: 'token',
    REDIRECT_AFTER_LOGIN: 'redirectAfterLogin',
} as const

export const AUTH_ENDPOINTS = {
    LOGIN: '/auth/login',
    ME: '/auth/me',
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh',
} as const

export const AUTH_PATHS = {
    LOGIN: '/login',
    REGISTER: '/register',
} as const

