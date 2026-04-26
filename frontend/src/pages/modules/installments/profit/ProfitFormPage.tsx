import { useEffect } from 'react'
import type { ReactNode } from 'react'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'

import {
    useProfitDistribution,
    useProfitDistributionCreate,
    useProfitDistributionUpdate,
} from '@hooks/modules'
import type { MonthlyProfitDistributionRequest } from '@/types/modules/profit.types'
import Button from '@components/common/Button'
import Input from '@components/common/Input'
import Card from '@components/ui/Card'
import LoadingSpinner from '@components/ui/LoadingSpinner'
import { APP_ROUTES } from '@/router/routes.config'
import './ProfitFormPage.css'

interface ProfitFormPageProps {
    distributionId?: number
}

interface ProfitFormData {
    monthYear: string
    totalProfit: number
    managementFeePercentage: number
    zakatPercentage: number
    calculationNotes: string
}

export default function ProfitFormPage({ distributionId }: ProfitFormPageProps): ReactNode {
    const { t } = useTranslation('profit')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()
    const isEditMode = distributionId != null && distributionId > 0

    const { distribution: existing, loading: fetchLoading } = useProfitDistribution(isEditMode ? distributionId : 0)
    const { createDistribution, loading: createLoading } = useProfitDistributionCreate()
    const { updateDistribution, loading: updateLoading } = useProfitDistributionUpdate()

    const {
        register,
        handleSubmit,
        reset,
        formState: { errors },
    } = useForm<ProfitFormData>({
        defaultValues: {
            monthYear: new Date().toISOString().slice(0, 7),
            totalProfit: 0,
            managementFeePercentage: 0,
            zakatPercentage: 0,
            calculationNotes: '',
        },
    })

    useEffect(() => {
        if (!isEditMode || !existing) return
        reset({
            monthYear: existing.monthYear,
            totalProfit: existing.totalProfit,
            managementFeePercentage: existing.managementFeePercentage,
            zakatPercentage: existing.zakatPercentage,
            calculationNotes: existing.calculationNotes ?? '',
        })
    }, [existing, isEditMode, reset])

    const onSubmit = async (data: ProfitFormData) => {
        const payload: MonthlyProfitDistributionRequest = {
            monthYear: data.monthYear,
            totalProfit: Number(data.totalProfit),
            managementFeePercentage: Number(data.managementFeePercentage),
            zakatPercentage: Number(data.zakatPercentage),
            calculationNotes: data.calculationNotes || undefined,
        }

        if (isEditMode) {
            const updated = await updateDistribution(distributionId, payload)
            if (updated) navigate(APP_ROUTES.PROFITS.LIST)
            return
        }

        const created = await createDistribution(payload)
        if (created) navigate(APP_ROUTES.PROFITS.LIST)
    }

    if (isEditMode && fetchLoading) {
        return <div className="profit-form__loading"><LoadingSpinner size="lg" /></div>
    }

    return (
        <div>
            <div className="profit-form__header">
                <h1 className="profit-form__title">
                    {isEditMode ? t('form.editTitle') : t('form.createTitle')}
                </h1>
            </div>

            <Card>
                <form onSubmit={handleSubmit(onSubmit)}>
                    <div className="profit-form__grid">
                        <Input
                            label={t('form.monthYear')}
                            type="month"
                            error={errors.monthYear?.message}
                            {...register('monthYear', { required: t('form.monthYearRequired') })}
                        />
                        <Input
                            label={t('form.totalProfit')}
                            type="number"
                            error={errors.totalProfit?.message}
                            {...register('totalProfit', { valueAsNumber: true, required: t('form.totalProfitRequired') })}
                        />
                        <Input
                            label={t('form.managementFeePercentage')}
                            type="number"
                            error={errors.managementFeePercentage?.message}
                            {...register('managementFeePercentage', { valueAsNumber: true, required: t('form.managementFeeRequired') })}
                        />
                        <Input
                            label={t('form.zakatPercentage')}
                            type="number"
                            error={errors.zakatPercentage?.message}
                            {...register('zakatPercentage', { valueAsNumber: true, required: t('form.zakatRequired') })}
                        />
                        <Input
                            label={t('form.calculationNotes')}
                            error={errors.calculationNotes?.message}
                            {...register('calculationNotes')}
                        />
                    </div>

                    <div className="profit-form__actions">
                        <Button type="button" variant="secondary" onClick={() => navigate(APP_ROUTES.PROFITS.LIST)}>
                            {tc('cancel')}
                        </Button>
                        <Button type="submit" loading={createLoading || updateLoading}>
                            {t('form.submit')}
                        </Button>
                    </div>
                </form>
            </Card>
        </div>
    )
}


