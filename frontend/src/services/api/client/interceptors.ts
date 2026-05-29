import type { AxiosError, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { toast } from 'react-toastify'
import apiClient from './axios.config'
import { ENV, IS_DEV } from '@config/env.config'
import i18n from "i18next";
import { AUTH_ENDPOINTS } from '@utils/constants'
import type { AuthRefreshResponse, AuthTokenResponse } from '@/types/auth.types'
import type { AuthRequestConfig } from './auth-request.config'
import {
    clearSessionStorage,
    getStoredAccessToken,
    getStoredRefreshToken,
    saveRedirectAfterLogin,
    setStoredSessionTokens,
} from '@services/auth/session'

// ────────────────────────────────────────────────────────────
// Constants — localStorage keys (single source of truth)
// ────────────────────────────────────────────────────────────
const LANGUAGE_KEY = 'language'

const AUTHLESS_ENDPOINTS = [
    AUTH_ENDPOINTS.LOGIN,
    AUTH_ENDPOINTS.LOGOUT,
    AUTH_ENDPOINTS.REFRESH,
] as const

const isAuthlessEndpoint = (url?: string): boolean => {
    return typeof url === 'string' && AUTHLESS_ENDPOINTS.some((endpoint) => url.startsWith(endpoint))
}

type RefreshableRequestConfig = AuthRequestConfig & { headers: InternalAxiosRequestConfig['headers'] }

const getAccessToken = (): string | null => {
    return getStoredAccessToken()
}

const getSessionRefreshToken = (): string | null => {
    return getStoredRefreshToken()
}

const extractAccessToken = (payload: AuthTokenResponse): string | null => {
    return payload.accessToken ?? payload.token ?? null
}

let refreshPromise: Promise<{ accessToken: string; refreshToken?: string; user?: unknown } | null> | null = null
let hasRedirectedToLogin = false

const startRefresh = async (): Promise<{ accessToken: string; refreshToken?: string; user?: unknown } | null> => {
    const refreshToken = getSessionRefreshToken()

    if (!refreshToken) {
        return null
    }

    const response = await apiClient.post<AuthRefreshResponse>(
        AUTH_ENDPOINTS.REFRESH,
        { refreshToken },
        { skipAuthRefresh: true } as AuthRequestConfig,
    )

    const accessToken = extractAccessToken(response.data)

    if (!accessToken) {
        throw new Error('Refresh response did not include an access token')
    }

    const nextRefreshToken = response.data.refreshToken ?? refreshToken
    setStoredSessionTokens({ accessToken, refreshToken: nextRefreshToken })

    return {
        accessToken,
        refreshToken: nextRefreshToken,
        user: response.data.user,
    }
}

const getRefreshSession = async (): Promise<{ accessToken: string; refreshToken?: string; user?: unknown } | null> => {
    if (!refreshPromise) {
        refreshPromise = startRefresh().finally(() => {
            refreshPromise = null
        })
    }

    return refreshPromise
}

const handleAuthFailure = (): void => {
    if (hasRedirectedToLogin) {
        return
    }

    hasRedirectedToLogin = true
    const currentPath = `${window.location.pathname}${window.location.search}${window.location.hash}`

    clearSessionStorage()

    if (currentPath !== AUTH_ENDPOINTS.LOGIN) {
        saveRedirectAfterLogin(currentPath)
        window.location.href = AUTH_ENDPOINTS.LOGIN
    }
}


// ────────────────────────────────────────────────────────────
const translate = (key: string, namespace: string = 'common') => {
    return i18n.t(key, { ns: namespace })
}
// ────────────────────────────────────────────────────────────
// Types — shape of the Spring Boot error response body
// ────────────────────────────────────────────────────────────

/** Standard error body returned by our Spring Boot @RestControllerAdvice */
interface ApiErrorResponse {
    message?: string
    status?: number
    errorsFields?: string[]
    timestamp?: string
    path?: string
}

// ────────────────────────────────────────────────────────────
// REQUEST INTERCEPTOR
// ────────────────────────────────────────────────────────────
apiClient.interceptors.request.use(

(config: InternalAxiosRequestConfig) => {
        // 1. Attach JWT Bearer token (if user is logged in)
        const token = getAccessToken()
        if (token && !isAuthlessEndpoint(config.url)) {
            config.headers.Authorization = `Bearer ${token}`
        }

        // 2. Tell the backend which language the UI is displaying
        //    so it can return translated validation messages, etc.
        const language = localStorage.getItem(LANGUAGE_KEY) || ENV.DEFAULT_LANGUAGE
    config.headers['Accept-Language'] = language

    // 3. Add language as a query parameter for endpoints that need it
    //    (e.g., /customers/stats/count?lang=en)
    if (!config.params) {
        config.params = {}
    }
    config.params.lang = language
        // 4. Development-only request logging
        if (IS_DEV && ENV.ENABLE_DEBUG) {
            console.log(
                `[API 20→] %c${config.method?.toUpperCase()} %c${config.baseURL}${config.url}`,
                'color: #3b82f6; font-weight: bold',
                'color: #6b7280',
                config.params ?? '',
            )
        }

        return config
    },
    (error: AxiosError) => {
        return Promise.reject(error)
    },
)

// const t = (key: string): string => i18n.t(key, { ns: 'validation' })

// ────────────────────────────────────────────────────────────
// RESPONSE INTERCEPTOR
// ────────────────────────────────────────────────────────────
apiClient.interceptors.response.use(
// ── Success (2xx) ──────────────────────────────────────
    (response: AxiosResponse) => {
        if (IS_DEV && ENV.ENABLE_DEBUG) {
            console.log(
                `[API ←] %c${response.status} %c${response.config.url}`,
                'color: #10b981; font-weight: bold',
                'color: #6b7280',
                response.data,
            )
        }
        return response
    },

    // ── Error (non-2xx) ────────────────────────────────────
    async (error: AxiosError<ApiErrorResponse>) => {
        // Network error or request cancelled — no response object
        if (!error.response) {
            // toast.info('ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd');

            toast.error(translate('errors.network', 'common'))
            return Promise.reject(error)
        }


        const { status, data } = error.response
        const message = data?.message
        const originalRequest = error.config as RefreshableRequestConfig | undefined

        if (!originalRequest) {
            return Promise.reject(error)
        }

        switch (status) {
            // ── 401 Unauthorized ───────────────────────────
            // Token is missing, expired, or invalid.
            // If this is a login request, pass error through for caller to handle.
            // Otherwise, save redirect and clear session for re-login.
            case 401: {
                if (isAuthlessEndpoint(originalRequest.url)) {
                    // For login/refresh endpoint, return error to caller
                    if (originalRequest.url?.startsWith(AUTH_ENDPOINTS.LOGIN) ||
                        originalRequest.url?.startsWith(AUTH_ENDPOINTS.REFRESH)) {
                        return Promise.reject(error)
                    }

                    // For logout endpoint, allow it to fail silently
                    handleAuthFailure()
                    return Promise.reject(error)
                }

                if (originalRequest._retry) {
                    handleAuthFailure()
                    return Promise.reject(error)
                }

                originalRequest._retry = true

                try {
                    const refreshedSession = await getRefreshSession()

                    if (!refreshedSession?.accessToken) {
                        handleAuthFailure()
                        return Promise.reject(error)
                    }

                    originalRequest.headers.Authorization = `Bearer ${refreshedSession.accessToken}`
                    return await apiClient.request(originalRequest)
                } catch (refreshError) {
                    handleAuthFailure()
                    return Promise.reject(refreshError)
                }
            }

            // ── 403 Forbidden ──────────────────────────────
            // User is authenticated but lacks permission.
            case 403: {
                if (IS_DEV) {
                    toast.error(message || 'You do not have permission to perform this action.')
                } else {
                    toast.error(translate('errors.forbidden'))
                }
                break
            }

            // ── 404 Not Found ──────────────────────────────
            case 404: {
                if (IS_DEV) {
                    toast.error(message || 'The requested resource was not found.')
                } else {
                    toast.error(translate('errors.notFound'))
                }
                break
            }

            // ── 422 Validation Error ───────────────────────
            // Spring @Valid failures come back as 422 (or 400).
            case 400:
            case 422: {

                // Handle validation errors: prefer backend message, then fallback to i18n
                const validationMsg =
                    message || translate('errors.validation', 'common')
                toast.error(validationMsg)
                break
            }

            // ── 500 Internal Server Error ──────────────────
            case 500:
            {
                // const ss = translate('errors.validation', 'common')
                if (IS_DEV) {
                    toast.error(message || `Internal server error (${status}).`)
                } else {
                    toast.error(translate('errors.server'))
                }
                break
        }
            // ── Everything else ────────────────────────────
            default:{
                if (IS_DEV) {
                    toast.error(message || `Request failed (${status}).`)
                } else {
                    toast.error(`${translate('errors.other')} (${status}).`)
                }
            }
        }

        // Development-only error logging
        if (IS_DEV && ENV.ENABLE_DEBUG) {
            console.error(
                `[API ✗] %c${status} %c${error.config?.url}`,
                'color: #ef4444; font-weight: bold',
                'color: #6b7280',
                data,
            )
        }
        return Promise.reject(error)
    },
)

export default apiClient
