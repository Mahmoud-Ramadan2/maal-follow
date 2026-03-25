import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { paymentApi } from '@services/api/modules/payment.api'
import type { Payment } from '@/types/modules/payment.types'

export function usePaymentActions() {
    const { t } = useTranslation('payment')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const cancelPayment = useCallback(async (id: number, reason?: string): Promise<Payment | null> => {
        const confirmed = window.confirm(t('cancelConfirm'))
        if (!confirmed) return null
        setLoading(true); setError(null)
        try {
            const result = await paymentApi.cancel(id, reason)
            toast.success(t('cancelled'))
            return result
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('cancelError')
            setError(msg); toast.error(t('cancelError'))
            return null
        } finally { setLoading(false) }
    }, [t])

    const refundPayment = useCallback(async (id: number, reason?: string): Promise<Payment | null> => {
        const confirmed = window.confirm(t('refundConfirm'))
        if (!confirmed) return null
        setLoading(true); setError(null)
        try {
            const result = await paymentApi.refund(id, reason)
            toast.success(t('refunded'))
            return result
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('refundError')
            setError(msg); toast.error(t('refundError'))
            return null
        } finally { setLoading(false) }
    }, [t])

    return { cancelPayment, refundPayment, loading, error }
}

