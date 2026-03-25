import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { paymentApi } from '@services/api/modules/payment.api'
import type { Payment } from '@/types/modules/payment.types'

export function usePaymentDetail(id: number) {
    const { t } = useTranslation('payment')
    const [payment, setPayment] = useState<Payment | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetch = useCallback(async () => {
        if (!id) return
        setLoading(true); setError(null)
        try {
            setPayment(await paymentApi.getById(id))
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('fetchOneError')
            setError(msg); toast.error(t('fetchOneError'))
        } finally { setLoading(false) }
    }, [id, t])

    useEffect(() => { fetch() }, [fetch])
    return { payment, loading, error, refetch: fetch }
}

