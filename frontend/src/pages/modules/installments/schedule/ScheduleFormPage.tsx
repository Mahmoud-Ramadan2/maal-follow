import { useEffect, useMemo } from 'react'
import { toast } from 'react-toastify'
import type { ReactNode } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'

import {useCollectors, useSchedule, useScheduleCreate, useScheduleUpdate} from '@hooks/modules'
import { scheduleCreateSchema } from '@utils/validators/schedule.validator'
import type { ScheduleFormData } from '@utils/validators/schedule.validator'
import type { InstallmentSchedule, ScheduleMetadataUpdateRequest } from '@/types/modules/schedule.types'
import Input from '@components/common/Input'
import Button from '@components/common/Button'
import Card from '@components/ui/Card'
import { APP_ROUTES } from '@/router/routes.config'
import './ScheduleFormPage.css'

interface ScheduleFormPageProps {
    scheduleId?: number
}

interface LocationState {
    schedule?: InstallmentSchedule
}

export default function ScheduleFormPage({ scheduleId }: ScheduleFormPageProps): ReactNode {
    const { t } = useTranslation('schedule')
    const { t: tu } = useTranslation('user')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()
    const location = useLocation()
    const state = location.state as LocationState | null

    const isEditMode = scheduleId !== undefined
    const sourceSchedule = state?.schedule
    // Fetch existing schedule if edit page was opened directly (no location state).
    const scheduleQuery = useSchedule(isEditMode ? scheduleId : 0)
    const { createSchedule, loading: creating } = useScheduleCreate()
    const { updateSchedule, loading: updating } = useScheduleUpdate()
    const { collectors, loading: collectorsLoading } = useCollectors(['COLLECTOR', 'MANAGER', 'ADMIN'])
    const editSchedule = sourceSchedule ?? scheduleQuery.schedule ?? undefined

    const mappedEditDefaults = useMemo<ScheduleFormData | null>(() => {
        if (!editSchedule) return null

        return {
            // contractId is immutable in edit mode
            contractId: editSchedule.contractId,
            sequenceNumber: editSchedule.sequenceNumber,
            profitMonth: editSchedule.profitMonth,
            amount: editSchedule.amount,
            dueDate: editSchedule.dueDate,
            discountApplied: editSchedule.discountApplied,
            isFinalPayment: editSchedule.isFinalPayment,
            status: editSchedule.status,
            collectorId: editSchedule.collectorId ?? undefined,
            notes: editSchedule.notes ?? '',
        }
    }, [editSchedule])

    const {
        register,
        handleSubmit,
        reset,
        setValue,
        formState: { errors },
    } = useForm<ScheduleFormData>({
        resolver: zodResolver(scheduleCreateSchema),
        defaultValues: {
            contractId: 0,
            sequenceNumber: 1,
            profitMonth: new Date().toISOString().slice(0, 7),
            amount: 0,
            dueDate: new Date().toISOString().split('T')[0] ?? '',
            discountApplied: 0,
            isFinalPayment: false,
            status: 'PENDING',
            collectorId: undefined,
            notes: '',
        },
    })

    useEffect(() => {
        if (!isEditMode || !mappedEditDefaults) return
        reset(mappedEditDefaults)
    }, [isEditMode, mappedEditDefaults, reset])

    useEffect(() => {
        if (!isEditMode || !editSchedule?.collectorName || editSchedule.collectorId || collectors.length === 0) return
        const matchedCollector = collectors.find((collector) => collector.name === editSchedule.collectorName)
        if (matchedCollector) {
            setValue('collectorId', matchedCollector.id)
        }
    }, [isEditMode, editSchedule, collectors, setValue])

    const onSubmit = async (data: ScheduleFormData) => {
        if (isEditMode) {
            if (!editSchedule) {
                toast.error(t('form.missingEditData'))
                return
            }

            if (!scheduleId) {
                toast.error(t('form.missingEditData'))
                return
            }

            const confirmed = window.confirm(t('form.updateConfirm'))
            if (!confirmed) return

            const metadataPayload: ScheduleMetadataUpdateRequest = {
                dueDate: data.dueDate,
                notes: data.notes || undefined,
                collectorId: data.collectorId,
                clearCollector: data.collectorId === undefined,
            }

            const result = await updateSchedule(scheduleId, metadataPayload)
            if (!result) return
            navigate(APP_ROUTES.SCHEDULES.LIST)
            return
        }

        const result = await createSchedule(data)
        if (!result) return
        navigate(APP_ROUTES.SCHEDULES.LIST)
    }

    const loading = creating || updating || (isEditMode && scheduleQuery.loading && !editSchedule)

    return (
        <div>
            <div className="schedule-form__header">
                <h1 className="schedule-form__title">
                    {isEditMode ? t('form.editTitle') : t('form.createTitle')}
                </h1>
            </div>

            <Card>
                <form className="schedule-form" onSubmit={handleSubmit(onSubmit)}>
                    {isEditMode && (
                        <div className="schedule-form__hint" role="status">
                            {t('form.editDataNotice')}
                        </div>
                    )}
                    {isEditMode && (
                        <div className="schedule-form__info" role="status">
                            <strong>{t('form.metadataOnlyEditTitle')}</strong>: {t('form.metadataOnlyEditDescription')}
                        </div>
                    )}

                    <Input
                        label={t('form.contractId')}
                        type="number"
                        disabled={isEditMode}
                        error={errors.contractId?.message}
                        {...register('contractId', { valueAsNumber: true })}
                    />
                    <Input
                        label={t('form.sequenceNumber')}
                        type="number"
                        disabled={isEditMode}
                        error={errors.sequenceNumber?.message}
                        {...register('sequenceNumber', { valueAsNumber: true })}
                    />
                    <Input
                        label={t('form.profitMonth')}
                        placeholder="YYYY-MM"
                        disabled={isEditMode}
                        error={errors.profitMonth?.message}
                        {...register('profitMonth')}
                    />
                    <Input
                        label={t('form.amount')}
                        type="number"
                        step="0.01"
                        disabled={isEditMode}
                        error={errors.amount?.message}
                        {...register('amount', { valueAsNumber: true })}
                    />
                    <Input
                        label={t('form.dueDate')}
                        type="date"
                        error={errors.dueDate?.message}
                        {...register('dueDate')}
                    />
                    <Input
                        label={t('form.discountApplied')}
                        type="number"
                        step="0.01"
                        disabled={isEditMode}
                        error={errors.discountApplied?.message}
                        {...register('discountApplied', { valueAsNumber: true })}
                    />
                    <div className="schedule-form__field">
                        <label htmlFor="status" className="schedule-form__label">{t('form.status')}</label>
                        <select id="status" className="schedule-form__select" disabled={isEditMode} {...register('status')}>
                            <option value="PENDING">{t('status.PENDING')}</option>
                            <option value="PAID">{t('status.PAID')}</option>
                            <option value="LATE">{t('status.LATE')}</option>
                            <option value="PARTIALLY_PAID">{t('status.PARTIALLY_PAID')}</option>
                            <option value="CANCELLED">{t('status.CANCELLED')}</option>
                        </select>
                    </div>
                    <div className="schedule-form__field">
                        <label htmlFor="collectorId" className="schedule-form__label">{t('form.collectorId')}</label>
                        <select
                            id="collectorId"
                            className="schedule-form__select"
                            disabled={collectorsLoading || isEditMode}
                            {...register('collectorId', {
                                setValueAs: (value) => {
                                    if (value === '' || value === null || value === undefined) return undefined
                                    return Number(value)
                                },
                            })}
                        >
                            <option value="">{t('form.collectorPlaceholder')}</option>
                            {collectors.map((collector) => (
                                <option key={collector.id} value={collector.id}>
                                    {collector.name} - {tu(`role.${collector.role}`, collector.role)}
                                </option>
                            ))}
                        </select>
                        {errors.collectorId?.message && <span className="schedule-form__error">{errors.collectorId.message}</span>}
                    </div>
                    <div className="schedule-form__field schedule-form__field--full">
                        <label htmlFor="notes" className="schedule-form__label">{t('form.notes')}</label>
                        <textarea id="notes" className="schedule-form__textarea" rows={4} {...register('notes')} />
                        {errors.notes?.message && <span className="schedule-form__error">{errors.notes.message}</span>}
                    </div>
                    <div className="schedule-form__field schedule-form__checkbox">
                        <label>
                            <input type="checkbox" disabled={isEditMode} {...register('isFinalPayment')} /> {t('form.isFinalPayment')}
                        </label>
                    </div>

                    <div className="schedule-form__actions">
                        <Button type="button" variant="secondary" onClick={() => navigate(APP_ROUTES.SCHEDULES.LIST)}>
                            {tc('cancel')}
                        </Button>
                        <Button type="submit" loading={loading}>
                            {isEditMode ? t('form.submitUpdate') : t('form.submitCreate')}
                        </Button>
                    </div>
                </form>
            </Card>
        </div>
    )
}

