import i18n from 'i18next'
import type { InitOptions } from 'i18next'
import { initReactI18next } from 'react-i18next'
import Backend from 'i18next-http-backend'
import LanguageDetector from 'i18next-browser-languagedetector'
import { ENV } from './env.config'

// ────────────────────────────────────────────
// Supported languages
// ────────────────────────────────────────────
export const SUPPORTED_LANGUAGES =
    ['en', 'ar'] as const
export type SupportedLanguage =
    (typeof SUPPORTED_LANGUAGES)[number]

// ────────────────────────────────────────────
// Translation namespaces
// Each maps to /public/locales/{{lng}}/{{ns}}.json
// ────────────────────────────────────────────
export const NAMESPACES = [
    'common',
    'purchase',
    'payment',
    'partner',
    'capital',
    'customer',
    'contract',
    'schedule',
    'vendor',
    'user',
    'validation',
] as const
export type Namespace = (typeof NAMESPACES)[number]

export const DEFAULT_NS: Namespace = 'common'

// ────────────────────────────────────────────
// i18next configuration
// ────────────────────────────────────────────
const i18nOptions: InitOptions = {
    // Fallback to Arabic (default language)
    fallbackLng: ENV.DEFAULT_LANGUAGE,

    // Show debug logs only when ENABLE_DEBUG is true
    debug: ENV.ENABLE_DEBUG,

    // Namespaces
    ns: [...NAMESPACES],
    defaultNS: DEFAULT_NS,

    // Supported languages list
    supportedLngs: [...SUPPORTED_LANGUAGES],

    // Backend — loads JSON files from /public/locales
    backend: {
        loadPath: '/locales/{{lng}}/{{ns}}.json',
    },

    // Language detection — localStorage first, then browser
    detection: {
        order: ['localStorage', 'navigator'],
        lookupLocalStorage: 'i18nextLng',
        caches: ['localStorage'],
    },

    interpolation: {
        // React already escapes values
        escapeValue: false,
    },

    react: {
        // Disable suspense so we can show our own loading state
        useSuspense: false,
    },
}

i18n.use(Backend).use(LanguageDetector).use(initReactI18next).init(i18nOptions)

export default i18n
