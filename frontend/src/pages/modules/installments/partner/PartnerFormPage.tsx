import {useEffect, useMemo} from 'react'
import type {ReactNode} from 'react'
import {useNavigate} from 'react-router-dom'
import {useTranslation} from 'react-i18next'
import {useForm, useWatch} from 'react-hook-form'
import {zodResolver} from '@hookform/resolvers/zod'

import {usePartner, usePartnerCreate, usePartnerUpdate} from '@hooks/modules'
import {partnerCreateSchema} from '@utils/validators/partner.validator'
import type {PartnerFormData} from '@utils/validators/partner.validator'
import {PartnershipType} from '@/types/modules/partner.types'
import type {PartnerRequest} from '@/types/modules/partner.types'
import Button from '@components/common/Button'
import Input from '@components/common/Input'
import Card from '@components/ui/Card'
import LoadingSpinner from '@components/ui/LoadingSpinner'
import {APP_ROUTES} from '@/router/routes.config'
import './PartnerFormPage.css'

interface PartnerFormPageProps {
    partnerId?: number
}

export default function PartnerFormPage({partnerId}: PartnerFormPageProps): ReactNode {
    const {t} = useTranslation('partner')
    const {t: tc} = useTranslation('common')
    const navigate = useNavigate()
    const isEditMode = partnerId !== undefined

    const {partner: existing, loading: fetchLoading} = usePartner(isEditMode ? partnerId : 0)
    const {createPartner, loading: createLoading} = usePartnerCreate()
    const {updatePartner, loading: updateLoading} = usePartnerUpdate()
    const submitting = createLoading || updateLoading

    const currentDate = useMemo(() => new Date().toISOString().split('T')[0], [])

    const profiteMonth = useMemo(() => {
        const date = new Date(currentDate);
        date.setMonth(date.getMonth() + 2);
        return date.toISOString().slice(0, 7);
    }, [currentDate])

    const {
        register, handleSubmit, reset, setValue, getValues, control,
        formState: {errors},
    } = useForm<PartnerFormData>({
        resolver: zodResolver(partnerCreateSchema),
        defaultValues: {
            name: '',
            phone: '',
            nationalId: '',
            address: '',
            partnershipType: '',
            totalInvestment: undefined,
            investmentStartDate: currentDate,
                profitCalculationStartMonth: profiteMonth,
            profitSharingActive: true,
            notes: '',
            createdBy: 1, // TODO: replace with auth user ID
        },
    })

    useEffect(() => {
        if (isEditMode && existing) {
            reset({
                name: existing.name,
                phone: existing.phone,
                nationalId: existing.nationalId,
                address: existing.address ?? '',
                partnershipType: existing.partnershipType,
                totalInvestment: existing.totalInvestment,
                investmentStartDate: existing.investmentStartDate,
                profitCalculationStartMonth: existing.profitCalculationStartMonth ?? profiteMonth,
                profitSharingActive: existing.profitSharingActive,
                notes: existing.notes ?? '',
                createdBy: 1,
            })
        }
    }, [isEditMode, existing, reset, profiteMonth])

    const investmentStartDateValue = useWatch({control, name: 'investmentStartDate'})
    const notesValue = useWatch({control, name: 'notes'}) ?? ''

    useEffect(() => {
        const baseDate = investmentStartDateValue || currentDate
        if (!/^\d{4}-\d{2}-\d{2}$/.test(baseDate)) return

        const  date = new Date(baseDate);
        date.setMonth(date.getMonth() + 2);

        const profiteMonth = date.toISOString().slice(0, 7)
        if (getValues('profitCalculationStartMonth') !== profiteMonth) {
            setValue('profitCalculationStartMonth', profiteMonth, {shouldValidate: true})
        }
    }, [investmentStartDateValue, currentDate, getValues, setValue])


    const onSubmit = async (data: PartnerFormData) => {
        const payload: PartnerRequest = {
            name: data.name,
            phone: data.phone,
            nationalId: data.nationalId,
            totalInvestment: data.totalInvestment,
            investmentStartDate: data.investmentStartDate,
            createdBy: data.createdBy,
            partnershipType: data.partnershipType as PartnerRequest['partnershipType'],
            profitSharingActive: data.profitSharingActive,
            address: data.address || undefined,
            notes: data.notes || undefined,
            profitCalculationStartMonth: data.profitCalculationStartMonth || undefined,
        }

        if (isEditMode) {
            const updated = await updatePartner(partnerId, payload)
            if (updated) navigate(APP_ROUTES.PARTNERS.LIST)
        } else {
            const created = await createPartner(payload)
            if (created) navigate(APP_ROUTES.PARTNERS.LIST)
        }
    }

    if (isEditMode && fetchLoading) {
        return <div className="partner-form__loading"><LoadingSpinner size="lg"/></div>
    }

    return (
        <div>
            <div className="partner-form__header">
                <h1 className="partner-form__title">
                    {isEditMode ? t('form.editTitle') : t('form.createTitle')}
                </h1>
            </div>

            <Card>
                <form onSubmit={handleSubmit(onSubmit)} noValidate>
                    <div className="partner-form__grid">
                        <Input
                            label={t('form.name') + ' *'}
                            placeholder={t('form.namePlaceholder')}
                            error={errors.name?.message}
                            {...register('name')}
                        />
                        <Input
                            label={t('form.phone') + ' *'}
                            placeholder={t('form.phonePlaceholder')}
                            error={errors.phone?.message}
                            {...register('phone')}
                        />
                        <Input
                            label={t('form.nationalId') + ' *'}
                            placeholder={t('form.nationalIdPlaceholder')}
                            error={errors.nationalId?.message}
                            {...register('nationalId')}
                        />
                        <Input
                            label={t('form.address')}
                            placeholder={t('form.addressPlaceholder')}
                            error={errors.address?.message}
                            {...register('address')}
                        />

                        {/* Partnership Type */}
                        <div className="form-field">
                            <label className="form-field__label">{t('form.partnershipType')} *</label>
                            <select
                                className={`partner-form__select ${errors.partnershipType ? 'partner-form__select--error' : ''}`}
                                {...register('partnershipType')}
                            >
                                <option value="">{t('form.partnershipTypePlaceholder')}</option>
                                {Object.values(PartnershipType).map((v) => (
                                    <option key={v} value={v}>{t(`partnershipType.${v}`)}</option>
                                ))}
                            </select>
                            {errors.partnershipType && <span className="form-field__error"
                                                             role="alert">{errors.partnershipType.message}</span>}
                        </div>

                        <Input
                            label={t('form.totalInvestment') + ' *'}
                            type="number"
                            placeholder={t('form.totalInvestment')}
                            error={errors.totalInvestment?.message}
                            {...register('totalInvestment', {valueAsNumber: true})}
                        />
                        <Input
                            label={t('form.investmentStartDate') + ' *'}
                            type="date"
                            error={errors.investmentStartDate?.message}
                            {...register('investmentStartDate')}
                        />
                        <Input
                            label={t('form.profitCalculationStartMonth')}
                            type="month"
                            error={errors.profitCalculationStartMonth?.message}
                            {...register('profitCalculationStartMonth')}
                        />

                        {/* Profit Sharing Checkbox */}
                        <div className="form-field">
                            <label className="form-field__label">{t('form.profitSharingActive')}</label>
                            <div className="partner-form__checkbox">
                                <input type="checkbox" {...register('profitSharingActive')}
                                />
                                <span>{t('form.profitSharingActive')}</span>
                            </div>
                        </div>
                    </div>

                    {/* Notes */}
                    <div className="partner-form__full" style={{marginTop: 'var(--spacing-5)'}}>
                        <div className="form-field">
                            <label htmlFor="notes" className="form-field__label">{t('form.notes')}</label>
                            <textarea
                                id="notes"
                                className={`partner-form__textarea ${errors.notes ? 'partner-form__textarea--error' : ''}`}
                                placeholder={t('form.notesPlaceholder')}
                                maxLength={500}
                                {...register('notes')}
                            />
                            <div className="partner-form__char-count">{notesValue.length} / 500</div>
                            {errors.notes &&
                                <span className="form-field__error" role="alert">{errors.notes.message}</span>}
                        </div>
                    </div>

                    <div className="partner-form__actions">
                        <Button variant="secondary" onClick={() => navigate(APP_ROUTES.PARTNERS.LIST)}
                                disabled={submitting}>
                            {tc('cancel')}
                        </Button>
                        <Button type="submit" loading={submitting}>
                            {submitting ? t('form.submitting') : t('form.submit')}
                        </Button>
                    </div>
                </form>
            </Card>
        </div>
    )
}

