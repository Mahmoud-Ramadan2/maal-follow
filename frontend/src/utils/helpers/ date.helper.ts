/**
 * Format an ISO date string to a locale-friendly display.
 *
 * @param isoDate  ISO date string (`YYYY-MM-DD` or `YYYY-MM-DDTHH:mm:ss`)
 * @param options  Intl.DateTimeFormat options override
 * @returns Formatted string (e.g. `26/02/2026`) or `'—'` for invalid input
 *
 * @example
 * formatDate('2026-02-26')                       // "26/02/2026"
 * formatDate('2026-02-26T14:30:00', { dateStyle: 'long' }) // "26 February 2026"
 */
export function formatDate(
    isoDate: string | null | undefined,
    options?: Intl.DateTimeFormatOptions,
): string {
    if (!isoDate) return '—'

    const date = new Date(isoDate)
    if (isNaN(date.getTime())) return '—'

    // Default: DD/MM/YYYY
    const defaults: Intl.DateTimeFormatOptions = {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        ...options,
    }

    return date.toLocaleDateString(undefined, defaults)
}

/**
 * Format an ISO datetime string to include date + time.
 *
 * @param isoDateTime  ISO datetime string (`YYYY-MM-DDTHH:mm:ss`)
 * @returns Formatted string (e.g. `26/02/2026, 14:30`)
 */
export function formatDateTime(
    isoDateTime: string | null | undefined,
): string {
    if (!isoDateTime) return '—'

    const date = new Date(isoDateTime)
    if (isNaN(date.getTime())) return '—'

    return date.toLocaleString(undefined, {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    })
}
