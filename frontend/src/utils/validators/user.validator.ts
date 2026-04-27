import { z } from 'zod'
import i18n from '@config/i18n.config'

const t = (key: string): string => i18n.t(key, { ns: 'validation' })

export const userCreateSchema = z.object({
    name: z.string({ error: () => t('name.required') }).min(2, { error: t('name.minSize') }).max(200, { error: t('name.maxSize') }),
    email: z.string().email({ error: t('user.email.invalid') }).max(200, { error: t('user.email.size') }).optional().or(z.literal('')),
    password: z.string({ error: () => t('user.password.required') }).min(6, { error: t('user.password.size') }).max(100, { error: t('user.password.size') }),
    role: z.enum(['ADMIN', 'OWNER', 'MANAGER', 'COLLECTOR', 'ACCOUNTANT', 'USER'], { error: () => t('user.role.required') }),
    phone: z.string().max(20, { error: t('user.phone.size') }).optional().or(z.literal('')),
})

export const userUpdateSchema = userCreateSchema.extend({
    password: z.string().min(6, { error: t('user.password.size') }).max(100, { error: t('user.password.size') }).optional().or(z.literal('')),
})

export type UserCreateFormData = z.infer<typeof userCreateSchema>
export type UserUpdateFormData = z.infer<typeof userUpdateSchema>


