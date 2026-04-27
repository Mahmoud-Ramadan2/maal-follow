import { useCallback, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'

import { paymentApi } from '@services/api/modules/payment.api'
import type { InstallmentSchedule } from '@/types/modules/schedule.types'
import { APP_ROUTES } from '@/router/routes.config'

interface UseSchedulePaymentReturn {
    openPaymentForm: (schedule: InstallmentSchedule) => void
    sendReminder: (schedule: InstallmentSchedule) => Promise<boolean>
    loading: boolean
    error: string | null
}

/**
 * Shared actions for schedule-driven payment workflows:
 * - Navigate to prefilled payment create form
 * - Trigger reminder jobs from schedule context
 */
export function useSchedulePayment(): UseSchedulePaymentReturn {
    const { t } = useTranslation('schedule')
    const navigate = useNavigate()
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const openPaymentForm = useCallback((schedule: InstallmentSchedule) => {
        if (!schedule.id) {
            toast.error(t('pay.missingScheduleId'))
            return
        }
        const  amountToPaid = schedule.status === 'PAID' ? 0 : schedule.amount - schedule.paidAmount
        const params = new URLSearchParams({
            scheduleId: String(schedule.id),
            amount: String(amountToPaid),
            contractId: String(schedule.contractId),
            customerName: schedule.customerName,
            dueDate: schedule.dueDate,
        })

        navigate(`${APP_ROUTES.PAYMENTS.CREATE}?${params.toString()}`)
    }, [navigate, t])

    const sendReminder = useCallback(async (schedule: InstallmentSchedule): Promise<boolean> => {
        if (!schedule.id) {
            toast.error(t('pay.missingScheduleId'))
            return false
        }

        setLoading(true)
        setError(null)
        try {
            // Backend currently exposes batch reminder jobs. Keep this action available
            // and add a clear note that schedule-specific reminder endpoint is planned.
            await paymentApi.createReminders()
            await paymentApi.sendPendingReminders()
            toast.success(t('reminders.sent'))
            toast.info(t('reminders.scheduleSpecificComingSoon'))
            return true
        } catch (err) {
            const message = err instanceof Error ? err.message : t('reminders.failed')
            setError(message)
            toast.error(message)
            return false
        } finally {
            setLoading(false)
        }
    }, [t])

    return {
        openPaymentForm,
        sendReminder,
        loading,
        error,
    }
}


