/**
 * Type-safe wrapper for Vite environment variables.
 *
 * All env vars must be prefixed with `VITE_` and defined in
 * `.env.development` / `.env.production`.
 */

// ────────────────────────────────────────────
// Supported language union
// ────────────────────────────────────────────
type SupportedLanguage = 'en' | 'ar'

// ────────────────────────────────────────────
// Environment configuration shape
// ────────────────────────────────────────────
interface EnvConfig {
    /** Base URL for all API calls (e.g. http://localhost:8080/api/v1) */
    API_BASE_URL: string
    /** Display name of the application */
    APP_NAME: string
    /** Semantic version string */
    APP_VERSION: string
    /** Enable debug logging / dev tools */
    ENABLE_DEBUG: boolean
    /** Default UI language (ar = Arabic, en = English) */
    DEFAULT_LANGUAGE: SupportedLanguage
    /** Default rows per page in paginated views */
    DEFAULT_PAGE_SIZE: number
}

// ────────────────────────────────────────────
// Helper — read & validate a single env var
// ────────────────────────────────────────────
const getEnvVar
    =
    (key: string, fallback?: string): string => {
    const value = import.meta.env[key] as string | undefined
    if (value !== undefined && value !== '') return value
    if (fallback !== undefined) return fallback
    throw new Error(
        `[env.config] Missing required environment variable: ${key}. ` +
        `Make sure it is defined in  .env file.`,
    )
}

// ────────────────────────────────────────────
// Exported ENV object — import { ENV } from '@config/env.config'
// ────────────────────────────────────────────
export const ENV: EnvConfig = {
    API_BASE_URL: getEnvVar('VITE_API_BASE_URL'),
    APP_NAME: getEnvVar('VITE_APP_NAME', 'MaalFlow'),
    APP_VERSION: getEnvVar('VITE_APP_VERSION', '0.0.0'),
    ENABLE_DEBUG: getEnvVar('VITE_ENABLE_DEBUG', 'false') === 'true',
    DEFAULT_LANGUAGE: getEnvVar('VITE_DEFAULT_LANGUAGE', 'ar') as SupportedLanguage,
    DEFAULT_PAGE_SIZE: parseInt(getEnvVar('VITE_DEFAULT_PAGE_SIZE', '10'), 10),
}

/** Convenience check — true when running `vite` (not `vite build`) */
export const IS_DEV = import.meta.env.DEV

/** Convenience check — true when running `vite build` */
export const IS_PROD = import.meta.env.PROD

