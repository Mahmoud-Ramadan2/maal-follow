import { createContext } from 'react'

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

export type Language = 'ar' | 'en'
export type Direction = 'rtl' | 'ltr'

export interface LanguageContextType {
    language: Language
    direction: Direction
    changeLanguage: (lang: Language) => void
    t: (key: string) => string
}

// ────────────────────────────────────────────────────────────
// Context
// ────────────────────────────────────────────────────────────

export const LanguageContext = createContext<LanguageContextType | undefined>(undefined)

