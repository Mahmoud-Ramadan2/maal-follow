import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { paymentApi } from '@services/api/modules/payment.api'
import type { Payment, PaymentRequest } from '@/types/modules/payment.types'

export function usePaymentCreate() {
    const { t } = useTranslation('payment')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const createPayment = useCallback(async (data: PaymentRequest): Promise<Payment | null> => {
        setLoading(true); setError(null)
        try {
            const created = await paymentApi.create(data)
            toast.success(t('created'))
            return created
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('createError')
            setError(msg); toast.error(t('createError'))
            return null
        } finally { setLoading(false) }
    }, [t])

    return { createPayment, loading, error }
}

