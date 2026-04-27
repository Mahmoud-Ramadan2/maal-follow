import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { scheduleApi } from '@services/api/modules/schedule.api'
import type { InstallmentSchedule } from '@/types/modules/schedule.types'

interface UseScheduleReturn {
    schedule: InstallmentSchedule | null
    loading: boolean
    error: string | null
    refetch: () => void
}

/**
 * Fetch one schedule by ID using the direct endpoint.
 */
export function useSchedule(scheduleId: number | null): UseScheduleReturn {
    const { t } = useTranslation('schedule')
    const [schedule, setSchedule] = useState<InstallmentSchedule | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetch = useCallback(async () => {
        if (!scheduleId) return

        setLoading(true)
        setError(null)
        try {
            const scheduleData = await scheduleApi.getById(scheduleId)
            setSchedule(scheduleData)
            console.log("Fetched schedule:", scheduleData)
        } catch (err) {
            const message = err instanceof Error ? err.message : t('fetchOneError')
            setError(message)
            toast.error(t('fetchOneError'))
        } finally {
            setLoading(false)
        }
    }, [scheduleId, t])

    useEffect(() => { fetch() }, [fetch])

    return { schedule, loading, error, refetch: fetch }
}

