import { createContext, useContext } from 'react'
import type { ThemeMode, ResolvedThemeMode } from '@config/theme.config'

export interface ThemeContextType {
    themeMode: ThemeMode
    resolvedTheme: ResolvedThemeMode
    isDarkMode: boolean
    isLightMode: boolean
    setThemeMode: (mode: ThemeMode) => void
    toggleTheme: () => void
    resetThemeMode: () => void
}

export const ThemeContext = createContext<ThemeContextType | undefined>(undefined)

export const useTheme = () => {
    const context = useContext(ThemeContext)
    if (context === undefined) {
        throw new Error('useTheme must be used within ThemeProvider')
    }
    return context
}

