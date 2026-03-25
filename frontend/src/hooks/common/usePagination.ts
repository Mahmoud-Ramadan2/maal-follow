import { useState, useCallback, useMemo } from 'react'
import { ENV } from '@config/env.config'

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

/** Configuration options for `usePagination`. */
interface UsePaginationOptions {
    /**
     * Initial page index (zero-based, matching Spring Data).
     * @default 0
     */
    initialPage?: number
    /**
     * Initial page size.
     * @default ENV.DEFAULT_PAGE_SIZE  (from .env — typically 10)
     */
    initialSize?: number
}

/** Current pagination state. */
interface PaginationState {
    /** Current page index (zero-based) */
    page: number
    /** Number of items per page */
    size: number
}

/** Actions returned by the hook. */
interface PaginationActions {
    /** Go to the next page */
    nextPage: () => void
    /** Go to the previous page (no-op on page 0) */
    prevPage: () => void
    /** Jump to a specific zero-based page index */
    goToPage: (page: number) => void
    /** Change the page size and reset to page 0 */
    setSize: (size: number) => void
    /** Reset pagination to initial values */
    reset: () => void
}

/** Full return type of `usePagination`. */
type UsePaginationReturn = PaginationState & PaginationActions

// ────────────────────────────────────────────────────────────
// Hook
// ────────────────────────────────────────────────────────────

/**
 * Manages client-side pagination state.
 *
 * Page indices are **zero-based** to match Spring Data's `Pageable`.
 * The returned `page` and `size` can be spread directly into a
 * request params object.
 *
 * @param options - Optional initial page and size overrides.
 * @returns Current state (`page`, `size`) and control methods.
 *
 * @example
 * ```tsx
 * const { page, size, nextPage, prevPage, goToPage, setSize } = usePagination()
 *
 * // Fetch with current pagination
 * const data = await api.get<PaginatedResponse<Purchase>>('/purchases', {
 *     params: { page, size, sort: 'createdAt,desc' },
 * })
 *
 * // Wire up to UI
 * <button onClick={prevPage} disabled={page === 0}>Previous</button>
 * <button onClick={nextPage} disabled={data.last}>Next</button>
 * ```
 */
export function usePagination
(options: UsePaginationOptions = {}): UsePaginationReturn {
    const {
        initialPage = 0,
        initialSize = ENV.DEFAULT_PAGE_SIZE,
    } = options

    const [page, setPage] = useState(initialPage)
    const [size, setSizeState] = useState(initialSize)

    const nextPage = useCallback(() => {
        setPage((prev) => prev + 1)
    }, [])

    const prevPage = useCallback(() => {
        setPage((prev) => Math.max(0, prev - 1))
    }, [])

    const goToPage = useCallback((target: number) => {
        setPage(Math.max(0, target))
    }, [])

    /** Change page size and reset to first page (page 0). */
    const setSize = useCallback((newSize: number) => {
        setSizeState(newSize)
        setPage(0)
    }, [])

    const reset = useCallback(() => {
        setPage(initialPage)
        setSizeState(initialSize)
    }, [initialPage, initialSize])

    return useMemo(() => ({
        page,
        size,
        nextPage,
        prevPage,
        goToPage,
        setSize,
        reset,
    }), [page, size, nextPage, prevPage, goToPage, setSize, reset])
}

