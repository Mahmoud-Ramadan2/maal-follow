import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { contractApi } from '@services/api/modules/contract.api'
import { installmentScheduleApi } from '@services/api/modules/installmentSchedule.api'
import { contractExpenseApi } from '@services/api/modules/contractExpense.api'
import type { Contract, InstallmentSchedule, ContractExpense } from '@/types/modules/contract.types'

interface UseContractReturn {
    contract: Contract | null
    schedules: InstallmentSchedule[]
    expenses: ContractExpense[]
    loading: boolean
    error: string | null
    refetch: () => void
}

/**
 * Fetches a single contract by ID along with its
 * installment schedules and expenses in parallel.
 */
export function useContract(id: number): UseContractReturn {
    const { t } = useTranslation('contract')
    const [contract, setContract] = useState<Contract | null>(null)
    const [schedules, setSchedules] = useState<InstallmentSchedule[]>([])
    const [expenses, setExpenses] = useState<ContractExpense[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetch = useCallback(async () => {
        if (!id) return
        setLoading(true)
        setError(null)
        try {
            const contractData = await contractApi.getById(id)
            console.log('Fetched contract:', contractData)
            setContract(contractData)

            // Fetch sub-entities in parallel using contractId
            const [sched, exp] = await Promise.all([
                installmentScheduleApi.getByContract(id).catch(() => [] as InstallmentSchedule[]),
                contractExpenseApi.getByContract(id).catch(() => [] as ContractExpense[]),
            ])
            setSchedules(sched)
            setExpenses(exp)
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('fetchOneError')
            setError(msg)
            toast.error(t('fetchOneError'))
        } finally {
            setLoading(false)
        }
    }, [id, t])

    useEffect(() => { fetch() }, [fetch])

    return { contract, schedules, expenses, loading, error, refetch: fetch }
}

