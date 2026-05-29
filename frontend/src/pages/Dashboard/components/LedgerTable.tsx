import { useEffect, useState } from 'react'
import { ledgerApi } from '@/services/api/modules/ledger.api'
import { formatCurrency } from '@/utils/formatters/number'

export default function LedgerTable() {
    const [rows, setRows] = useState<any[]>([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        let mounted = true
        setLoading(true)
        void ledgerApi.getAll({ page: 0, size: 6 }).then(res => {
            if (!mounted) return
            setRows(res.content ?? [])
        }).catch(() => {
            // ignore
        }).finally(() => mounted && setLoading(false))

        return () => { mounted = false }
    }, [])

    return (
        <div className="ledger-table" role="table">
            <div className="ledger-table__head">
                <div>Date</div>
                <div>Entity</div>
                <div>Category</div>
                <div className="text-right">Amount</div>
            </div>
            <div className="ledger-table__body">
                {loading ? <div className="p-4">Loading...</div> : (
                    rows.length === 0 ? <div className="p-4">No recent movements</div> : rows.map(r => (
                        <div key={r.id} className="ledger-table__row">
                            <div>{new Date(r.date).toLocaleDateString()}</div>
                            <div>{r.partnerName ?? r.userName ?? '—'}</div>
                            <div>{r.description ?? r.source}</div>
                            <div className="text-right">{formatCurrency(r.amount)}</div>
                        </div>
                    ))
                )}
            </div>
        </div>
    )
}


