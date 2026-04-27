import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { paymentApi } from '@services/api/modules/payment.api'
import type { Payment } from '@/types/modules/payment.types'

export function usePaymentActions() {
    const { t } = useTranslation('payment')
    const cancelConfirmText = t('cancelConfirm')
    const cancelledText = t('cancelled')
    const cancelErrorText = t('cancelError')
    const refundConfirmText = t('refundConfirm')
    const refundedText = t('refunded')
    const refundErrorText = t('refundError')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const cancelPayment = useCallback(async (id: number, reason?: string): Promise<Payment | null> => {
        const confirmed = window.confirm(cancelConfirmText)
        if (!confirmed) return null
        setLoading(true); setError(null)
        try {
            const result = await paymentApi.cancel(id, reason)
            toast.success(cancelledText)
            return result
        } catch (err) {
            const msg = err instanceof Error ? err.message : cancelErrorText
            setError(msg); toast.error(cancelErrorText)
            return null
        } finally { setLoading(false) }
    }, [cancelConfirmText, cancelledText, cancelErrorText])

    const refundPayment = useCallback(async (id: number, reason?: string): Promise<Payment | null> => {
        const confirmed = window.confirm(refundConfirmText)
        if (!confirmed) return null
        setLoading(true); setError(null)
        try {
            const result = await paymentApi.refund(id, reason)
            toast.success(refundedText)
            return result
        } catch (err) {
            const msg = err instanceof Error ? err.message : refundErrorText
            setError(msg); toast.error(refundErrorText)
            return null
        } finally { setLoading(false) }
    }, [refundConfirmText, refundedText, refundErrorText])

    return { cancelPayment, refundPayment, loading, error }
}

