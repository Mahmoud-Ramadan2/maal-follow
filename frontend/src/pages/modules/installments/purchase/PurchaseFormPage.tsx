import {useEffect, useMemo} from 'react'
import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'

import { usePurchase, usePurchaseCreate, usePurchaseUpdate, useVendors } from '@hooks/modules'
import { purchaseCreateSchema } from '@utils/validators/purchase.validator'
import type { PurchaseFormData } from '@utils/validators/purchase.validator'
import type {Vendor, VendorFilters} from '@/types/modules/vendor.types'
import Button from '@components/common/Button'
import Input from '@components/common/Input'
import Card from '@components/ui/Card'
import LoadingSpinner from '@components/ui/LoadingSpinner'
import { APP_ROUTES } from '@/router/routes.config'
import './PurchaseFormPage.css'


// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

interface PurchaseFormPageProps {
    /**
     * When provided the form switches to **edit mode**:
     * it fetches the existing purchase and pre-fills every field.
     * When `undefined` the form is in **create mode**.
     */
    purchaseId?: number
}

// ────────────────────────────────────────────────────────────
// Component
// ────────────────────────────────────────────────────────────

/**
 * Reusable purchase form used by both Create and Edit pages.
 *
 * - **Create mode** (`purchaseId` is undefined):
 *   Empty form → `usePurchaseCreate` → navigate to list on success
 *
 * - **Edit mode** (`purchaseId` is provided):
 *   Fetches existing data → pre-fills form → `usePurchaseUpdate`
 *
 * Validation is powered by Zod via `@hookform/resolvers/zod`.
 * Field-level errors appear instantly under each input.
 */
export default function PurchaseFormPage({ purchaseId }: PurchaseFormPageProps): ReactNode {
    const { t } = useTranslation('purchase')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()

    const isEditMode = purchaseId !== undefined

    // ── Fetch existing purchase in edit mode ───────────────
    const {
        purchase: existingPurchase,
        loading: fetchLoading,
    } = usePurchase(isEditMode ? purchaseId : 0)

    // ── Fetch vendors for the dropdown ─────────────────────
    // Memoize filters to prevent infinite re-renders
    const vendorFilters: VendorFilters = useMemo<VendorFilters>(() => ({status: 'active'}), [])
    // const vendorFilters = useMemo(() => ({ size: 100 }), [])
    const { vendors, loading: vendorsLoading } = useVendors(vendorFilters)
    // const { vendors, loading: vendorsLoading } = useVendors({ size: 100 })

    // ── Mutation hooks ─────────────────────────────────────
    // ──inpo Mutation hooks ─────────────────────────────────────
    const { createPurchase, loading: createLoading } = usePurchaseCreate()
    const { updatePurchase, loading: updateLoading } = usePurchaseUpdate()

    const submitting = createLoading || updateLoading

    // ── react-hook-form setup ──────────────────────────────
    const {
        register,
        handleSubmit,
        reset,
        watch,
        formState: { errors },
    } = useForm<PurchaseFormData>({
        resolver: zodResolver(purchaseCreateSchema),
        defaultValues: {
            vendorId: 0,
            productName: '',
            buyPrice: 0,
            purchaseDate: '',
            notes: '',
        },
    })

    // ── Pre-fill form when existing purchase loads ─────────
    useEffect(() => {
        if (isEditMode && existingPurchase) {
            // Find vendorId from real vendors by name
            const vendor = vendors.find(
                (v: Vendor) => v.name === existingPurchase.vendorName,
            )
            reset({
                vendorId: vendor?.id ?? 0,
                productName: existingPurchase.productName,
                buyPrice: existingPurchase.buyPrice,
                purchaseDate: existingPurchase.purchaseDate,
                notes: existingPurchase.notes ?? '',
            })
        }
    }, [isEditMode, existingPurchase, reset])

    // ── Watch notes for character count ────────────────────
    const notesValue = watch('notes') ?? ''

    // ── Submit handler ─────────────────────────────────────
    const onSubmit = async (data: PurchaseFormData) => {
        const payload = {
            vendorId: data.vendorId,
            productName: data.productName,
            buyPrice: data.buyPrice,
            purchaseDate: data.purchaseDate,
            notes: data.notes || undefined,
        }

        try {
            if (isEditMode) {
                const updated = await updatePurchase(purchaseId, payload)

                if (updated) {
                    navigate(APP_ROUTES.PROCUREMENT.LIST)
                }
            } else {
                const created = await createPurchase(payload)
                if (created) {
                    navigate(APP_ROUTES.PROCUREMENT.LIST)
                }
            }
        } catch (error) {
            console.error('Submit error:', error)
            // Error is already handled by the hook (toast)
        }
    }

    // ── Loading state (edit mode only) ─────────────────────
    if (isEditMode && fetchLoading) {
        return (
            <div className="purchase-form__loading">
                <LoadingSpinner size="lg" />
            </div>
        )
    }

    // ── Render ─────────────────────────────────────────────
    return (
        <div>
            {/* Header */}
            <div className="purchase-form__header">
                <h1 className="purchase-form__title">
                    {isEditMode ? t('form.editTitle') : t('form.createTitle')}
                </h1>
            </div>

            <Card>
                <form onSubmit={handleSubmit(onSubmit)} noValidate>
                    <div className="purchase-form__grid">

                        {/* ── Vendor select ───────────── */}
                        <div className="form-field">
                            <label htmlFor="vendorId" className="form-field__label">
                                {t('form.vendorId')}
                                <span className="form-field__required">*</span>
                            </label>
                            <select
                                id="vendorId"
                                className={[
                                    'purchase-form__select',
                                    errors.vendorId && 'purchase-form__select--error',
                                ].filter(Boolean).join(' ')}
                                {...register('vendorId', { valueAsNumber: true })}
                            >
                                <option value={0} disabled>
                                    {vendorsLoading ? '...' : t('form.vendorPlaceholder')}
                                </option>
                                {vendors.map((v) => (
                                    <option key={v.id} value={v.id}>{v.name}</option>
                                ))}
                            </select>
                            {errors.vendorId && (
                                <span className="form-field__error" role="alert">
                                    {errors.vendorId.message}
                                </span>
                            )}
                        </div>

                        {/* ── Product name ─────────────── */}
                        <Input
                            label={t('form.productName')}
                            placeholder={t('form.productNamePlaceholder')}
                            required
                            error={errors.productName?.message}
                            {...register('productName')}
                        />

                        {/* ── Buy price ────────────────── */}
                        <Input
                            label={t('form.buyPrice')}
                            type="number"
                            step="0.01"
                            min="1"
                            placeholder={t('form.buyPricePlaceholder')}
                            required
                            error={errors.buyPrice?.message}
                            {...register('buyPrice', { valueAsNumber: true })}
                        />

                        {/* ── Purchase date ────────────── */}
                        <Input
                            label={t('form.purchaseDate')}
                            type="date"
                            max={new Date().toISOString().split('T')[0]}
                            required
                            error={errors.purchaseDate?.message}
                            {...register('purchaseDate')}
                        />

                        {/* ── Notes (full width) ────────── */}
                        <div className="form-field purchase-form__full">
                            <label htmlFor="notes" className="form-field__label">
                                {t('form.notes')}
                            </label>
                            <textarea
                                id="notes"
                                className={[
                                    'purchase-form__textarea',
                                    errors.notes && 'purchase-form__textarea--error',
                                ].filter(Boolean).join(' ')}
                                placeholder={t('form.notesPlaceholder')}
                                maxLength={500}
                                {...register('notes')}
                            />
                            <div className="purchase-form__char-count">
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
                    <div className="purchase-form__actions">
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

