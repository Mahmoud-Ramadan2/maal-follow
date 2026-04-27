import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { paymentApi } from '@services/api/modules/payment.api'
import type { Payment, PaymentRequest } from '@/types/modules/payment.types'

export function usePaymentCreate() {
    const { t } = useTranslation('payment')
    const createdText = t('created')
    const createErrorText = t('createError')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const createPayment = useCallback(async (data: PaymentRequest): Promise<Payment | null> => {
        setLoading(true); setError(null)
        try {
            const created = await paymentApi.create(data)
            toast.success(createdText)
            return created
        } catch (err) {
            const msg = err instanceof Error ? err.message : createErrorText
            setError(msg); toast.error(createErrorText)
            return null
        } finally { setLoading(false) }
    }, [createdText, createErrorText])

    return { createPayment, loading, error }
}

