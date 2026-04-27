import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { scheduleApi } from '@services/api/modules/schedule.api'
import type { PaginatedResponse } from '@/types/common.types'
import type { InstallmentSchedule } from '@/types/modules/schedule.types'

interface UseInstallmentSchedulesReturn {
    schedules: InstallmentSchedule[]
    page: number
    size: number
    totalPages: number
    totalElements: number
    loading: boolean
    error: string | null
    refetch: () => void
}

interface UseInstallmentSchedulesOptions {
    page?: number
    size?: number
    sort?: string
}

/** Fetches all installment schedules for a given contract. */
export function useInstallmentSchedules(
    contractId: number,
    options: UseInstallmentSchedulesOptions = {},
): UseInstallmentSchedulesReturn {
    const { t } = useTranslation('contract')
    const { page = 0, size = 10, sort } = options
    const [schedules, setSchedules] = useState<InstallmentSchedule[]>([])
    const [totalPages, setTotalPages] = useState(0)
    const [totalElements, setTotalElements] = useState(0)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetch = useCallback(async () => {
        if (!contractId) return
        setLoading(true)
        setError(null)
        try {
            const data = await scheduleApi.getByContract(contractId, page, size, sort)
            setSchedules(data.content)
            setTotalPages(data.totalPages)
            setTotalElements(data.totalElements)
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('schedule.fetchError')
            setError(msg)
            toast.error(t('schedule.fetchError'))
            const empty: PaginatedResponse<InstallmentSchedule> = {
                content: [],
                totalElements: 0,
                totalPages: 0,
                size,
                number: page,
                first: page === 0,
                last: true,
                empty: true,
            }
            setSchedules(empty.content)
            setTotalPages(empty.totalPages)
            setTotalElements(empty.totalElements)
        } finally {
            setLoading(false)
        }
    }, [contractId, page, size, sort, t])

    useEffect(() => { fetch() }, [fetch])

    return { schedules, page, size, totalPages, totalElements, loading, error, refetch: fetch }
}

