import axios from 'axios'
import { ENV } from '@config/env.config'

/**
 * Pre-configured Axios instance for the Spring Boot backend.
 *
 * • Base URL comes from VITE_API_BASE_URL (e.g. http://localhost:8080/api/v1)
 *   In development Vite proxies /api → localhost:8080, so the instance
 *   uses ENV.API_BASE_URL which resolves through the proxy.
 *
 * • Timeout is set to 15 seconds — long enough for report-style
 *   queries but short enough to fail fast on network issues.
 *
 * • Default headers tell the backend we speak JSON.
 *
 * Interceptors are attached separately in `interceptors.ts` so this
 * file stays focused on instance creation.
 */
const apiClient = axios.create({
    baseURL: ENV.API_BASE_URL,
    timeout: 15_000, // 15 seconds
    headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
    },
})

export default apiClient
