import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMemo, useCallback, useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { v4 as uuidv4 } from 'uuid'

import { usePaymentCreate } from '@hooks/modules'
import { paymentCreateSchema } from '@utils/validators/payment.validator'
import type { PaymentFormData } from '@utils/validators/payment.validator'
import { PaymentMethod } from '@/types/modules/payment.types'
import type { PaymentRequest } from '@/types/modules/payment.types'
import Button from '@components/common/Button'
import Input from '@components/common/Input'
import Modal from '@components/common/Modal/Modal'
import Card from '@components/ui/Card'
import { APP_ROUTES } from '@/router/routes.config'
import { formatCurrency } from '@utils/helpers/format.helper'
import './PaymentFormPage.css'

export default function PaymentFormPage(): ReactNode {
    const { t } = useTranslation('payment')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()
    const [searchParams] = useSearchParams()

    const prefill = useMemo(() => {
        const parsePositive = (value: string | null): number | undefined => {
            if (!value) return undefined
            const parsed = Number(value)
            return Number.isFinite(parsed) && parsed > 0 ? parsed : undefined
        }

        return {
            scheduleId: parsePositive(searchParams.get('scheduleId')),
            amount: parsePositive(searchParams.get('amount')),
            contractId: parsePositive(searchParams.get('contractId')),
            customerName: searchParams.get('customerName') || '',
            dueDate: searchParams.get('dueDate') || '',
        }
    }, [searchParams])

    const isSchedulePayment = Boolean(prefill.scheduleId)
    const [isSubmitConfirmOpen, setIsSubmitConfirmOpen] = useState(false)
    const [pendingSubmitData, setPendingSubmitData] = useState<PaymentFormData | null>(null)

    const { createPayment, loading: submitting } = usePaymentCreate()

    const currentDate = useMemo(() => new Date().toISOString().split('T')[0], [])
    const currentMonth = useMemo(() => currentDate.slice(0, 7), [currentDate])

    const defaultValues = useMemo(() => ({
        idempotencyKey: `PAY-${uuidv4().slice(0, 8).toUpperCase()}`,
        installmentScheduleId: prefill.scheduleId,
        amount: prefill.amount,
        paymentMethod: '',
        actualPaymentDate: currentDate,
        agreedPaymentMonth: currentMonth,
        extraExpenses: 0,
        isEarlyPayment: false,
        discountAmount: 0,
        notes: '',
    }), [prefill.scheduleId, prefill.amount, currentDate, currentMonth])



    const {
        register, handleSubmit, watch, setValue, getValues,
        formState: { errors },
    } = useForm<PaymentFormData>({
        resolver: zodResolver(paymentCreateSchema),
        defaultValues,
    })

    const actualPaymentDateValue = watch('actualPaymentDate')
    const notesValue = watch('notes') ?? ''

    useEffect(() => {
        const sourceDate = actualPaymentDateValue || currentDate
        if (!/^\d{4}-\d{2}-\d{2}$/.test(sourceDate)) return

        const derivedMonth = sourceDate.slice(0, 7)
        if (getValues('agreedPaymentMonth') !== derivedMonth) {
            setValue('agreedPaymentMonth', derivedMonth, { shouldValidate: true })
        }
    }, [actualPaymentDateValue, currentDate, getValues, setValue])

    const toOptionalNumber = (value: number | undefined): number | undefined => {
        return typeof value === 'number' && Number.isFinite(value) ? value : undefined
    }

    const toAgreedMonth = (dateValue: string | undefined): string => {
        const sourceDate = dateValue && /^\d{4}-\d{2}-\d{2}$/.test(dateValue) ? dateValue : currentDate
        return sourceDate.slice(0, 7)
    }

    const submitPayment = useCallback(async (data: PaymentFormData) => {
        const agreedMonth = toAgreedMonth(data.actualPaymentDate)
        const payload: PaymentRequest = {
            ...data,
            paymentMethod: data.paymentMethod as PaymentRequest['paymentMethod'],
            installmentScheduleId: prefill.scheduleId ?? data.installmentScheduleId,
            actualPaymentDate: data.actualPaymentDate || currentDate,
            agreedPaymentMonth: agreedMonth,
            extraExpenses: toOptionalNumber(data.extraExpenses),
            discountAmount: toOptionalNumber(data.discountAmount),
            notes: data.notes || undefined,
        }
        const created = await createPayment(payload)
        if (created) navigate(APP_ROUTES.PAYMENTS.LIST)
        return created
    }, [createPayment, currentDate, navigate, prefill.scheduleId, toAgreedMonth, toOptionalNumber])

    const onSubmit = useCallback((data: PaymentFormData) => {
        setPendingSubmitData(data)
        setIsSubmitConfirmOpen(true)
    }, [])

    const closeSubmitConfirm = useCallback(() => {
        if (submitting) return
        setIsSubmitConfirmOpen(false)
        setPendingSubmitData(null)
    }, [submitting])

    const confirmSubmit = useCallback(async () => {
        if (!pendingSubmitData) return
        const created = await submitPayment(pendingSubmitData)
        if (created) {
            setIsSubmitConfirmOpen(false)
            setPendingSubmitData(null)
        }
    }, [pendingSubmitData, submitPayment])

    return (
        <div>
            <div className="payment-form__header">
                <h1 className="payment-form__title">{t('form.createTitle')}</h1>
            </div>

            <Card>
                <form onSubmit={handleSubmit(onSubmit)} noValidate>
                    {isSchedulePayment && (
                        <div className="payment-form__notice" role="status">
                            {t('form.prefilledFromSchedule', {
                                scheduleId: prefill.scheduleId,
                                contractId: prefill.contractId || '-',
                                customerName: prefill.customerName || '-',
                                dueDate: prefill.dueDate || '-',
                            })}
                        </div>
                    )}

                    <div className="payment-form__grid">
                        {isSchedulePayment && (
                            <Input
                                label={t('form.installmentScheduleId')}
                                name="installmentScheduleIdReadonly"
                                value={String(prefill.scheduleId)}
                                disabled
                                readOnly
                            />
                        )}

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
                            type="month"
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

            <Modal
                isOpen={isSubmitConfirmOpen}
                onClose={closeSubmitConfirm}
                title={t('form.submitConfirmTitle')}
                footer={(
                    <>
                        <Button variant="secondary" onClick={closeSubmitConfirm} disabled={submitting}>
                            {tc('cancel')}
                        </Button>
                        <Button onClick={confirmSubmit} loading={submitting}>
                            {tc('confirm')}
                        </Button>
                    </>
                )}
            >
                <p>{t('form.submitConfirm', { amount: formatCurrency(pendingSubmitData?.amount || 0) })}</p>
            </Modal>
        </div>
    )
}

