import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { contractApi } from '@services/api/modules/contract.api'
import { scheduleApi } from '@services/api/modules/schedule.api'
import { contractExpenseApi } from '@services/api/modules/contractExpense.api'
import type { PaginatedResponse } from '@/types/common.types'
import type { Contract, ContractExpense } from '@/types/modules/contract.types'
import type { InstallmentSchedule, ScheduleFilters } from '@/types/modules/schedule.types'

interface UseContractReturn {
    contract: Contract | null
    schedules: InstallmentSchedule[]
    schedulePage: number
    scheduleSize: number
    scheduleTotalPages: number
    scheduleTotalElements: number
    expenses: ContractExpense[]
    loading: boolean
    error: string | null
    refetch: () => void
}

interface UseContractOptions {
    schedulePage?: number
    scheduleSize?: number
    scheduleFilters?: Omit<ScheduleFilters, 'contractId' | 'page' | 'size'>
}

/**
 * Fetches a single contract by ID along with its
 * installment schedules and expenses in parallel.
 */
export function useContract(id: number, options: UseContractOptions = {}): UseContractReturn {
    const { t } = useTranslation('contract')
    const { schedulePage = 0, scheduleSize = 10, scheduleFilters } = options
    const [contract, setContract] = useState<Contract | null>(null)
    const [schedules, setSchedules] = useState<InstallmentSchedule[]>([])
    const [scheduleTotalPages, setScheduleTotalPages] = useState(0)
    const [scheduleTotalElements, setScheduleTotalElements] = useState(0)
    const [expenses, setExpenses] = useState<ContractExpense[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetch = useCallback(async () => {
        if (!id) return
        setLoading(true)
        setError(null)
        try {
            const contractData = await contractApi.getById(id)
            // console.log('Fetched contract:', contractData)
            setContract(contractData)

            // Fetch sub-entities in parallel using contractId
            const [sched, exp] = await Promise.all([
                scheduleApi.search({
                    page: schedulePage,
                    size: scheduleSize,
                    contractId: id,
                    ...scheduleFilters,
                }).catch(async (): Promise<PaginatedResponse<InstallmentSchedule>> => scheduleApi.getByContract(id, schedulePage, scheduleSize).catch((): PaginatedResponse<InstallmentSchedule> => ({
                    content: [],
                    totalElements: 0,
                    totalPages: 0,
                    size: scheduleSize,
                    number: schedulePage,
                    first: schedulePage === 0,
                    last: true,
                    empty: true,
                }))),
                contractExpenseApi.getByContract(id).catch(() => [] as ContractExpense[]),
            ])
            setSchedules(sched.content)
            setScheduleTotalPages(sched.totalPages)
            setScheduleTotalElements(sched.totalElements)
            setExpenses(exp)
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('fetchOneError')
            setError(msg)
            console.log('Error fetching contract:', err)
            toast.error(t('fetchOneError'))
        } finally {
            setLoading(false)
        }
    }, [id, scheduleFilters, schedulePage, scheduleSize, t])

    useEffect(() => { fetch() }, [fetch])

    return {
        contract,
        schedules,
        schedulePage,
        scheduleSize,
        scheduleTotalPages,
        scheduleTotalElements,
        expenses,
        loading,
        error,
        refetch: fetch,
    }
}

