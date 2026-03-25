import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { paymentApi } from '@services/api/modules/payment.api'
import type { PaymentStatistics } from '@/types/modules/payment.types'

export function usePaymentStatistics(year: number, month: number) {
    const { t } = useTranslation('payment')
    const [statistics, setStatistics] = useState<PaymentStatistics | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetch = useCallback(async () => {
        if (!year || !month) return
        setLoading(true); setError(null)
        try {
            setStatistics(await paymentApi.getMonthlyStatistics(year, month))
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('statistics.fetchError')
            setError(msg); toast.error(t('statistics.fetchError'))
        } finally { setLoading(false) }
    }, [year, month, t])

    useEffect(() => { fetch() }, [fetch])
    return { statistics, loading, error, refetch: fetch }
}

