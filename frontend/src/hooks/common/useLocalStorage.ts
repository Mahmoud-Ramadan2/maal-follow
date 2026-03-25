import { useState, useCallback, useEffect } from 'react'

// ────────────────────────────────────────────────────────────
// Helpers
// ────────────────────────────────────────────────────────────

/**
 * Safely read and parse a value from localStorage.
 * Returns `fallback` if the key doesn't exist or parsing fails.
 */
function readFromStorage<T>(key: string, fallback: T): T {
    try {
        const raw = localStorage.getItem(key)
        if (raw === null) return fallback
        return JSON.parse(raw) as T
    } catch {
        // Corrupted JSON or SecurityError (private browsing)
        return fallback
    }
}

/**
 * Safely write a value to localStorage as JSON.
 */
function writeToStorage<T>(key: string, value: T): void {
    try {
        localStorage.setItem(key, JSON.stringify(value))
    } catch {
        // QuotaExceededError or SecurityError — fail silently
        console.warn(`[useLocalStorage] Failed to write key "${key}"`)
    }
}

// ────────────────────────────────────────────────────────────
// Hook
// ────────────────────────────────────────────────────────────

/**
 * Like `useState`, but the value is persisted in `localStorage`.
 *
 * On mount the hook reads the stored value (falling back to
 * `initialValue` if the key doesn't exist). Every call to
 * `setValue` writes the new value to both React state **and**
 * localStorage.
 *
 * Also listens for the `storage` event so the value stays in
 * sync across browser tabs.
 *
 * @typeParam T - Type of the stored value. Must be JSON-serializable.
 * @param key          - localStorage key.
 * @param initialValue - Fallback when the key doesn't exist yet.
 * @returns A tuple of `[storedValue, setValue, removeValue]`.
 *
 * @example
 * ```tsx
 * const [theme, setTheme] = useLocalStorage<'light' | 'dark'>('theme', 'light')
 *
 * <button onClick={() => setTheme('dark')}>Dark mode</button>
 * ```
 *
 * @example
 * ```tsx
 * // Store an object
 * const [prefs, setPrefs] = useLocalStorage('userPrefs', { pageSize: 10 })
 * setPrefs({ ...prefs, pageSize: 25 })
 * ```
 */
export function useLocalStorage<T>(
    key: string,
    initialValue: T,
): [T, (value: T | ((prev: T) => T)) => void, () => void] {
    // Lazy initializer — only reads localStorage once on mount
    const [storedValue, setStoredValue] = useState<T>(() =>
        readFromStorage(key, initialValue),
    )

    /**
     * Update both React state and localStorage.
     * Accepts a direct value or a setter function (like `useState`).
     */
    const setValue = useCallback(
        (value: T | ((prev: T) => T)) => {
            setStoredValue((prev) => {
                const next = value instanceof Function ? value(prev) : value
                writeToStorage(key, next)
                return next
            })
        },
        [key],
    )

    /**
     * Remove the key from localStorage and reset state to `initialValue`.
     */
    const removeValue = useCallback(() => {
        try {
            localStorage.removeItem(key)
        } catch {
            // ignore
        }
        setStoredValue(initialValue)
    }, [key, initialValue])

    // ── Cross-tab sync ──────────────────────────────────────
    // When another tab writes to the same key we pick up the change.
    useEffect(() => {
        const handleStorageChange = (e: StorageEvent) => {
            if (e.key !== key) return
            setStoredValue(e.newValue === null ? initialValue : (JSON.parse(e.newValue) as T))
        }

        window.addEventListener('storage', handleStorageChange)
        return () => window.removeEventListener('storage', handleStorageChange)
    }, [key, initialValue])

    return [storedValue, setValue, removeValue]
}

