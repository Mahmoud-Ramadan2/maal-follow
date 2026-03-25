import { useState, useEffect } from 'react'

/**
 * Debounces a value by the given delay.
 *
 * Every time `value` changes the internal timer resets.
 * The returned value only updates once the caller stops
 * changing it for `delay` milliseconds.
 *
 * Common use-case: search inputs — avoid firing an API call
 * on every keystroke.
 *
 * @typeParam T - Type of the value being debounced.
 * @param value - The raw (fast-changing) value.
 * @param delay - Debounce window in milliseconds.
 * @returns The debounced value.
 *
 * @example
 * ```tsx
 * const [search, setSearch] = useState('')
 * const debouncedSearch = useDebounce(search, 400)
 *
 * useEffect(() => {
 *     // Only fires 400 ms after the user stops typing
 *     fetchResults(debouncedSearch)
 * }, [debouncedSearch])
 *
 * <input value={search} onChange={(e) => setSearch(e.target.value)} />
 * ```
 */
export function useDebounce<T>(value: T, delay: number = 300): T {
    const [debouncedValue, setDebouncedValue] = useState<T>(value)

    useEffect(() => {
        // Start a timer that will update the debounced value
        const timer = setTimeout(() => {
            setDebouncedValue(value)
        }, delay)

        // If `value` or `delay` changes before the timer fires,
        // clear the old timer so it never fires.
        return () => {
            clearTimeout(timer)
        }
    }, [value, delay])

    return debouncedValue
}

