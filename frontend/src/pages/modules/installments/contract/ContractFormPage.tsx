import { useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'

import { useContract, useContractCreate, useContractUpdate } from '@hooks/modules'
import { useCustomers } from '@hooks/modules'
import { usePurchases } from '@hooks/modules'
import { useDebounce } from '@hooks/common'
import { contractCreateSchema } from '@utils/validators/contract.validator'
import type { ContractFormData } from '@utils/validators/contract.validator'
import { ContractStatus } from '@/types/modules/contract.types'
import type { CustomerFilters } from '@/types/modules/customer.types'
import type { PurchaseFilters } from '@/types/modules/purchase.types'
import Button from '@components/common/Button'
import Input from '@components/common/Input'
import Card from '@components/ui/Card'
import LoadingSpinner from '@components/ui/LoadingSpinner'
import { APP_ROUTES } from '@/router/routes.config'
import './ContractFormPage.css'

interface ContractFormPageProps {
    contractId?: number
}

export default function ContractFormPage({ contractId }: ContractFormPageProps): ReactNode {
    const { t } = useTranslation('contract')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()
    const isEditMode = contractId !== undefined

    const [customerSearch, setCustomerSearch] = useState('')
    const [purchaseSearch, setPurchaseSearch] = useState('')
    const debouncedCustomerSearch = useDebounce(customerSearch, 350)
    const debouncedPurchaseSearch = useDebounce(purchaseSearch, 350)

    // Fetch existing contract in edit mode
    const { contract: existing,
        loading: fetchLoading } =
        useContract(isEditMode ? contractId : 0)

    // Fetch customers and purchases for typeahead selectors
    const customerFilters = useMemo<CustomerFilters>(() => ({
        page: 0,
        size: 10,
        status: 'active',
        ...(debouncedCustomerSearch.trim() && { search: debouncedCustomerSearch.trim() }),
    }), [debouncedCustomerSearch])

    const purchaseFilters = useMemo<PurchaseFilters>(() => ({
        page: 0,
        size: 10,
        sort: 'purchaseDate,desc',
        ...(debouncedPurchaseSearch.trim() && { searchTerm: debouncedPurchaseSearch.trim() }),
    }), [debouncedPurchaseSearch])

    const { customers, loading: customersLoading } = useCustomers(customerFilters)
    const { purchases, loading: purchasesLoading } = usePurchases(purchaseFilters)

    // Mutation hooks
    const { createContract, loading: createLoading } = useContractCreate()
    const { updateContract, loading: updateLoading } = useContractUpdate()
    const submitting = createLoading || updateLoading

    const {
        register, handleSubmit, reset, watch, setValue,
        formState: { errors },
    } = useForm<ContractFormData>({
        resolver: zodResolver(contractCreateSchema),
        defaultValues: {
            customerId: 0,
            purchaseId: 0,
            finalPrice: 0,
            downPayment: 0,
            months: 12,
            monthlyAmount: undefined,
            startDate: new Date().toISOString().split('T')[0] ?? '',
            status: 'ACTIVE',
            additionalCosts: 0,
            earlyPaymentDiscountRate: 0,
            agreedPaymentDay: 1,
            contractNumber: '',
            notes: '',
            partnerId: undefined,
            responsibleUserId: undefined,
        },
    })

    // Pre-fill form in edit mode
    useEffect(() => {
        if (isEditMode && existing) {
            const existingCustomerId = existing.customerId ?? 0
            const existingPurchaseId = existing.purchaseId ?? 0
            const safeStatus = existing.status === ContractStatus.ALL ? 'ACTIVE' : existing.status

            reset({
                customerId: existingCustomerId,
                purchaseId: existingPurchaseId,
                finalPrice: existing.finalPrice,
                downPayment: existing.downPayment,
                months: existing.months,
                monthlyAmount: existing.monthlyAmount,
                startDate: existing.startDate,
                status: safeStatus,
                additionalCosts: existing.additionalCosts,
                earlyPaymentDiscountRate: existing.earlyPaymentDiscountRate,
                agreedPaymentDay: existing.agreedPaymentDay,
                contractNumber: existing.contractNumber ?? '',
                notes: existing.notes ?? '',
                partnerId: existing.partnerId ?? undefined,
                responsibleUserId: existing.responsibleUserId ?? undefined,
            })

            // Keep user-facing labels visible while relation fields are locked in edit mode
            setCustomerSearch(existing.customerName ?? '')
            setPurchaseSearch([existing.productName, existing.vendorName].filter(Boolean).join(' - '))
        }
    }, [isEditMode, existing, reset])

    const notesValue = watch('notes') ?? ''
    const selectedCustomerId = watch('customerId')
    const selectedPurchaseId = watch('purchaseId')

    const toOptionalNumber = (value: number | null | undefined): number | undefined => {
        if (value === undefined || Number.isNaN(value)) return undefined
        return value
    }

    const selectedCustomer = customers.find((c) => c.id === selectedCustomerId)
    const selectedPurchase = purchases.find((p) => p.id === selectedPurchaseId)

    useEffect(() => {
        if (!isEditMode && selectedCustomer) {
            setCustomerSearch(selectedCustomer.name)
        }
    }, [isEditMode, selectedCustomer])

    useEffect(() => {
        if (!isEditMode && selectedPurchase) {
            setPurchaseSearch(`${selectedPurchase.productName} - ${selectedPurchase.vendorName}`)
        }
    }, [isEditMode, selectedPurchase])

    const onSubmit = async (data: ContractFormData) => {
        const payload = {
            customerId: data.customerId,
            purchaseId: data.purchaseId,
            finalPrice: data.finalPrice,
            downPayment: data.downPayment,
            months: toOptionalNumber(data.months),
            monthlyAmount: toOptionalNumber(data.monthlyAmount),
            startDate: data.startDate,
            status: data.status || undefined,
            additionalCosts: toOptionalNumber(data.additionalCosts),
            earlyPaymentDiscountRate: toOptionalNumber(data.earlyPaymentDiscountRate),
            agreedPaymentDay: toOptionalNumber(data.agreedPaymentDay),
            notes: data.notes || undefined,
            contractNumber: data.contractNumber || undefined,
            partnerId: toOptionalNumber(data.partnerId),
            responsibleUserId: toOptionalNumber(data.responsibleUserId),
        }

        if (isEditMode) {
            const updated = await updateContract(contractId, payload)
            if (updated) navigate(APP_ROUTES.CONTRACTS.LIST)
        } else {
            const created = await createContract(payload)
            if (created) navigate(APP_ROUTES.CONTRACTS.LIST)
        }
    }

    if (isEditMode && fetchLoading) {
        return <div className="contract-form__loading"><LoadingSpinner size="lg" /></div>
    }

    return (
        <div>
            <div className="contract-form__header">
                <h1 className="contract-form__title">
                    {isEditMode ? t('form.editTitle') : t('form.createTitle')}
                </h1>
            </div>

            <Card>
                <form onSubmit={handleSubmit(onSubmit)} noValidate>
                    {/* ── Section 1: Customer & Purchase ─────── */}
                    <h3 className="contract-form__section-title">{t('details.contractInfo')}</h3>
                    <div className="contract-form__grid">
                        {/* Customer searchable selector */}
                        <div className="form-field">
                            <label className="form-field__label">{t('form.customerId')} *</label>
                            <Input
                                name="customerSearch"
                                placeholder={t('form.customerSearchPlaceholder')}
                                value={customerSearch}
                                disabled={isEditMode}
                                onChange={(e) => {
                                    setCustomerSearch(e.target.value)
                                    if (!isEditMode) {
                                        setValue('customerId', 0, { shouldValidate: true })
                                    }
                                }}
                            />
                            <select
                                className={`contract-form__select ${errors.customerId ? 'contract-form__select--error' : ''}`}
                                value={selectedCustomerId ?? 0}
                                disabled={isEditMode || customersLoading}
                                onChange={(e) => {
                                    setValue('customerId', Number(e.target.value), { shouldValidate: true })
                                }}
                            >
                                <option value={0}>{t('form.customerPlaceholder')}</option>
                                {!customersLoading && customers.map((c) => (
                                    <option key={c.id} value={c.id}>{`${c.name} - ${c.phone}`}</option>
                                ))}
                            </select>
                            <input type="hidden" {...register('customerId', { valueAsNumber: true })} />
                            {isEditMode && <span className="contract-form__hint">{t('form.editLockedHint')}</span>}
                            {errors.customerId && <span className="form-field__error" role="alert">{errors.customerId.message}</span>}
                        </div>

                        {/* Purchase searchable selector */}
                        <div className="form-field">
                            <label className="form-field__label">{t('form.purchaseId')} *</label>
                            <Input
                                name="purchaseSearch"
                                placeholder={t('form.purchaseSearchPlaceholder')}
                                value={purchaseSearch}
                                disabled={isEditMode}
                                onChange={(e) => {
                                    setPurchaseSearch(e.target.value)
                                    if (!isEditMode) {
                                        setValue('purchaseId', 0, { shouldValidate: true })
                                    }
                                }}
                            />
                            <select
                                className={`contract-form__select ${errors.purchaseId ? 'contract-form__select--error' : ''}`}
                                value={selectedPurchaseId ?? 0}
                                disabled={isEditMode || purchasesLoading}
                                onChange={(e) => {
                                    setValue('purchaseId', Number(e.target.value), { shouldValidate: true })
                                }}
                            >
                                <option value={0}>{t('form.purchasePlaceholder')}</option>
                                {!purchasesLoading && purchases.map((p) => (
                                    <option key={p.id} value={p.id}>{`${p.productName} - ${p.vendorName}`}</option>
                                ))}
                            </select>
                            <input type="hidden" {...register('purchaseId', { valueAsNumber: true })} />
                            {isEditMode && <span className="contract-form__hint">{t('form.editLockedHint')}</span>}
                            {errors.purchaseId && <span className="form-field__error" role="alert">{errors.purchaseId.message}</span>}
                        </div>

                        {/* Status */}
                        <div className="form-field">
                            <label className="form-field__label">{t('details.status')}</label>
                            <select
                                className={`contract-form__select ${errors.status ? 'contract-form__select--error' : ''}`}
                                {...register('status')}
                            >
                                <option value={ContractStatus.ACTIVE}>{t('status.ACTIVE')}</option>
                                <option value={ContractStatus.LATE}>{t('status.LATE')}</option>
                                <option value={ContractStatus.COMPLETED}>{t('status.COMPLETED')}</option>
                                <option value={ContractStatus.CANCELLED}>{t('status.CANCELLED')}</option>
                            </select>
                            {errors.status && <span className="form-field__error" role="alert">{errors.status.message}</span>}
                        </div>

                        {/* Contract Number */}
                        <Input
                            label={t('form.contractNumber')}
                            placeholder={t('form.contractNumberPlaceholder')}
                            error={errors.contractNumber?.message}
                            {...register('contractNumber')}
                        />

                        {/* Start Date */}
                        <Input
                            label={t('form.startDate') + ' *'}
                            type="date"
                            error={errors.startDate?.message}
                            {...register('startDate')}
                        />
                    </div>

                    {/* ── Section 2: Financial ───────────────── */}
                    <h3 className="contract-form__section-title">{t('details.financialSummary')}</h3>
                    <div className="contract-form__grid">
                        <Input
                            label={t('form.finalPrice') + ' *'}
                            type="number"
                            placeholder={t('form.finalPricePlaceholder')}
                            error={errors.finalPrice?.message}
                            {...register('finalPrice', { valueAsNumber: true })}
                        />
                        <Input
                            label={t('form.downPayment') + ' *'}
                            type="number"
                            placeholder={t('form.downPaymentPlaceholder')}
                            error={errors.downPayment?.message}
                            {...register('downPayment', { valueAsNumber: true })}
                        />
                        <Input
                            label={t('form.additionalCosts')}
                            type="number"
                            placeholder={t('form.additionalCostsPlaceholder')}
                            error={errors.additionalCosts?.message}
                            {...register('additionalCosts', {
                                setValueAs: (value) => value === '' ? undefined : Number(value),
                            })}
                        />
                        <Input
                            label={t('form.months')}
                            type="number"
                            placeholder={t('form.monthsPlaceholder')}
                            error={errors.months?.message}
                            {...register('months', {
                                setValueAs: (value) => value === '' ? undefined : Number(value),
                            })}
                        />
                        <Input
                            label={t('form.monthlyAmount')}
                            type="number"
                            placeholder={t('form.monthlyAmountPlaceholder')}
                            error={errors.monthlyAmount?.message}
                            {...register('monthlyAmount', {
                                setValueAs: (value) => value === '' ? undefined : Number(value),
                            })}
                        />
                        <Input
                            label={t('form.agreedPaymentDay')}
                            type="number"
                            placeholder={t('form.agreedPaymentDayPlaceholder')}
                            error={errors.agreedPaymentDay?.message}
                            {...register('agreedPaymentDay', {
                                setValueAs: (value) => value === '' ? undefined : Number(value),
                            })}
                        />
                        <Input
                            label={t('form.earlyPaymentDiscountRate')}
                            type="number"
                            placeholder={t('form.earlyPaymentDiscountPlaceholder')}
                            error={errors.earlyPaymentDiscountRate?.message}
                            {...register('earlyPaymentDiscountRate', {
                                setValueAs: (value) => value === '' ? undefined : Number(value),
                            })}
                        />

                        <Input
                            label={t('form.partnerId')}
                            type="number"
                            placeholder={t('form.partnerIdPlaceholder')}
                            error={errors.partnerId?.message}
                            {...register('partnerId', {
                                setValueAs: (value) => value === '' ? undefined : Number(value),
                            })}
                        />

                        <Input
                            label={t('form.responsibleUserId')}
                            type="number"
                            placeholder={t('form.responsibleUserIdPlaceholder')}
                            error={errors.responsibleUserId?.message}
                            {...register('responsibleUserId', {
                                setValueAs: (value) => value === '' ? undefined : Number(value),
                            })}
                        />
                    </div>

                    {/* ── Section 3: Notes ────────────────────── */}
                    <div className="contract-form__full" style={{ marginTop: 'var(--spacing-5)' }}>
                        <div className="form-field">
                            <label htmlFor="notes" className="form-field__label">{t('form.notes')}</label>
                            <textarea
                                id="notes"
                                className={`contract-form__textarea ${errors.notes ? 'contract-form__textarea--error' : ''}`}
                                placeholder={t('form.notesPlaceholder')}
                                maxLength={500}
                                {...register('notes')}
                            />
                            <div className="contract-form__char-count">{notesValue.length} / 500</div>
                            {errors.notes && <span className="form-field__error" role="alert">{errors.notes.message}</span>}
                        </div>
                    </div>

                    {/* ── Actions ─────────────────────────────── */}
                    <div className="contract-form__actions">
                        <Button variant="secondary" onClick={() => navigate(APP_ROUTES.CONTRACTS.LIST)} disabled={submitting}>
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

