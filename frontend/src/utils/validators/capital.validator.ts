import { z } from 'zod'
import i18n from '@config/i18n.config'

const t = (key: string): string => i18n.t(key, { ns: 'validation' })

// ── Capital Pool Schema ─────────────────────────────────────
export const capitalPoolSchema = z.object({
    totalAmount: z
        .number({ error: () => t('capital.totalAmount.required') })
        .min(0, { error: t('capital.totalAmount.positive') }),
    ownerContribution: z
        .number({ error: () => t('capital.ownerContribution.required') })
        .min(0, { error: t('capital.ownerContribution.positive') }),
    partnerContributions: z
        .number({ error: () => t('capital.partnerContributions.required') })
        .min(0, { error: t('capital.partnerContributions.positive') }),
    description: z
        .string()
        .max(500, { error: t('notes.size') })
        .optional()
        .or(z.literal('')),
})

export type CapitalPoolFormData = z.infer<typeof capitalPoolSchema>

// ── Capital Transaction Schema ──────────────────────────────
export const capitalTransactionSchema = z.object({
    transactionType: z
        .string({ error: () => t('capital.transactionType.required') })
        .min(1, { error: t('capital.transactionType.required') }),
    amount: z
        .number({ error: () => t('capital.amount.required') })
        .positive({ error: t('capital.amount.positive') }),
    partnerId: z.number().positive().optional(),
    contractId: z.number().positive().optional(),
    description: z
        .string()
        .max(500, { error: t('capital.description.size') })
        .optional()
        .or(z.literal('')),
})

export type CapitalTransactionFormData = z.infer<typeof capitalTransactionSchema>

// ── Validation Helpers ──────────────────────────────────────
interface ValidationResult<T> {
    success: boolean
    data?: T
    errors?: Record<string, string>
}

export function validateCapitalPool(data: unknown): ValidationResult<CapitalPoolFormData> {
    const result = capitalPoolSchema.safeParse(data)
    if (result.success) return { success: true, data: result.data }
    const errors: Record<string, string> = {}
    for (const issue of result.error.issues) {
        const field = issue.path.join('.')
        if (!errors[field]) errors[field] = issue.message
    }
    return { success: false, errors }
}

export function validateCapitalTransaction(data: unknown): ValidationResult<CapitalTransactionFormData> {
    const result = capitalTransactionSchema.safeParse(data)
    if (result.success) return { success: true, data: result.data }
    const errors: Record<string, string> = {}
    for (const issue of result.error.issues) {
        const field = issue.path.join('.')
        if (!errors[field]) errors[field] = issue.message
    }
    return { success: false, errors }
}

