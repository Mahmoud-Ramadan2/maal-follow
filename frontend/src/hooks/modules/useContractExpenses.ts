import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { contractExpenseApi } from '@services/api/modules/contractExpense.api'
import type { ContractExpense } from '@/types/modules/contract.types'

interface UseContractExpensesReturn {
    expenses: ContractExpense[]
    loading: boolean
    error: string | null
    refetch: () => void
}

/** Fetches all expenses for a given contract. */
export function useContractExpenses(contractId: number): UseContractExpensesReturn {
    const { t } = useTranslation('contract')
    const [expenses, setExpenses] = useState<ContractExpense[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetch = useCallback(async () => {
        if (!contractId) return
        setLoading(true); setError(null)
        try {
            setExpenses(await contractExpenseApi.getByContract(contractId))
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('expense.fetchError')
            setError(msg); toast.error(msg)
        } finally { setLoading(false) }
    }, [contractId, t])

    useEffect(() => { fetch() }, [fetch])
    return { expenses, loading, error, refetch: fetch }
}

