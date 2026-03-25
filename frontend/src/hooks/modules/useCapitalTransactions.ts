import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { capitalTransactionApi } from '@services/api/modules/capital.api'
import type { CapitalTransaction, CapitalTransactionType } from '@/types/modules/capital.types'

export function useCapitalTransactions(page = 0, size = 20, typeFilter?: CapitalTransactionType) {
    const { t } = useTranslation('capital')
    const [transactions, setTransactions] = useState<CapitalTransaction[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [totalPages, setTotalPages] = useState(0)
    const [totalElements, setTotalElements] = useState(0)

    const fetch = useCallback(async () => {
        setLoading(true); setError(null)
        try {
            if (typeFilter) {
                const list = await capitalTransactionApi.getByType(typeFilter)
                setTransactions(list)
                setTotalPages(1)
                setTotalElements(list.length)
            } else {
                const res = await capitalTransactionApi.getAll(page, size)
                setTransactions(res.content)
                setTotalPages(res.totalPages)
                setTotalElements(res.totalElements)
            }
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('transaction.fetchError')
            setError(msg); toast.error(t('transaction.fetchError'))
        } finally { setLoading(false) }
    }, [page, size, typeFilter, t])

    useEffect(() => { fetch() }, [fetch])
    return { transactions, loading, error, totalPages, totalElements, refetch: fetch }
}

