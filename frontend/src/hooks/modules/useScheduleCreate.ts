import { useCallback, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { scheduleApi } from '@services/api/modules/schedule.api'
import type { InstallmentSchedule, InstallmentScheduleRequest } from '@/types/modules/schedule.types'

interface UseScheduleCreateReturn {
    createSchedule: (data: InstallmentScheduleRequest) => Promise<InstallmentSchedule | null>
    loading: boolean
    error: string | null
}

/** Creates a single installment schedule row manually. */
export function useScheduleCreate(): UseScheduleCreateReturn {
    const { t } = useTranslation('schedule')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const createSchedule = useCallback(async (data: InstallmentScheduleRequest) => {
        setLoading(true)
        setError(null)
        try {
            const created = await scheduleApi.create(data)
            toast.success(t('created'))
            return created
        } catch (err) {
            const message = err instanceof Error ? err.message : t('createError')
            setError(message)
            toast.error(message)
            return null
        } finally {
            setLoading(false)
        }
    }, [t])

    return { createSchedule, loading, error }
}

