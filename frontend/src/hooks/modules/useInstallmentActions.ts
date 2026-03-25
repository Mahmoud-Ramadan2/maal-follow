import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { installmentScheduleApi } from '@services/api/modules/installmentSchedule.api'
import type {
    InstallmentSchedule, GenerateScheduleParams, RescheduleParams,
} from '@/types/modules/contract.types'

interface UseInstallmentActionsReturn {
    generate: (contractId: number) => Promise<InstallmentSchedule[] | null>
    generateCustom: (contractId: number, params: GenerateScheduleParams) => Promise<InstallmentSchedule[] | null>
    swapRemainder: (contractId: number) => Promise<InstallmentSchedule[] | null>
    deleteUnpaid: (contractId: number) => Promise<boolean>
    reschedule: (contractId: number, params: RescheduleParams) => Promise<InstallmentSchedule[] | null>
    skipMonth: (contractId: number, reason: string) => Promise<boolean>
    loading: boolean
    error: string | null
}

/**
 * Provides action functions for generating, rescheduling,
 * swapping, and managing installment schedules.
 */
export function useInstallmentActions(): UseInstallmentActionsReturn {
    const { t } = useTranslation('contract')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const generate = useCallback(async (contractId: number) => {
        setLoading(true); setError(null)
        try {
            const res = await installmentScheduleApi.generate(contractId)
            toast.success(t('schedule.generated'))
            return res
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('schedule.generateError')
            setError(msg); toast.error(msg)
            return null
        } finally { setLoading(false) }
    }, [t])

    const generateCustom = useCallback(async (contractId: number, params: GenerateScheduleParams) => {
        setLoading(true); setError(null)
        try {
            const res = await installmentScheduleApi.generateCustom(contractId, params)
            toast.success(t('schedule.generated'))
            return res
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('schedule.generateError')
            setError(msg); toast.error(msg)
            return null
        } finally { setLoading(false) }
    }, [t])

    const swapRemainder = useCallback(async (contractId: number) => {
        setLoading(true); setError(null)
        try {
            const res = await installmentScheduleApi.swapRemainder(contractId)
            toast.success(t('schedule.swapped'))
            return res
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('schedule.swapError')
            setError(msg); toast.error(msg)
            return null
        } finally { setLoading(false) }
    }, [t])

    const deleteUnpaid = useCallback(async (contractId: number) => {
        const confirmed = window.confirm(t('schedule.deleteUnpaidConfirm'))
        if (!confirmed) return false
        setLoading(true); setError(null)
        try {
            await installmentScheduleApi.deleteUnpaid(contractId)
            toast.success(t('schedule.unpaidDeleted'))
            return true
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('schedule.deleteError')
            setError(msg); toast.error(msg)
            return false
        } finally { setLoading(false) }
    }, [t])

    const reschedule = useCallback(async (contractId: number, params: RescheduleParams) => {
        setLoading(true); setError(null)
        try {
            const res = await installmentScheduleApi.reschedule(contractId, params)
            toast.success(t('schedule.rescheduled'))
            return res
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('schedule.rescheduleError')
            setError(msg); toast.error(msg)
            return null
        } finally { setLoading(false) }
    }, [t])

    const skipMonth = useCallback(async (contractId: number, reason: string) => {
        setLoading(true); setError(null)
        try {
            await installmentScheduleApi.skipMonth(contractId, reason)
            toast.success(t('schedule.monthSkipped'))
            return true
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('schedule.skipError')
            setError(msg); toast.error(msg)
            return false
        } finally { setLoading(false) }
    }, [t])

    return { generate, generateCustom, swapRemainder, deleteUnpaid, reschedule, skipMonth, loading, error }
}

