/**
 * Format a number as currency.
 *
 * Uses the browser's `Intl.NumberFormat` so decimal / thousand
 * separators adapt to the user's locale automatically.
 *
 * @param amount   Numeric amount
 * @param currency  (default `'EGP'`)
 * @returns Formatted string (e.g. `EGP 1,200.00`)
 */
export function formatCurrency(
    amount: number | null | undefined,
    currency: string = 'EGP',
): string {
    if (amount == null) return '—'

    return new Intl.NumberFormat(undefined, {
        style: 'currency',
        currency,
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    }).format(amount)
}
