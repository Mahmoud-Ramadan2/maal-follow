export const AUTH_STORAGE_KEYS = {
    ACCESS_TOKEN: 'token',
    REFRESH_TOKEN: 'refreshToken',
    AUTH_USER: 'authUser',
    REDIRECT_AFTER_LOGIN: 'redirectAfterLogin',
} as const

export const AUTH_ENDPOINTS = {
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout',
    LOGOUT_ALL: '/auth/logout-all',
    REFRESH: '/auth/refresh',
} as const


