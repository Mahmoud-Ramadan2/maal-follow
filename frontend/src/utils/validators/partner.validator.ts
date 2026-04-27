import { z } from 'zod'
import i18n from '@config/i18n.config'

const t = (key: string): string => i18n.t(key, { ns: 'validation' })

const hasMaxTwoDecimals = (value: number): boolean => Number.isInteger(value * 100)

// ────────────────────────────────────────────────────────────
// partnerCreateSchema
// ────────────────────────────────────────────────────────────

export const partnerCreateSchema = z.object({
    name: z
        .string({ error: () => t('name.required') })
        .min(4, { error: () => t('name.minSize') })
        .max(200, { error: () => t('partner.name.maxSize') }),

    phone: z
        .string({ error: () => t('phone.required') })
        .min(1, { error: () => t('phone.required') })
        .regex(/^\+?[0-9]{10,15}$/, { error: () => t('phone.pattern') }),

    nationalId: z
        .string({ error: () => t('nationalId.required') })
        .min(6, { error: () => t('nationalId.size') })
        .max(14, { error: () => t('nationalId.size') })
        .regex(/^[0-9]+$/, { error: () => t('nationalId.pattern') }),

    address: z
        .string()
        .max(200, { error: () => t('partner.address.size') })
        .optional()
        .or(z.literal('')),

    partnershipType: z
        .string({ error: () => t('partner.partnershipType.required') })
        .min(1, { error: () => t('partner.partnershipType.required') }),

    totalInvestment: z
        .number({ error: () => t('partner.totalInvestment.required') })
        .min(100, { error: () => t('partner.totalInvestment.min') })
        .refine(hasMaxTwoDecimals, { error: () => t('partner.totalInvestment.format') }),

    investmentStartDate: z
        .string({ error: () => t('partner.investmentStartDate.required') })
        .min(1, { error: () => t('partner.investmentStartDate.required') }),

    profitCalculationStartMonth: z
        .string()
        .max(7, { error: () => t('partner.profitCalculationStartMonth.size') })
        .regex(/^\d{4}-\d{2}$/, { error: () => t('partner.profitCalculationStartMonth.pattern') })
        .optional()
        .or(z.literal('')),

    profitSharingActive: z.boolean().optional(),

    notes: z
        .string()
        .max(500, { error: () => t('notes.size') })
        .optional()
        .or(z.literal('')),

    createdBy: z
        .number({ error: () => t('partner.createdBy.required') })
        .positive({ error: () => t('partner.createdBy.required') }),
})

export const partnerUpdateSchema = partnerCreateSchema

export type PartnerFormData = z.infer<typeof partnerCreateSchema>

// ────────────────────────────────────────────────────────────
// Validation helpers
// ────────────────────────────────────────────────────────────

interface ValidationResult<T> {
    success: boolean
    data?: T
    errors?: Record<string, string>
}

export function validatePartner(data: unknown): ValidationResult<PartnerFormData> {
    const result = partnerCreateSchema.safeParse(data)
    if (result.success) return { success: true, data: result.data }
    const errors: Record<string, string> = {}
    for (const issue of result.error.issues) {
        const field = issue.path.join('.')
        if (!errors[field]) errors[field] = issue.message
    }
    return { success: false, errors }
}

