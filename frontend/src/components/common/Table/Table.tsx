import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import LoadingSpinner from '@components/ui/LoadingSpinner'
import { classNames } from '@utils/helpers/classNames'
import './Table.css'

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

/**
 * Describes a single table column.
 *
 * @typeParam T - Row data type.
 */
export interface TableColumn<T> {
    /** Unique key — also used to read the default cell value via `row[key]` */
    key: string
    /** Visible column header text */
    label: string
    /**
     * Optional custom renderer.
     * When provided the return value is rendered instead of `row[key]`.
     */
    render?: (row: T, index: number) => ReactNode
}

interface TableProps<T> {
    /** Column definitions */
    columns: TableColumn<T>[]
    /** Row data array */
    data: T[]
    /** Show skeleton rows while data is loading. @default false */
    loading?: boolean
    /** Text shown when `data` is empty and not loading. */
    emptyMessage?: string
    /** Number of skeleton rows to display while loading. @default 5 */
    skeletonRows?: number
    /** Optional stable row key accessor for richer datasets. */
    rowKey?: keyof T | ((row: T, index: number) => string | number)
}

// ────────────────────────────────────────────────────────────
// Component
// ────────────────────────────────────────────────────────────

/**
 * Generic data table with loading skeleton and empty state.
 *
 * Column `key` is used as a property accessor on each row
 * object unless a custom `render` function is supplied.
 *
 * @typeParam T - Type of each row object.
 *
 * @example
 * ```tsx
 * const columns: TableColumn<Purchase>[] = [
 *     { key: 'id',     label: '#' },
 *     { key: 'name',   label: t('purchase.name') },
 *     { key: 'amount', label: t('purchase.amount'),
 *       render: (row) => `$${row.amount.toFixed(2)}` },
 *     { key: 'actions', label: '',
 *       render: (row) => <Button size="sm" onClick={() => edit(row.id)}>Edit</Button> },
 * ]
 *
 * <Table columns={columns} data={purchases} loading={isLoading} />
 * ```
 */
export default function Table<T extends object>({
    columns,
    data,
    loading = false,
    emptyMessage,
    skeletonRows = 5,
    rowKey,
}: TableProps<T>): ReactNode {
    const { t } = useTranslation()

    const resolveRowKey = (row: T, rowIdx: number): string | number => {
        if (typeof rowKey === 'function') {
            return rowKey(row, rowIdx)
        }

        if (rowKey) {
            return String((row as Record<string, unknown>)[rowKey as string] ?? rowIdx)
        }

        const fallback = (row as Record<string, unknown>)['id']
        return typeof fallback === 'string' || typeof fallback === 'number' ? fallback : rowIdx
    }

    // ── Loading state ──────────────────────────────────────
    if (loading) {
        return (
            <div className="table-wrapper">
                <table className="table">
                    <thead>
                        <tr>
                            {columns.map((col) => (
                                <th key={col.key}>{col.label}</th>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {Array.from({ length: skeletonRows }).map((_, rowIdx) => (
                            <tr key={rowIdx}>
                                {columns.map((col, colIdx) => (
                                    <td key={`${col.key}-${colIdx}`}>
                                        <div
                                            className="table__skeleton"
                                            style={{ width: `${60 + ((rowIdx + colIdx) % 5) * 8}%` }}
                                        />
                                    </td>
                                ))}
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        )
    }

    // ── Empty state ────────────────────────────────────────
    if (data.length === 0) {
        return (
            <div className="table-wrapper">
                <div className="table__state">
                    <LoadingSpinner size="sm" inheritColor />
                    <span className="table__state-text">
                        {emptyMessage || t('noData', 'No data available')}
                    </span>
                </div>
            </div>
        )
    }

    // ── Data table ─────────────────────────────────────────
    return (
        <div className={classNames('table-wrapper')}>
            <table className="table">
                <thead>
                    <tr>
                        {columns.map((col) => (
                            <th key={col.key}>{col.label}</th>
                        ))}
                    </tr>
                </thead>
                <tbody>
                    {data.map((row, rowIdx) => (
                        <tr key={resolveRowKey(row, rowIdx)}>
                            {columns.map((col) => (
                                <td key={col.key}>
                                    {col.render
                                        ? col.render(row, rowIdx)
                                        : ((row as Record<string, unknown>)[col.key] as ReactNode) ?? '—'}
                                </td>
                            ))}
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    )
}
