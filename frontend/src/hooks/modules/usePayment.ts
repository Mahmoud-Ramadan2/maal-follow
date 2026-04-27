import { useState, useEffect, useCallback, useMemo } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { paymentApi } from '@services/api/modules/payment.api'
import type { PaymentSearchFilters, PaymentSummary } from '@/types/modules/payment.types'
import { IS_DEV } from '@/config'

interface UsePaymentsReturn {
    payments: PaymentSummary[]
    loading: boolean
    error: string | null
    totalPages: number
    totalElements: number
    refetch: () => void
}

export function usePayments(filters: PaymentSearchFilters = {}): UsePaymentsReturn {
    const { t } = useTranslation('payment')
    const fetchErrorText = t('fetchError')
    const [payments, setPayments] = useState<PaymentSummary[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [totalPages, setTotalPages] = useState(0)
    const [totalElements, setTotalElements] = useState(0)

    const filtersKey = useMemo(() => {
        const cleaned = Object.entries(filters).reduce<Record<string, unknown>>((acc, [key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                acc[key] = value
            }
            return acc
        }, {})

        return JSON.stringify(cleaned)
    }, [filters])

    const parsedFilters = useMemo(() => JSON.parse(filtersKey) as PaymentSearchFilters, [filtersKey])

    const fetch = useCallback(async () => {
        setLoading(true); setError(null)
        try {
            const res = await paymentApi.search(parsedFilters)
            setPayments(res.content)
            setTotalPages(res.totalPages)
            setTotalElements(res.totalElements)
        } catch (err) {
            const msg = IS_DEV && err instanceof Error ? err.message : fetchErrorText

            setError(msg); toast.error(fetchErrorText)
        } finally { setLoading(false) }
    }, [parsedFilters, fetchErrorText])

    useEffect(() => { fetch() }, [fetch])
    return { payments, loading, error, totalPages, totalElements, refetch: fetch }
}

