import { useEffect } from 'react'
import type { ReactNode } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'

import { useCapitalPool, useCapitalPoolActions, useCapitalTransactionCreate } from '@hooks/modules'
import { capitalPoolSchema, capitalTransactionSchema } from '@utils/validators/capital.validator'
import type { CapitalPoolFormData, CapitalTransactionFormData } from '@utils/validators/capital.validator'
import { CapitalTransactionType } from '@/types/modules/capital.types'
import Button from '@components/common/Button'
import Input from '@components/common/Input'
import Card from '@components/ui/Card'
import LoadingSpinner from '@components/ui/LoadingSpinner'
import { APP_ROUTES } from '@/router/routes.config'
import './CapitalFormPage.css'

export default function CapitalFormPage(): ReactNode {
    const { t } = useTranslation('capital')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()
    const [searchParams] = useSearchParams()
    const isTransactionTab = searchParams.get('tab') === 'transaction'

    if (isTransactionTab) {
        return <TransactionForm />
    }
    return <PoolForm />
}

// ── Pool Form ───────────────────────────────────────────────
function PoolForm(): ReactNode {
    const { t } = useTranslation('capital')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()

    const { pool, loading: fetchLoading } = useCapitalPool()
    const { createPool, updatePool, loading: submitting } = useCapitalPoolActions()
    const isEditMode = !!pool

    const {
        register, handleSubmit, reset, watch,
        formState: { errors },
    } = useForm<CapitalPoolFormData>({
        resolver: zodResolver(capitalPoolSchema),
        defaultValues: { totalAmount: 0, ownerContribution: 0, partnerContributions: 0, description: '' },
    })

    useEffect(() => {
        if (pool) {
            reset({
                totalAmount: pool.totalAmount,
                ownerContribution: pool.ownerContribution,
                partnerContributions: pool.partnerContributions,
                description: pool.description ?? '',
            })
        }
    }, [pool, reset])

    const descValue = watch('description') ?? ''

    const onSubmit = async (data: CapitalPoolFormData) => {
        const payload = { ...data, description: data.description || undefined }
        const result = isEditMode ? await updatePool(payload) : await createPool(payload)
        if (result) navigate(APP_ROUTES.CAPITAL.LIST)
    }

    if (fetchLoading) {
        return <div className="capital-form__loading"><LoadingSpinner size="lg" /></div>
    }

    return (
        <div>
            <div className="capital-form__header">
                <h1 className="capital-form__title">
                    {isEditMode ? t('poolForm.editTitle') : t('poolForm.createTitle')}
                </h1>
            </div>
            <Card>
                <form onSubmit={handleSubmit(onSubmit)} noValidate>
                    <div className="capital-form__grid">
                        <Input
                            label={t('poolForm.totalAmount') + ' *'}
                            type="number"
                            placeholder={t('poolForm.totalAmountPlaceholder')}
                            error={errors.totalAmount?.message}
                            {...register('totalAmount', { valueAsNumber: true })}
                        />
                        <Input
                            label={t('poolForm.ownerContribution') + ' *'}
                            type="number"
                            placeholder={t('poolForm.ownerContributionPlaceholder')}
                            error={errors.ownerContribution?.message}
                            {...register('ownerContribution', { valueAsNumber: true })}
                        />
                        <Input
                            label={t('poolForm.partnerContributions') + ' *'}
                            type="number"
                            placeholder={t('poolForm.partnerContributionsPlaceholder')}
                            error={errors.partnerContributions?.message}
                            {...register('partnerContributions', { valueAsNumber: true })}
                        />
                    </div>
                    <div className="capital-form__full" style={{ marginTop: 'var(--spacing-5)' }}>
                        <div className="form-field">
                            <label htmlFor="description" className="form-field__label">{t('poolForm.description')}</label>
                            <textarea
                                id="description"
                                className={`capital-form__textarea ${errors.description ? 'capital-form__textarea--error' : ''}`}
                                placeholder={t('poolForm.descriptionPlaceholder')}
                                maxLength={500}
                                {...register('description')}
                            />
                            <div className="capital-form__char-count">{descValue.length} / 500</div>
                            {errors.description && <span className="form-field__error" role="alert">{errors.description.message}</span>}
                        </div>
                    </div>
                    <div className="capital-form__actions">
                        <Button variant="secondary" onClick={() => navigate(APP_ROUTES.CAPITAL.LIST)} disabled={submitting}>
                            {tc('cancel')}
                        </Button>
                        <Button type="submit" loading={submitting}>
                            {submitting ? t('poolForm.submitting') : t('poolForm.submit')}
                        </Button>
                    </div>
                </form>
            </Card>
        </div>
    )
}

// ── Transaction Form ────────────────────────────────────────
function TransactionForm(): ReactNode {
    const { t } = useTranslation('capital')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()

    const { createTransaction, loading: submitting } = useCapitalTransactionCreate()

    const {
        register, handleSubmit, watch,
        formState: { errors },
    } = useForm<CapitalTransactionFormData>({
        resolver: zodResolver(capitalTransactionSchema),
        defaultValues: { transactionType: '', amount: undefined, description: '' },
    })

    const descValue = watch('description') ?? ''

    const onSubmit = async (data: CapitalTransactionFormData) => {
        const payload = { ...data, description: data.description || undefined }
        const result = await createTransaction(payload)
        if (result) navigate(APP_ROUTES.CAPITAL.LIST)
    }

    return (
        <div>
            <div className="capital-form__header">
                <h1 className="capital-form__title">{t('transactionForm.createTitle')}</h1>
            </div>
            <Card>
                <form onSubmit={handleSubmit(onSubmit)} noValidate>
                    <div className="capital-form__grid">
                        <div className="form-field">
                            <label className="form-field__label">{t('transactionForm.transactionType')} *</label>
                            <select
                                className={`capital-form__select ${errors.transactionType ? 'capital-form__select--error' : ''}`}
                                {...register('transactionType')}
                            >
                                <option value="">{t('transactionForm.transactionTypePlaceholder')}</option>
                                {Object.values(CapitalTransactionType).map((v) => (
                                    <option key={v} value={v}>{t(`transactionType.${v}`)}</option>
                                ))}
                            </select>
                            {errors.transactionType && <span className="form-field__error" role="alert">{errors.transactionType.message}</span>}
                        </div>
                        <Input
                            label={t('transactionForm.amount') + ' *'}
                            type="number"
                            placeholder={t('transactionForm.amountPlaceholder')}
                            error={errors.amount?.message}
                            {...register('amount', { valueAsNumber: true })}
                        />
                        <Input
                            label={t('transactionForm.contractId')}
                            type="number"
                            placeholder={t('transactionForm.contractPlaceholder')}
                            error={errors.contractId?.message}
                            {...register('contractId', { valueAsNumber: true })}
                        />
                    </div>
                    <div className="capital-form__full" style={{ marginTop: 'var(--spacing-5)' }}>
                        <div className="form-field">
                            <label htmlFor="tx-desc" className="form-field__label">{t('transactionForm.description')}</label>
                            <textarea
                                id="tx-desc"
                                className={`capital-form__textarea ${errors.description ? 'capital-form__textarea--error' : ''}`}
                                placeholder={t('transactionForm.descriptionPlaceholder')}
                                maxLength={500}
                                {...register('description')}
                            />
                            <div className="capital-form__char-count">{descValue.length} / 500</div>
                            {errors.description && <span className="form-field__error" role="alert">{errors.description.message}</span>}
                        </div>
                    </div>
                    <div className="capital-form__actions">
                        <Button variant="secondary" onClick={() => navigate(APP_ROUTES.CAPITAL.LIST)} disabled={submitting}>
                            {tc('cancel')}
                        </Button>
                        <Button type="submit" loading={submitting}>
                            {submitting ? t('transactionForm.submitting') : t('transactionForm.submit')}
                        </Button>
                    </div>
                </form>
            </Card>
        </div>
    )
}

