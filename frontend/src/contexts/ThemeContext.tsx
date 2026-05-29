import React, { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import {
    applyThemeModeToDocument,
    DEFAULT_THEME_MODE,
    getStoredThemeMode,
    getSystemThemeMode,
    normalizeThemeMode,
    resolveThemeMode,
    setStoredThemeMode,
    type ResolvedThemeMode,
    type ThemeMode,
} from '@config/theme.config'
import { ThemeContext, type ThemeContextType } from './theme.context'

export const ThemeProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [themeMode, setThemeModeState] = useState<ThemeMode>(() => getStoredThemeMode())
    const [systemTheme, setSystemTheme] = useState<ResolvedThemeMode>(() => getSystemThemeMode())

    useEffect(() => {
        if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
            return undefined
        }

        const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')

        const syncSystemTheme = () => {
            setSystemTheme(mediaQuery.matches ? 'dark' : 'light')
        }

        syncSystemTheme()

        if (typeof mediaQuery.addEventListener === 'function') {
            mediaQuery.addEventListener('change', syncSystemTheme)
            return () => mediaQuery.removeEventListener('change', syncSystemTheme)
        }

        mediaQuery.addListener(syncSystemTheme)
        return () => mediaQuery.removeListener(syncSystemTheme)
    }, [])

    useEffect(() => {
        const normalizedMode = normalizeThemeMode(themeMode)
        setStoredThemeMode(normalizedMode)
        applyThemeModeToDocument(normalizedMode, systemTheme)
    }, [systemTheme, themeMode])

    const setThemeMode = useCallback((mode: ThemeMode) => {
        setThemeModeState(normalizeThemeMode(mode))
    }, [])

    const toggleTheme = useCallback(() => {
        setThemeModeState((currentMode) => {
            const activeTheme = resolveThemeMode(currentMode, systemTheme)
            return activeTheme === 'dark' ? 'light' : 'dark'
        })
    }, [systemTheme])

    const resetThemeMode = useCallback(() => {
        setThemeModeState(DEFAULT_THEME_MODE)
    }, [])

    const resolvedTheme = resolveThemeMode(themeMode, systemTheme)

    const value: ThemeContextType = useMemo(() => ({
        themeMode,
        resolvedTheme,
        isDarkMode: resolvedTheme === 'dark',
        isLightMode: resolvedTheme === 'light',
        setThemeMode,
        toggleTheme,
        resetThemeMode,
    }), [resetThemeMode, resolvedTheme, setThemeMode, themeMode, toggleTheme])

    return (
        <ThemeContext.Provider value={value}>
            {children}
        </ThemeContext.Provider>
    )
}

