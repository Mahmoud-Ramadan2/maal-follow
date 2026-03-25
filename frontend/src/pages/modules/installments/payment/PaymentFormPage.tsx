import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { v4 as uuidv4 } from 'uuid'

import { usePaymentCreate } from '@hooks/modules'
import { paymentCreateSchema } from '@utils/validators/payment.validator'
import type { PaymentFormData } from '@utils/validators/payment.validator'
import { PaymentMethod } from '@/types/modules/payment.types'
import Button from '@components/common/Button'
import Input from '@components/common/Input'
import Card from '@components/ui/Card'
import { APP_ROUTES } from '@/router/routes.config'
import './PaymentFormPage.css'

export default function PaymentFormPage(): ReactNode {
    const { t } = useTranslation('payment')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()

    const { createPayment, loading: submitting } = usePaymentCreate()

    const {
        register, handleSubmit, watch,
        formState: { errors },
    } = useForm<PaymentFormData>({
        resolver: zodResolver(paymentCreateSchema),
        defaultValues: {
            idempotencyKey: `PAY-${uuidv4().slice(0, 8).toUpperCase()}`,
            amount: undefined,
            paymentMethod: '',
            actualPaymentDate: new Date().toISOString().split('T')[0],
            agreedPaymentMonth: '',
            extraExpenses: undefined,
            isEarlyPayment: false,
            discountAmount: undefined,
            notes: '',
        },
    })

    const notesValue = watch('notes') ?? ''

    const onSubmit = async (data: PaymentFormData) => {
        const payload = {
            ...data,
            agreedPaymentMonth: data.agreedPaymentMonth || undefined,
            notes: data.notes || undefined,
        }
        const created = await createPayment(payload)
        if (created) navigate(APP_ROUTES.PAYMENTS.LIST)
    }

    return (
        <div>
            <div className="payment-form__header">
                <h1 className="payment-form__title">{t('form.createTitle')}</h1>
            </div>

            <Card>
                <form onSubmit={handleSubmit(onSubmit)} noValidate>
                    <div className="payment-form__grid">
                        <Input
                            label={t('form.idempotencyKey') + ' *'}
                            placeholder={t('form.idempotencyKeyPlaceholder')}
                            error={errors.idempotencyKey?.message}
                            {...register('idempotencyKey')}
                        />
                        <Input
                            label={t('form.amount') + ' *'}
                            type="number"
                            placeholder={t('form.amountPlaceholder')}
                            error={errors.amount?.message}
                            {...register('amount', { valueAsNumber: true })}
                        />

                        {/* Payment Method */}
                        <div className="form-field">
                            <label className="form-field__label">{t('form.paymentMethod')} *</label>
                            <select
                                className={`payment-form__select ${errors.paymentMethod ? 'payment-form__select--error' : ''}`}
                                {...register('paymentMethod')}
                            >
                                <option value="">{t('form.paymentMethodPlaceholder')}</option>
                                {Object.values(PaymentMethod).map((v) => (
                                    <option key={v} value={v}>{t(`paymentMethod.${v}`)}</option>
                                ))}
                            </select>
                            {errors.paymentMethod && <span className="form-field__error" role="alert">{errors.paymentMethod.message}</span>}
                        </div>

                        <Input
                            label={t('form.actualPaymentDate') + ' *'}
                            type="date"
                            error={errors.actualPaymentDate?.message}
                            {...register('actualPaymentDate')}
                        />
                        <Input
                            label={t('form.agreedPaymentMonth')}
                            placeholder={t('form.agreedPaymentMonthPlaceholder')}
                            error={errors.agreedPaymentMonth?.message}
                            {...register('agreedPaymentMonth')}
                        />
                        <Input
                            label={t('form.extraExpenses')}
                            type="number"
                            placeholder={t('form.extraExpensesPlaceholder')}
                            error={errors.extraExpenses?.message}
                            {...register('extraExpenses', { valueAsNumber: true })}
                        />
                        <Input
                            label={t('form.discountAmount')}
                            type="number"
                            placeholder={t('form.discountAmountPlaceholder')}
                            error={errors.discountAmount?.message}
                            {...register('discountAmount', { valueAsNumber: true })}
                        />

                        {/* Early Payment checkbox */}
                        <div className="payment-form__checkbox">
                            <input type="checkbox" id="isEarlyPayment" {...register('isEarlyPayment')} />
                            <label htmlFor="isEarlyPayment">{t('form.isEarlyPayment')}</label>
                        </div>
                    </div>

                    {/* Notes */}
                    <div className="payment-form__full" style={{ marginTop: 'var(--spacing-5)' }}>
                        <div className="form-field">
                            <label htmlFor="notes" className="form-field__label">{t('form.notes')}</label>
                            <textarea
                                id="notes"
                                className={`payment-form__textarea ${errors.notes ? 'payment-form__textarea--error' : ''}`}
                                placeholder={t('form.notesPlaceholder')}
                                maxLength={500}
                                {...register('notes')}
                            />
                            <div className="payment-form__char-count">{notesValue.length} / 500</div>
                            {errors.notes && <span className="form-field__error" role="alert">{errors.notes.message}</span>}
                        </div>
                    </div>

                    <div className="payment-form__actions">
                        <Button variant="secondary" onClick={() => navigate(APP_ROUTES.PAYMENTS.LIST)} disabled={submitting}>
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

