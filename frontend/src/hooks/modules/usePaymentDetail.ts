import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { paymentApi } from '@services/api/modules/payment.api'
import type { Payment } from '@/types/modules/payment.types'

export function usePaymentDetail(id: number) {
    const { t } = useTranslation('payment')
    const fetchOneErrorText = t('fetchOneError')
    const [payment, setPayment] = useState<Payment | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetch = useCallback(async () => {
        if (!id || Number.isNaN(id)) {
            setPayment(null)
            setLoading(false)
            setError(fetchOneErrorText)
            return
        }
        setLoading(true); setError(null)
        try {
            setPayment(await paymentApi.getById(id))
        } catch (err) {
            const msg = err instanceof Error ? err.message : fetchOneErrorText
            setError(msg); toast.error(fetchOneErrorText)
        } finally { setLoading(false) }
    }, [id, fetchOneErrorText])

    useEffect(() => { fetch() }, [fetch])
    return { payment, loading, error, refetch: fetch }
}

