import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { scheduleApi } from '@services/api/modules/schedule.api'
import type {
    InstallmentSchedule, GenerateScheduleParams, RescheduleParams, ScheduleMetadataUpdateRequest,
} from '@/types/modules/schedule.types'

interface UseInstallmentActionsReturn {
    generate: (contractId: number) => Promise<InstallmentSchedule[] | null>
    generateCustom: (contractId: number, params: GenerateScheduleParams) => Promise<InstallmentSchedule[] | null>
    swapRemainder: (contractId: number) => Promise<InstallmentSchedule[] | null>
    deleteUnpaid: (contractId: number) => Promise<boolean>
    updateScheduleMetadata: (scheduleId: number, data: ScheduleMetadataUpdateRequest) => Promise<InstallmentSchedule | null>
    rescheduleUnpaidInstallments: (contractId: number, params: RescheduleParams) => Promise<InstallmentSchedule[] | null>
    skipMonthPayment: (contractId: number, reason: string) => Promise<boolean>
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
            const res = await scheduleApi.generate(contractId)
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
            const res = await scheduleApi.generateCustom(contractId, params)
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
            const res = await scheduleApi.swapRemainder(contractId)
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
            await scheduleApi.deleteUnpaid(contractId)
            toast.success(t('schedule.unpaidDeleted'))
            return true
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('schedule.deleteError')
            setError(msg); toast.error(msg)
            return false
        } finally { setLoading(false) }
    }, [t])

    const updateScheduleMetadata = useCallback(async (scheduleId: number, data: ScheduleMetadataUpdateRequest) => {
        setLoading(true); setError(null)
        try {
            const res = await scheduleApi.updateMetadata(scheduleId, data)
            toast.success(t('schedule.metadataUpdated'))
            return res
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('schedule.metadataUpdateError')
            setError(msg); toast.error(msg)
            return null
        } finally { setLoading(false) }
    }, [t])

    const rescheduleUnpaidInstallments = useCallback(async (contractId: number, params: RescheduleParams) => {
        setLoading(true); setError(null)
        try {
            const res = await scheduleApi.reschedule(contractId, params)
            toast.success(t('schedule.rescheduled'))
            return res
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('schedule.rescheduleError')
            setError(msg); toast.error(msg)
            return null
        } finally { setLoading(false) }
    }, [t])

    const skipMonthPayment = useCallback(async (contractId: number, reason: string) => {
        setLoading(true); setError(null)
        try {
            await scheduleApi.skipMonth(contractId, reason)
            toast.success(t('schedule.monthSkipped'))
            return true
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('schedule.skipError')
            setError(msg); toast.error(msg)
            return false
        } finally { setLoading(false) }
    }, [t])

    // Backward-compatible aliases
    const reschedule = rescheduleUnpaidInstallments
    const skipMonth = skipMonthPayment

    return {
        generate,
        generateCustom,
        swapRemainder,
        deleteUnpaid,
        updateScheduleMetadata,
        rescheduleUnpaidInstallments,
        skipMonthPayment,
        reschedule,
        skipMonth,
        loading,
        error,
    }
}

