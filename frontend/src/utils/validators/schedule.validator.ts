import { z } from 'zod'
import i18n from '@config/i18n.config'

const t = (key: string): string => i18n.t(key, { ns: 'validation' })

/** Schema for creating a schedule row manually. */
export const scheduleCreateSchema = z.object({
    contractId: z.number({ error: () => t('schedule.contractId.required') }).positive({ error: t('schedule.contractId.required') }),
    sequenceNumber: z.number({ error: () => t('schedule.sequenceNumber.required') }).int().positive({ error: t('schedule.sequenceNumber.positive') }),
    profitMonth: z.string({ error: () => t('schedule.profitMonth.required') }).regex(/^\d{4}-\d{2}$/, { error: t('schedule.profitMonth.format') }),
    amount: z.number({ error: () => t('schedule.amount.required') }).min(0.01, { error: t('schedule.amount.min') }),
    dueDate: z.string({ error: () => t('schedule.dueDate.required') }).min(1, { error: t('schedule.dueDate.required') }),
    discountApplied: z.number().min(0, { error: t('schedule.discountApplied.min') }).optional(),
    isFinalPayment: z.boolean().optional(),
    status: z.enum(['PENDING', 'PAID', 'LATE', 'PARTIALLY_PAID', 'CANCELLED']).optional(),
    collectorId: z.number().positive({ error: t('schedule.collectorId.positive') }).optional(),
    notes: z.string().max(500, { error: t('notes.size') }).optional().or(z.literal('')),
})

/** Same constraints as create for now. */
export const scheduleUpdateSchema = scheduleCreateSchema

export type ScheduleFormData = z.infer<typeof scheduleCreateSchema>

interface ValidationResult<T> {
    success: boolean
    data?: T
    errors?: Record<string, string>
}

/** Validates unknown payload and returns a flattened field-error map. */
export function validateSchedule(data: unknown): ValidationResult<ScheduleFormData> {
    const result = scheduleCreateSchema.safeParse(data)
    if (result.success) return { success: true, data: result.data }

    const errors: Record<string, string> = {}
    for (const issue of result.error.issues) {
        const field = issue.path.join('.')
        if (!errors[field]) errors[field] = issue.message
    }

    return { success: false, errors }
}

