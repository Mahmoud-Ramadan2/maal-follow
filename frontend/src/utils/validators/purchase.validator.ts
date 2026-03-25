import { z } from 'zod'
import i18n from '@config/i18n.config'

// ────────────────────────────────────────────────────────────
// Helper — read a validation message from the i18n store
// ────────────────────────────────────────────────────────────

/**
 * Returns the translated validation message for the given key.
 *
 */
const t = (key: string): string => i18n.t(key, { ns: 'validation' })

// ────────────────────────────────────────────────────────────
// 1. purchaseCreateSchema
// ────────────────────────────────────────────────────────────

export const purchaseCreateSchema
    = z.object({
    // ── vendorId ─────────────────────────────────────────
    // @NotNull + @Positive
    vendorId: z
        .number({ error: () => t('vendorId.required') })
        .int()
        .positive({ error: t('vendorId.positive') }),

    // ── productName ──────────────────────────────────────
    // @NotBlank
    productName: z
        .string({ error: () => t('productName.required') })
        .min(1, { error: t('productName.required') }),

    // ── buyPrice ─────────────────────────────────────────
    // @NotNull + @DecimalMin("1") + @Digits(integer=10, fraction=2)
    buyPrice: z
        .number({ error: () => t('buyPrice.required') })
        .min(100, { error: t('buyPrice.invalid') })
        .refine(
            (val) => /^\d{1,10}(\.\d{1,2})?$/.test(val.toString()),
            { error: () => t('buyPrice.format') },
        ),

    // ── purchaseDate ─────────────────────────────────────
    // @NotNull + @PastOrPresent
    purchaseDate: z
        .string({ error: () => t('purchaseDate.required') })
        .min(1, { error: t('purchaseDate.required') })
        .refine(
            (val) => {
                const input = new Date(val)
                const today = new Date()
                // Compare date-only (ignore time)
                today.setHours(23, 59, 59, 999)
                return !isNaN(input.getTime()) && input <= today
            },
            { error: () => t('purchaseDate.invalid') },
        ),

    // ── notes ────────────────────────────────────────────
    // @Size(max = 500), optional
    notes: z
        .string()
        .max(500, { error: t('notes.size') })
        .optional()
        .or(z.literal('')),
})

// ────────────────────────────────────────────────────────────
// 2. purchaseUpdateSchema
// ────────────────────────────────────────────────────────────

/**
 * Zod schema for the Purchase edit form.
 *
 * Identical to `purchaseCreateSchema` because the backend's
 * `PUT /purchases/{id}` expects the same `PurchaseRequest` DTO
 * with the same validations.
 */
export const purchaseUpdateSchema
    = purchaseCreateSchema

// ────────────────────────────────────────────────────────────
// 3. Inferred TypeScript type
// ────────────────────────────────────────────────────────────

/**
 * TypeScript type derived from the Zod schema.
 *
 * Use this as the generic for `useForm<PurchaseFormData>()`
 * so the form fields are fully typed.
 */
export type PurchaseFormData
    = z.infer<typeof purchaseCreateSchema>

// ────────────────────────────────────────────────────────────
// 4. Standalone validation helper
// ────────────────────────────────────────────────────────────

/**
 * Result of a standalone validation via `validatePurchase()`.
 */
interface ValidationResult {
    /** `true` when data passes all rules */
    success: boolean
    /** Parsed & typed data — only present when `success` is `true` */
    data?: PurchaseFormData
    /** Field-level error map — only present when `success` is `false` */
    errors?: Record<string, string>
}

/**
 * Validate purchase data outside of react-hook-form.
 *
 * Useful for programmatic validation (e.g. before calling
 * `purchaseApi.create()` directly without a form).
 *
 * @param data - Raw input data to validate.
 * @returns A `ValidationResult` with either `data` or `errors`.
 *
 * @example
 * ```ts
 * const result = validatePurchase(rawInput)
 * if (result.success) {
 *     await purchaseApi.create(result.data)
 * } else {
 *     console.log(result.errors)
 *     // { buyPrice: "Buy price must be at least 1" }
 * }
 * ```
 */
export function validatePurchase(data: unknown): ValidationResult {
    const result = purchaseCreateSchema.safeParse(data)

    if (result.success) {
        return { success: true, data: result.data }
    }

    // Flatten Zod issues into a { fieldName: message } map
    const errors: Record<string, string> = {}
    for (const issue of result.error.issues) {
        const field = issue.path.join('.')
        // Keep only the first error per field
        if (!errors[field]) {
            errors[field] = issue.message
        }
    }

    return { success: false, errors }
}
