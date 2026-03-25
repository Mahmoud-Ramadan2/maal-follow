import React, { useState, useEffect, type ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import i18n from '../config/i18n.config'
import { LanguageContext } from './language.context'
import type { Language, Direction, LanguageContextType } from './language.context'

// ────────────────────────────────────────────────────────────
// Provider
// ────────────────────────────────────────────────────────────

export const LanguageProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const { t } = useTranslation()
    const [language, setLanguage] = useState<Language>('ar')
    const [direction, setDirection] = useState<Direction>('rtl')

    const changeLanguage = (lang: Language) => {
        i18n.changeLanguage(lang)
        setLanguage(lang)

        const newDirection = lang === 'en' ? 'ltr' : 'rtl'
        setDirection(newDirection)

        document.documentElement.lang = lang
        document.documentElement.dir = newDirection

        localStorage.setItem('language', lang)
    }

    useEffect(() => {
        const savedLang = localStorage.getItem('language') as Language
        const initialLang = savedLang || 'ar'
        changeLanguage(initialLang)
    }, [])

    const value: LanguageContextType = {
        language,
        direction,
        changeLanguage,
        t,
    }

    return (
        <LanguageContext.Provider value={value}>
            {children}
        </LanguageContext.Provider>
    )
}
