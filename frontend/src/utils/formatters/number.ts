// Small numeric format helpers used across dashboard components
export function formatCurrency(value: number | null | undefined, currency = ''): string {
    if (value == null || Number.isNaN(value)) return '-'
    // Use Intl.NumberFormat for locale-aware formatting (default locale)
    try {
        const nf = new Intl.NumberFormat(undefined, {
            minimumFractionDigits: 0,
            maximumFractionDigits: 2,
        })
        return currency ? `${nf.format(value)} ${currency}` : nf.format(value)
    } catch {
        return String(value)
    }
}

export function formatNumberCompact(value: number | null | undefined): string {
    if (value == null || Number.isNaN(value)) return '-'
    // compact display like 1.2K, 3.4M
    try {
        return new Intl.NumberFormat(undefined, { notation: 'compact', maximumFractionDigits: 1 }).format(value)
    } catch {
        return String(value)
    }
}

