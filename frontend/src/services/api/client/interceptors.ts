import type { AxiosError, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { toast } from 'react-toastify'
import apiClient from './axios.config'
import { ENV, IS_DEV } from '@config/env.config'
import i18n from "i18next";

// ────────────────────────────────────────────────────────────
// Constants — localStorage keys (single source of truth)
// ────────────────────────────────────────────────────────────
const TOKEN_KEY = 'token'
const LANGUAGE_KEY = 'language'


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
        const token = localStorage.getItem(TOKEN_KEY)
        if (token) {
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
    (error: AxiosError<ApiErrorResponse>) => {
        // Network error or request cancelled — no response object
        if (!error.response) {

            toast.error(translate('errors.network', 'common'))
            return Promise.reject(error)
        }


        const { status, data } = error.response
        const message = data?.message

        switch (status) {
            // ── 401 Unauthorized ───────────────────────────
            // Token is missing, expired, or invalid.
            // Save the page the user was on so we can redirect
            // back after login, then clear the session.
            case 401: {
                localStorage.removeItem(TOKEN_KEY)

                // Avoid redirect loop: only save + redirect if we
                // are not already on the login page.
                const currentPath = window.location.pathname
                if (currentPath !== '/login') {
                    localStorage.setItem('redirectAfterLogin', currentPath)
                    window.location.href = '/login'
                }
                if (IS_DEV) {
                    toast.error(message || 'Session expired — please log in again.')
                } else {
                    toast.error(translate('errors.unauthorized'))
                }
                break
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
                const validationMsg =
                    data?.errorsFields?.join(', ') || message || translate('errors.validation', 'common')
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
