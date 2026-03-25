import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { installmentScheduleApi } from '@services/api/modules/installmentSchedule.api'
import type { InstallmentSchedule } from '@/types/modules/contract.types'

interface UseInstallmentSchedulesReturn {
    schedules: InstallmentSchedule[]
    loading: boolean
    error: string | null
    refetch: () => void
}

/** Fetches all installment schedules for a given contract. */
export function useInstallmentSchedules(contractId: number): UseInstallmentSchedulesReturn {
    const { t } = useTranslation('contract')
    const [schedules, setSchedules] = useState<InstallmentSchedule[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetch = useCallback(async () => {
        if (!contractId) return
        setLoading(true)
        setError(null)
        try {
            const data = await installmentScheduleApi.getByContract(contractId)
            setSchedules(data)
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('schedule.fetchError')
            setError(msg)
            toast.error(t('schedule.fetchError'))
        } finally {
            setLoading(false)
        }
    }, [contractId, t])

    useEffect(() => { fetch() }, [fetch])

    return { schedules, loading, error, refetch: fetch }
}

