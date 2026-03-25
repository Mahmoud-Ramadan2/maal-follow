/**
 * Public API surface for the HTTP client.
 *
 * Import from `@services/api` everywhere in the app:
 *
 *   import { api } from '@services/api'
 *   const users = await api.get<User[]>('/users')
 *
 * The module re-exports the Axios instance (with interceptors
 * already attached) plus four typed helper functions that
 * unwrap `AxiosResponse` so callers get `T` directly instead
 * of `AxiosResponse<T>`.
 */

import type { AxiosRequestConfig } from 'axios'

// Import the client WITH interceptors attached.
// `interceptors.ts` imports `axios.config.ts` internally and
// registers request/response interceptors on it, then
// re-exports the same instance — so this single import gives
// us a fully configured client.
import apiClient from './client/interceptors'

// ────────────────────────────────────────────────────────────
// Generic helper functions
//
// Each unwraps the AxiosResponse and returns `response.data`
// so every call-site gets the typed payload directly:
//
//   const purchase = await api.get<Purchase>('/purchases/1')
//   //    ^? Purchase  (not AxiosResponse<Purchase>)
// ────────────────────────────────────────────────────────────

/**
 * GET request — fetch a resource.
 *
 * @param url    Endpoint path (appended to baseURL)
 * @param config Optional Axios config (params, headers …)
 * @returns      Parsed response body typed as `T`
 */
async function get<T>
(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response =
        await apiClient.get<T>(url, config)
    return response.data
}

/**
 * POST request — create a resource.
 *
 * @param url    Endpoint path
 * @param data   Request body
 * @param config Optional Axios config
 * @returns      Parsed response body typed as `T`
 */
async function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    const response = await apiClient.post<T>(url, data, config)
    return response.data
}

/**
 * PUT request — fully replace a resource.
 *
 * @param url    Endpoint path
 * @param data   Request body
 * @param config Optional Axios config
 * @returns      Parsed response body typed as `T`
 */
async function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    const response = await apiClient.put<T>(url, data, config)
    return response.data
}

/**
 * PATCH request — partially update a resource.
 *
 * @param url    Endpoint path
 * @param data   Request body (partial fields)
 * @param config Optional Axios config
 * @returns      Parsed response body typed as `T`
 */
async function patch<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    const response = await apiClient.patch<T>(url, data, config)
    return response.data
}

/**
 * DELETE request — remove a resource.
 *
 * @param url    Endpoint path
 * @param config Optional Axios config
 * @returns      Parsed response body typed as `T`
 */
async function del<T = void>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await apiClient.delete<T>(url, config)
    return response.data
}

// ────────────────────────────────────────────────────────────
// Bundled API object for convenient imports
//
//   import { api } from '@services/api'
//   await api.get<Purchase[]>('/purchases')
//   await api.post<Purchase>('/purchases', payload)
//   await api.del('/purchases/1')
// ────────────────────────────────────────────────────────────
export const api = { get, post, put, patch, del } as const

// Also export the raw Axios instance for advanced use-cases
// (e.g. file uploads that need onUploadProgress)
export { apiClient }
