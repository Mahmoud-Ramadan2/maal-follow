import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { scheduleApi } from '@services/api/modules/schedule.api'
import type { MonthlyCollectionSummary } from '@/types/modules/schedule.types'

interface UseScheduleMonthlySummaryReturn {
    summary: MonthlyCollectionSummary | null
    loading: boolean
    error: string | null
    refetch: () => void
}

export function useScheduleMonthlySummary(month: string): UseScheduleMonthlySummaryReturn {
    const { t } = useTranslation('schedule')
    const [summary, setSummary] = useState<MonthlyCollectionSummary | null>(null)
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const fetch = useCallback(async () => {
        if (!month) {
            setSummary(null)
            setError(null)
            return
        }

        setLoading(true)
        setError(null)
        try {
            const data = await scheduleApi.getMonthlySummary(month)
            setSummary(data)
        } catch (err) {
            const message = err instanceof Error ? err.message : t('monthlySummary.fetchError')
            setError(message)
            setSummary(null)
        } finally {
            setLoading(false)
        }
    }, [month, t])

    useEffect(() => { fetch() }, [fetch])

    return { summary, loading, error, refetch: fetch }
}

