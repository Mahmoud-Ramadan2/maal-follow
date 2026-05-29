import { z } from 'zod'
import i18n from '@config/i18n.config'

const t = (key: string): string => i18n.t(key, { ns: 'validation' })

export const loginSchema = z.object({
    email: z
        .string().min(4, t('user.email.size')).max(100, t('user.email.size'))
        .regex( /^[^\s@]+@[^\s@]+\.[^\s@]+$/, t('user.email.invalid')),
    password: z.string().min(6, { error: t('user.password.size') }).max(100, { error: t('user.password.size') }).optional().or(z.literal('')),
})


export type LoginFormData = z.infer<typeof loginSchema>


