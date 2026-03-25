import { z } from 'zod'
import i18n from '@config/i18n.config'

// ────────────────────────────────────────────────────────────
// Helper — read a validation message from the i18n store
// ────────────────────────────────────────────────────────────

/**
 * Returns the translated validation message for the given key.
 */
const t = (key: string): string => i18n.t(key, { ns: 'validation' })

// ────────────────────────────────────────────────────────────
// 1. vendorCreateSchema
// ────────────────────────────────────────────────────────────

export const vendorCreateSchema = z.object({
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

    // ── notes ────────────────────────────────────────────
    // @Size(max = 500), optional
    notes: z
        .string()
        .max(500, { error: t('notes.size') })
        .optional()
        .or(z.literal('')),
})

// ────────────────────────────────────────────────────────────
// 2. vendorUpdateSchema
// ────────────────────────────────────────────────────────────

/**
 * Zod schema for the Vendor edit form.
 *
 * Identical to `vendorCreateSchema` because the backend's
 * `PATCH /vendors/{id}` expects the same `VendorRequest` DTO
 * with the same validations.
 */
export const vendorUpdateSchema = vendorCreateSchema

// ────────────────────────────────────────────────────────────
// 3. Inferred TypeScript type
// ────────────────────────────────────────────────────────────

/**
 * TypeScript type derived from the Zod schema.
 *
 * Use this as the generic for `useForm<VendorFormData>()`
 * so the form fields are fully typed.
 */
export type VendorFormData = z.infer<typeof vendorCreateSchema>

// ────────────────────────────────────────────────────────────
// 4. Standalone validation helper
// ────────────────────────────────────────────────────────────

/**
 * Result of a standalone validation via `validateVendor()`.
 */
interface ValidationResult {
    /** `true` when data passes all rules */
    success: boolean
    /** Parsed & typed data — only present when `success` is `true` */
    data?: VendorFormData
    /** Field-level error map — only present when `success` is `false` */
    errors?: Record<string, string>
}

/**
 * Validate vendor data outside of react-hook-form.
 *
 * Useful for programmatic validation (e.g. before calling
 * `vendorApi.create()` directly without a form).
 *
 * @param data - Raw input data to validate.
 * @returns A `ValidationResult` with either `data` or `errors`.
 *
 * @example
 * ```ts
 * const result = validateVendor(rawInput)
 * if (result.success) {
 *     await vendorApi.create(result.data)
 * } else {
 *     console.log(result.errors)
 *     // { name: "Name must be between 4 and 50 characters" }
 * }
 * ```
 */
export function validateVendor(data: unknown): ValidationResult {
    const result = vendorCreateSchema.safeParse(data)

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

