import { useEffect } from 'react'
import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'

import { useVendor, useVendorCreate, useVendorUpdate } from '@hooks/modules'
import { vendorCreateSchema } from '@utils/validators/vendor.validator'
import type { VendorFormData } from '@utils/validators/vendor.validator'
import Button from '@components/common/Button'
import Input from '@components/common/Input'
import Card from '@components/ui/Card'
import LoadingSpinner from '@components/ui/LoadingSpinner'
import { APP_ROUTES } from '@/router/routes.config'
import './VendorFormPage.css'

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

interface VendorFormPageProps {
    /**
     * When provided the form switches to **edit mode**:
     * it fetches the existing vendor and pre-fills every field.
     * When `undefined` the form is in **create mode**.
     */
    vendorId?: number
}

// ────────────────────────────────────────────────────────────
// Component
// ────────────────────────────────────────────────────────────

/**
 * Reusable vendor form used by both Create and Edit pages.
 *
 * - **Create mode** (`vendorId` is undefined):
 *   Empty form → `useVendorCreate` → navigate to list on success
 *
 * - **Edit mode** (`vendorId` is provided):
 *   Fetches existing data → pre-fills form → `useVendorUpdate`
 *
 * Validation is powered by Zod via `@hookform/resolvers/zod`.
 * Field-level errors appear instantly under each input.
 */
export default function VendorFormPage({ vendorId }: VendorFormPageProps): ReactNode {
    const { t } = useTranslation('vendor')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()

    const isEditMode = vendorId !== undefined

    // ── Fetch existing vendor in edit mode ─────────────────
    const {
        vendor: existingVendor,
        loading: fetchLoading,
    } = useVendor(isEditMode ? vendorId : 0)

    // ── Mutation hooks ─────────────────────────────────────
    const { createVendor, loading: createLoading } = useVendorCreate()
    const { updateVendor, loading: updateLoading } = useVendorUpdate()

    const submitting = createLoading || updateLoading

    // ── react-hook-form setup ──────────────────────────────
    const {
        register,
        handleSubmit,
        reset,
        watch,
        formState: { errors },
    } = useForm<VendorFormData>({
        resolver: zodResolver(vendorCreateSchema),
        defaultValues: {
            name: '',
            phone: '',
            address: '',
            notes: '',
        },
    })

    // ── Pre-fill form when existing vendor loads ───────────
    useEffect(() => {
        if (isEditMode && existingVendor) {
            reset({
                name: existingVendor.name,
                phone: existingVendor.phone,
                address: existingVendor.address,
                notes: existingVendor.notes ?? '',
            })
        }
    }, [isEditMode, existingVendor, reset])

    // ── Watch notes for character count ────────────────────
    const notesValue = watch('notes') ?? ''

    // ── Submit handler ─────────────────────────────────────
    const onSubmit = async (data: VendorFormData) => {
        const payload = {
            name: data.name,
            phone: data.phone,
            address: data.address,
            notes: data.notes || undefined,
        }

        if (isEditMode) {
            const updated = await updateVendor(vendorId, payload)
            if (updated) navigate(APP_ROUTES.PROCUREMENT.LIST)
        } else {
            const created = await createVendor(payload)
            if (created) navigate(APP_ROUTES.PROCUREMENT.LIST)
        }
    }

    // ── Loading state (edit mode only) ─────────────────────
    if (isEditMode && fetchLoading) {
        return (
            <div className="vendor-form__loading">
                <LoadingSpinner size="lg" />
            </div>
        )
    }

    // ── Render ─────────────────────────────────────────────
    return (
        <div>
            {/* Header */}
            <div className="vendor-form__header">
                <h1 className="vendor-form__title">
                    {isEditMode ? t('form.editTitle') : t('form.createTitle')}
                </h1>
            </div>

            <Card>
                <form onSubmit={handleSubmit(onSubmit)} noValidate>
                    <div className="vendor-form__grid">

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

                        {/* ── Address (full width) ─────── */}
                        <div className="vendor-form__full">
                            <Input
                                label={t('form.address')}
                                placeholder={t('form.addressPlaceholder')}
                                required
                                error={errors.address?.message}
                                {...register('address')}
                            />
                        </div>

                        {/* ── Notes (full width) ────────── */}
                        <div className="form-field vendor-form__full">
                            <label htmlFor="notes" className="form-field__label">
                                {t('form.notes')}
                            </label>
                            <textarea
                                id="notes"
                                className={[
                                    'vendor-form__textarea',
                                    errors.notes && 'vendor-form__textarea--error',
                                ].filter(Boolean).join(' ')}
                                placeholder={t('form.notesPlaceholder')}
                                maxLength={500}
                                {...register('notes')}
                            />
                            <div className="vendor-form__char-count">
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
                    <div className="vendor-form__actions">
                        <Button
                            variant="secondary"
                            onClick={() => navigate(APP_ROUTES.PROCUREMENT.LIST)}
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

