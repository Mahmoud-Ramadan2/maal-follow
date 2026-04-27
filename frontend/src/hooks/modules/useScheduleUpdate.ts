import { useCallback, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { scheduleApi } from '@services/api/modules/schedule.api'
import type { InstallmentSchedule, ScheduleMetadataUpdateRequest } from '@/types/modules/schedule.types'

interface UseScheduleUpdateReturn {
    updateSchedule: (id: number, data: ScheduleMetadataUpdateRequest) => Promise<InstallmentSchedule | null>
    loading: boolean
    error: string | null
}

/** Updates a single installment schedule row using metadata-only patch. */
export function useScheduleUpdate(): UseScheduleUpdateReturn {
    const { t } = useTranslation('schedule')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const updateSchedule = useCallback(async (id: number, data: ScheduleMetadataUpdateRequest) => {
        setLoading(true)
        setError(null)
        try {
            const updated = await scheduleApi.updateMetadata(id, data)
            toast.success(t('updated'))
            return updated
        } catch (err) {
            const message = err instanceof Error ? err.message : t('updateError')
            setError(message)
            toast.error(message)
            return null
        } finally {
            setLoading(false)
        }
    }, [t])

    return { updateSchedule, loading, error }
}

