import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { scheduleApi } from '@services/api/modules/schedule.api'
import type { InstallmentSchedule, ScheduleFilters } from '@/types/modules/schedule.types'

interface UseSchedulesReturn {
    schedules: InstallmentSchedule[]
    loading: boolean
    error: string | null
    totalPages: number
    totalElements: number
    refetch: () => void
}

/** Fetch installment schedules using backend endpoints + fallback strategy in `scheduleApi.query`. */
export function useSchedules(filters: ScheduleFilters = {}): UseSchedulesReturn {
    const { t } = useTranslation('schedule')
    const [schedules, setSchedules] = useState<InstallmentSchedule[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [totalPages, setTotalPages] = useState(0)
    const [totalElements, setTotalElements] = useState(0)

    const fetch = useCallback(async () => {
        setLoading(true)
        setError(null)

        try {
            const res = await scheduleApi.query(filters)
            setSchedules(res.content)
            setTotalPages(res.totalPages)
            setTotalElements(res.totalElements)
        } catch (err) {
            const message = err instanceof Error ? err.message : t('fetchError')
            setError(message)
            toast.error(t('fetchError'))
            setSchedules([])
            setTotalPages(0)
            setTotalElements(0)
        } finally {
            setLoading(false)
        }
    }, [filters, t])

    useEffect(() => { fetch() }, [fetch])

    return { schedules, loading, error, totalPages, totalElements, refetch: fetch }
}

