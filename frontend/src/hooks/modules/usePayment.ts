import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { paymentApi } from '@services/api/modules/payment.api'
import type { PaymentSummary } from '@/types/modules/payment.types'
import {IS_DEV} from "@/config";

interface UsePaymentsReturn {
    payments: PaymentSummary[]
    loading: boolean
    error: string | null
    totalPages: number
    totalElements: number
    refetch: () => void
}

export function usePayments(page = 0, size = 20, month?: string): UsePaymentsReturn {
    const { t } = useTranslation('payment')
    const [payments, setPayments] = useState<PaymentSummary[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [totalPages, setTotalPages] = useState(0)
    const [totalElements, setTotalElements] = useState(0)

    const fetch = useCallback(async () => {
        setLoading(true); setError(null)
        try {
            const res = month
                ? await paymentApi.getByMonth(month, page, size)
                : await paymentApi.getAll(page, size)
            setPayments(res.content)
            setTotalPages(res.totalPages)
            setTotalElements(res.totalElements)
        } catch (err) {
            // const msg = err instanceof Error ? err.message : t('fetchError')
            const msg = IS_DEV ? err.message : t('fetchError')

            setError(msg); toast.error(t('fetchError'))
        } finally { setLoading(false) }
    }, [page, size, month, t])

    useEffect(() => { fetch() }, [fetch])
    return { payments, loading, error, totalPages, totalElements, refetch: fetch }
}

