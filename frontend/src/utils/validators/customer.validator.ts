import { z } from 'zod'
import i18n from '@config/i18n.config'

// ────────────────────────────────────────────────────────────
// Helper — read a validation message from the i18n store
// ────────────────────────────────────────────────────────────

const t = (key: string): string => i18n.t(key, { ns: 'validation' })

// ────────────────────────────────────────────────────────────
// 1. customerCreateSchema
// ────────────────────────────────────────────────────────────

export const customerCreateSchema = z.object({
    // ── name ─────────────────────────────────────────────
    // @NotBlank + @Size(min=4, max=50)
    name: z
        .string({ error: () => t('name.required') })
        .min(4, { error: t('name.minSize') })
        .max(50, { error: t('name.maxSize') }),

    // ── phone ────────────────────────────────────────────
    // @NotBlank + @Pattern(regexp = "^\\+?[0-9]{10,15}$")
    phone: z
        .string({ error: () => t('phone.required') })
        .min(1, { error: t('phone.required') })
        .regex(/^\+?[0-9]{10,15}$/, { error: t('phone.pattern') }),

    // ── address ──────────────────────────────────────────
    // @NotBlank + @Size(max=100)
    address: z
        .string({ error: () => t('address.required') })
        .min(1, { error: t('address.required') })
        .max(100, { error: t('address.size') }),

    // ── nationalId ───────────────────────────────────────
    // @NotBlank + @Size(min=6, max=14) + @Pattern(digits only)
    nationalId: z
        .string({ error: () => t('nationalId.required') })
        .min(6, { error: t('nationalId.size') })
        .max(14, { error: t('nationalId.size') })
        .regex(/^[0-9]+$/, { error: t('nationalId.pattern') }),

    // ── notes ────────────────────────────────────────────
    // @Size(max = 500), optional
    notes: z
        .string()
        .max(500, { error: t('notes.size') })
        .optional()
        .or(z.literal('')),
})

// ────────────────────────────────────────────────────────────
// 2. customerUpdateSchema
// ────────────────────────────────────────────────────────────

export const customerUpdateSchema = customerCreateSchema

// ────────────────────────────────────────────────────────────
// 3. Inferred TypeScript type
// ────────────────────────────────────────────────────────────

export type CustomerFormData = z.infer<typeof customerCreateSchema>

// ────────────────────────────────────────────────────────────
// 4. Standalone validation helper
// ────────────────────────────────────────────────────────────

interface ValidationResult {
    success: boolean
    data?: CustomerFormData
    errors?: Record<string, string>
}

export function validateCustomer(data: unknown): ValidationResult {
    const result = customerCreateSchema.safeParse(data)

    if (result.success) {
        return { success: true, data: result.data }
    }

    const errors: Record<string, string> = {}
    for (const issue of result.error.issues) {
        const field = issue.path.join('.')
        if (!errors[field]) {
            errors[field] = issue.message
        }
    }

    return { success: false, errors }
}

