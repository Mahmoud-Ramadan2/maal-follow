import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { contractExpenseApi } from '@services/api/modules/contractExpense.api'
import type { ContractExpense, ContractExpenseRequest } from '@/types/modules/contract.types'

interface UseContractExpenseActionsReturn {
    createExpense: (data: ContractExpenseRequest) => Promise<ContractExpense | null>
    updateExpense: (id: number, data: ContractExpenseRequest) => Promise<ContractExpense | null>
    deleteExpense: (id: number) => Promise<boolean>
    loading: boolean
    error: string | null
}

/** CRUD actions for contract expenses with toast feedback. */
export function useContractExpenseActions(): UseContractExpenseActionsReturn {
    const { t } = useTranslation('contract')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const createExpense = useCallback(async (data: ContractExpenseRequest) => {
        setLoading(true); setError(null)
        try {
            const created = await contractExpenseApi.create(data)
            toast.success(t('expense.created'))
            return created
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('expense.createError')
            setError(msg); toast.error(msg)
            return null
        } finally { setLoading(false) }
    }, [t])

    const updateExpense = useCallback(async (id: number, data: ContractExpenseRequest) => {
        setLoading(true); setError(null)
        try {
            const updated = await contractExpenseApi.update(id, data)
            toast.success(t('expense.updated'))
            return updated
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('expense.updateError')
            setError(msg); toast.error(msg)
            return null
        } finally { setLoading(false) }
    }, [t])

    const deleteExpense = useCallback(async (id: number) => {
        const confirmed = window.confirm(t('expense.deleteConfirm'))
        if (!confirmed) return false
        setLoading(true); setError(null)
        try {
            await contractExpenseApi.delete(id)
            toast.success(t('expense.deleted'))
            return true
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('expense.deleteError')
            setError(msg); toast.error(msg)
            return false
        } finally { setLoading(false) }
    }, [t])

    return { createExpense, updateExpense, deleteExpense, loading, error }
}

