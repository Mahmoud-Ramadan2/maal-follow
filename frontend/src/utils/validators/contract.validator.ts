import { z } from 'zod'
import i18n from '@config/i18n.config'

const t = (key: string): string => i18n.t(key, { ns: 'validation' })

// ────────────────────────────────────────────────────────────
// 1. contractCreateSchema
// ────────────────────────────────────────────────────────────

export const contractCreateSchema = z.object({
    customerId: z
        .number({ error: () => t('contract.customerId.required') })
        .positive({ error: t('contract.customerId.required') }),

    purchaseId: z
        .number({ error: () => t('contract.purchaseId.required') })
        .positive({ error: t('contract.purchaseId.required') }),

    finalPrice: z
        .number({ error: () => t('contract.finalPrice.required') })
        .min(0, { error: t('contract.finalPrice.min') }),

    downPayment: z
        .number({ error: () => t('contract.downPayment.required') })
        .min(0, { error: t('contract.downPayment.min') }),

    months: z
        .number()
        .int()
        .min(1, { error: t('contract.months.min') })
        .max(60, { error: t('contract.months.max') })
        .optional(),

    monthlyAmount: z
        .number()
        .min(1, { error: t('contract.monthlyAmount.min') })
        .optional(),

    startDate: z
        .string({ error: () => t('contract.startDate.required') })
        .min(1, { error: t('contract.startDate.required') }),

    additionalCosts: z
        .number()
        .min(0, { error: t('contract.additionalCosts.min') })
        .optional(),

    earlyPaymentDiscountRate: z
        .number()
        .min(0, { error: t('contract.earlyPaymentDiscountRate.min') })
        .max(100, { error: t('contract.earlyPaymentDiscountRate.max') })
        .optional(),

    agreedPaymentDay: z
        .number()
        .int()
        .min(1, { error: t('contract.agreedPaymentDay.invalid') })
        .max(31, { error: t('contract.agreedPaymentDay.invalid') })
        .optional(),

    status: z.enum(['ACTIVE', 'COMPLETED', 'LATE', 'CANCELLED']).optional(),

    partnerId: z
        .number()
        .positive()
        .optional()
        .nullable(),

    contractNumber: z
        .string()
        .max(50, { error: t('contract.contractNumber.size') })
        .optional()
        .or(z.literal('')),

    notes: z
        .string()
        .max(500, { error: t('notes.size') })
        .optional()
        .or(z.literal('')),

    responsibleUserId: z
        .number()
        .positive()
        .optional()
        .nullable(),
}).superRefine((data, ctx) => {
    if (data.months === undefined && data.monthlyAmount === undefined) {
        const message = t('contract.schedule.required')
        ctx.addIssue({ code: z.ZodIssueCode.custom, message, path: ['months'] })
        ctx.addIssue({ code: z.ZodIssueCode.custom, message, path: ['monthlyAmount'] })
    }
})

export const contractUpdateSchema = contractCreateSchema

export type ContractFormData = z.infer<typeof contractCreateSchema>

// ────────────────────────────────────────────────────────────
// 2. contractExpenseSchema
// ────────────────────────────────────────────────────────────

export const contractExpenseSchema = z.object({
    expenseType: z
        .string({ error: () => t('contract.expenseType.required') })
        .min(1, { error: t('contract.expenseType.required') }),

    amount: z
        .number({ error: () => t('contract.amount.required') })
        .min(0.01, { error: t('contract.amount.min') }),

    description: z
        .string()
        .max(255, { error: t('contract.description.size') })
        .optional()
        .or(z.literal('')),

    expenseDate: z
        .string({ error: () => t('contract.expenseDate.required') })
        .min(1, { error: t('contract.expenseDate.required') }),

    paidBy: z.string().optional(),

    receiptNumber: z
        .string()
        .max(100, { error: t('contract.receiptNumber.size') })
        .optional()
        .or(z.literal('')),

    notes: z
        .string()
        .max(500, { error: t('notes.size') })
        .optional()
        .or(z.literal('')),
})

export type ContractExpenseFormData = z.infer<typeof contractExpenseSchema>

// ────────────────────────────────────────────────────────────
// 3. Validation helpers
// ────────────────────────────────────────────────────────────

interface ValidationResult<T> {
    success: boolean
    data?: T
    errors?: Record<string, string>
}

function validate<T>(schema: z.ZodSchema<T>, data: unknown): ValidationResult<T> {
    const result = schema.safeParse(data)
    if (result.success) return { success: true, data: result.data }
    const errors: Record<string, string> = {}
    for (const issue of result.error.issues) {
        const field = issue.path.join('.')
        if (!errors[field]) errors[field] = issue.message
    }
    return { success: false, errors }
}

export function validateContract(data: unknown) {
    return validate(contractCreateSchema, data)
}

export function validateContractExpense(data: unknown) {
    return validate(contractExpenseSchema, data)
}

