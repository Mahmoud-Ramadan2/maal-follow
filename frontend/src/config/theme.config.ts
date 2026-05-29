/**
 * Theme configuration and document helpers.
 *
 * Phase 1 keeps the theme surface intentionally small:
 * - persisted preference: light | dark | system
 * - resolved runtime mode: light | dark
 * - document syncing via data-theme + color-scheme
 */

export type ThemeMode = 'light' | 'dark' | 'system'
export type ResolvedThemeMode = Exclude<ThemeMode, 'system'>

export const THEME_STORAGE_KEY = 'maalflow-theme'
export const THEME_ATTRIBUTE = 'data-theme'
export const THEME_MODE_ATTRIBUTE = 'data-theme-mode'
export const DEFAULT_THEME_MODE: ThemeMode = 'system'

export const THEME_MODES = ['light', 'dark', 'system'] as const

export const normalizeThemeMode = (value: string | null | undefined): ThemeMode => {
    if (value === 'light' || value === 'dark' || value === 'system') {
        return value
    }

    return DEFAULT_THEME_MODE
}

export const getSystemThemeMode = (): ResolvedThemeMode => {
    if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
        return 'light'
    }

    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export const resolveThemeMode = (
    mode: ThemeMode,
    systemMode: ResolvedThemeMode = getSystemThemeMode(),
): ResolvedThemeMode => {
    return mode === 'system' ? systemMode : mode
}

export const getStoredThemeMode = (): ThemeMode => {
    if (typeof window === 'undefined') {
        return DEFAULT_THEME_MODE
    }

    return normalizeThemeMode(window.localStorage.getItem(THEME_STORAGE_KEY))
}

export const setStoredThemeMode = (mode: ThemeMode): void => {
    if (typeof window === 'undefined') {
        return
    }

    window.localStorage.setItem(THEME_STORAGE_KEY, mode)
}

export const applyThemeModeToDocument = (
    mode: ThemeMode,
    systemMode: ResolvedThemeMode = getSystemThemeMode(),
): ResolvedThemeMode => {
    const resolvedMode = resolveThemeMode(mode, systemMode)

    if (typeof document !== 'undefined') {
        const root = document.documentElement
        root.setAttribute(THEME_ATTRIBUTE, resolvedMode)
        root.setAttribute(THEME_MODE_ATTRIBUTE, mode)
        root.style.colorScheme = resolvedMode
    }

    return resolvedMode
}

