import { z } from 'zod'
import i18n from '@config/i18n.config'


// ────────────────────────────────────────────────────────────
// Helper — read a validation message from the i18n store
// ────────────────────────────────────────────────────────────

const t = (key: string): string => i18n.t(key, { ns: 'validation' })


const hasMaxTwoDecimals = (value: number): boolean => Number.isInteger(value * 100)

export const paymentCreateSchema = z.object({
    idempotencyKey: z
        .string({ error: () => t('payment.idempotencyKey.required') })
        .min(1, { error: () => t('payment.idempotencyKey.required') })
        .max(100, { error: () => t('payment.idempotencyKey.size') }),

    installmentScheduleId: z.number().positive().optional(),

    amount: z
        .number({ error: () => t('payment.amount.required') })
        .min(50, { error: () => t('payment.amount.min') })
        .refine(hasMaxTwoDecimals, { error: () => t('payment.amount.format') }),

    paymentMethod: z
        .string({ error: () => t('payment.paymentMethod.required') })
        .min(1, { error: () => t('payment.paymentMethod.required') }),

    actualPaymentDate: z
        .string({ error: () => t('payment.actualPaymentDate.required') })
        .min(1, { error: () => t('payment.actualPaymentDate.required') }),

    agreedPaymentMonth: z
        .string()
        .regex(/^\d{4}-\d{2}$/, { error: () => t('payment.agreedPaymentMonth.format') })
        .optional(),

    extraExpenses: z
        .number()
        .min(0, { error: () => t('payment.extraExpenses.min') })
        .refine(hasMaxTwoDecimals, { error: () => t('payment.amount.format') })
        .optional(),

    isEarlyPayment: z.boolean().optional(),

    discountAmount: z
        .number()
        .min(0, { error: () => t('payment.discountAmount.min') })
        .refine(hasMaxTwoDecimals, { error: () => t('payment.discountAmount.format') })
        .optional(),

    notes: z
        .string()
        .max(500, { error: () => t('notes.size') })
        .optional()
        .or(z.literal('')),

    collectorId: z.number().positive().optional(),
})

export const paymentUpdateSchema = paymentCreateSchema

export type PaymentFormData = z.infer<typeof paymentCreateSchema>

interface ValidationResult<T> {
    success: boolean
    data?: T
    errors?: Record<string, string>
}

export function validatePayment(data: unknown): ValidationResult<PaymentFormData> {
    const result = paymentCreateSchema.safeParse(data)
    if (result.success) return { success: true, data: result.data }
    const errors: Record<string, string> = {}
    for (const issue of result.error.issues) {
        const field = issue.path.join('.')
        if (!errors[field]) errors[field] = issue.message
    }
    return { success: false, errors }
}

