import { useEffect } from 'react'
import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'

import { useCustomer, useCustomerCreate, useCustomerUpdate } from '@hooks/modules'
import { customerCreateSchema } from '@utils/validators/customer.validator'
import type { CustomerFormData } from '@utils/validators/customer.validator'
import Button from '@components/common/Button'
import Input from '@components/common/Input'
import Card from '@components/ui/Card'
import LoadingSpinner from '@components/ui/LoadingSpinner'
import { APP_ROUTES } from '@/router/routes.config'
import './CustomerFormPage.css'

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

interface CustomerFormPageProps {
    /**
     * When provided the form switches to **edit mode**:
     * it fetches the existing customer and pre-fills every field.
     * When `undefined` the form is in **create mode**.
     */
    customerId?: number
}

// ────────────────────────────────────────────────────────────
// Component
// ────────────────────────────────────────────────────────────

export default function CustomerFormPage({ customerId }: CustomerFormPageProps): ReactNode {
    const { t } = useTranslation('customer')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()

    const isEditMode = customerId !== undefined

    // ── Fetch existing customer in edit mode ───────────────
    const {
        customer: existingCustomer,
        loading: fetchLoading,
    } = useCustomer(isEditMode ? customerId : 0)

    // ── Mutation hooks ─────────────────────────────────────
    const { createCustomer, loading: createLoading } = useCustomerCreate()
    const { updateCustomer, loading: updateLoading } = useCustomerUpdate()

    const submitting = createLoading || updateLoading

    // ── react-hook-form setup ──────────────────────────────
    const {
        register,
        handleSubmit,
        reset,
        watch,
        formState: { errors },
    } = useForm<CustomerFormData>({
        resolver: zodResolver(customerCreateSchema),
        defaultValues: {
            name: '',
            phone: '',
            address: '',
            nationalId: '',
            notes: '',
        },
    })

    // ── Pre-fill form when existing customer loads ─────────
    useEffect(() => {
        if (isEditMode && existingCustomer) {
            reset({
                name: existingCustomer.name,
                phone: existingCustomer.phone,
                address: existingCustomer.address,
                nationalId: existingCustomer.nationalId,
                notes: existingCustomer.notes ?? '',
            })
        }
    }, [isEditMode, existingCustomer, reset])

    // ── Watch notes for character count ────────────────────
    const notesValue = watch('notes') ?? ''

    // ── Submit handler ─────────────────────────────────────
    const onSubmit = async (data: CustomerFormData) => {
        const payload = {
            name: data.name,
            phone: data.phone,
            address: data.address,
            nationalId: data.nationalId,
            notes: data.notes || undefined,
        }

        if (isEditMode) {
            const updated = await updateCustomer(customerId, payload)
            if (updated) navigate(APP_ROUTES.CUSTOMERS.LIST)
        } else {
            const created = await createCustomer(payload)
            if (created) navigate(APP_ROUTES.CUSTOMERS.LIST)
        }
    }

    // ── Loading state (edit mode only) ─────────────────────
    if (isEditMode && fetchLoading) {
        return (
            <div className="customer-form__loading">
                <LoadingSpinner size="lg" />
            </div>
        )
    }

    // ── Render ─────────────────────────────────────────────
    return (
        <div>
            {/* Header */}
            <div className="customer-form__header">
                <h1 className="customer-form__title">
                    {isEditMode ? t('form.editTitle') : t('form.createTitle')}
                </h1>
            </div>

            <Card>
                <form onSubmit={handleSubmit(onSubmit)} noValidate>
                    <div className="customer-form__grid">

                        {/* ── Name ─────────────────────── */}
                        <Input
                            label={t('form.name')}
                            placeholder={t('form.namePlaceholder')}
                            required
                            error={errors.name?.message}
                            {...register('name')}
                        />

                        {/* ── Phone ────────────────────── */}
                        <Input
                            label={t('form.phone')}
                            type="tel"
                            placeholder={t('form.phonePlaceholder')}
                            required
                            error={errors.phone?.message}
                            {...register('phone')}
                        />

                        {/* ── National ID ──────────────── */}
                        <Input
                            label={t('form.nationalId')}
                            placeholder={t('form.nationalIdPlaceholder')}
                            required
                            error={errors.nationalId?.message}
                            {...register('nationalId')}
                        />

                        {/* ── Address ──────────────────── */}
                        <Input
                            label={t('form.address')}
                            placeholder={t('form.addressPlaceholder')}
                            required
                            error={errors.address?.message}
                            {...register('address')}
                        />

                        {/* ── Notes (full width) ────────── */}
                        <div className="form-field customer-form__full">
                            <label htmlFor="notes" className="form-field__label">
                                {t('form.notes')}
                            </label>
                            <textarea
                                id="notes"
                                className={[
                                    'customer-form__textarea',
                                    errors.notes && 'customer-form__textarea--error',
                                ].filter(Boolean).join(' ')}
                                placeholder={t('form.notesPlaceholder')}
                                maxLength={500}
                                {...register('notes')}
                            />
                            <div className="customer-form__char-count">
                                {notesValue.length} / 500
                            </div>
                            {errors.notes && (
                                <span className="form-field__error" role="alert">
                                    {errors.notes.message}
                                </span>
                            )}
                        </div>
                    </div>

                    {/* ── Actions ───────────────────── */}
                    <div className="customer-form__actions">
                        <Button
                            variant="secondary"
                            onClick={() => navigate(APP_ROUTES.CUSTOMERS.LIST)}
                            disabled={submitting}
                        >
                            {tc('cancel')}
                        </Button>
                        <Button
                            type="submit"
                            loading={submitting}
                        >
                            {submitting ? t('form.submitting') : t('form.submit')}
                        </Button>
                    </div>
                </form>
            </Card>
        </div>
    )
}

