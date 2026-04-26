import { useEffect } from 'react'
import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'

import { useUser, useUserCreate, useUserUpdate } from '@hooks/modules'
import { userCreateSchema, userUpdateSchema } from '@utils/validators/user.validator'
import type { UserCreateFormData, UserUpdateFormData } from '@utils/validators/user.validator'
import { UserRole } from '@/types/modules/user.types'
import Button from '@components/common/Button'
import Input from '@components/common/Input'
import Card from '@components/ui/Card'
import LoadingSpinner from '@components/ui/LoadingSpinner'
import { APP_ROUTES } from '@/router/routes.config'
import './UserFormPage.css'

interface UserFormPageProps {
    userId?: number
}

export default function UserFormPage({ userId }: UserFormPageProps): ReactNode {
    const { t } = useTranslation('user')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()

    const isEditMode = userId !== undefined

    const { user: existingUser, loading: fetchLoading } = useUser(isEditMode ? userId : 0)
    const { createUser, loading: createLoading } = useUserCreate()
    const { updateUser, loading: updateLoading } = useUserUpdate()

    const submitting = createLoading || updateLoading

    const {
        register,
        handleSubmit,
        reset,
        formState: { errors },
    } = useForm<UserCreateFormData | UserUpdateFormData>({
        resolver: zodResolver(isEditMode ? userUpdateSchema : userCreateSchema),
        defaultValues: {
            name: '',
            email: '',
            password: '',
            phone: '',
            role: 'USER',
        },
    })

    useEffect(() => {
        if (!isEditMode || !existingUser) return
        reset({
            name: existingUser.name,
            email: existingUser.email || '',
            password: '',
            phone: existingUser.phone || '',
            role: existingUser.role,
        })
    }, [isEditMode, existingUser, reset])

    const onSubmit = async (data: UserCreateFormData | UserUpdateFormData) => {
        const payload = {
            name: data.name,
            email: data.email || undefined,
            phone: data.phone || undefined,
            role: data.role,
            password: data.password || undefined,
        }

        if (isEditMode) {
            if (!userId) return
            const updated = await updateUser(userId, payload)
            if (updated) navigate(APP_ROUTES.USERS.LIST)
            return
        }

        const created = await createUser(payload)
        if (created) navigate(APP_ROUTES.USERS.LIST)
    }

    if (isEditMode && fetchLoading) {
        return <div className="user-form__loading"><LoadingSpinner size="lg" /></div>
    }

    return (
        <div>
            <div className="user-form__header">
                <h1 className="user-form__title">{isEditMode ? t('form.editTitle') : t('form.createTitle')}</h1>
            </div>

            <Card>
                <form className="user-form" onSubmit={handleSubmit(onSubmit)}>
                    <Input label={t('form.name')} required error={errors.name?.message} {...register('name')} />
                    <Input label={t('form.email')} type="email" error={errors.email?.message} {...register('email')} />
                    <Input label={t('form.phone')} error={errors.phone?.message} {...register('phone')} />
                    <Input
                        label={isEditMode ? t('form.passwordOptional') : t('form.password')}
                        type="password"
                        error={errors.password?.message}
                        {...register('password')}
                    />

                    <div className="user-form__field user-form__field--full">
                        <label htmlFor="role" className="user-form__label">{t('form.role')}</label>
                        <select id="role" className="user-form__select" {...register('role')}>
                            {Object.values(UserRole).map((value) => (
                                <option key={value} value={value}>{t(`role.${value}`, value)}</option>
                            ))}
                        </select>
                        {errors.role?.message && <span className="user-form__error">{errors.role.message}</span>}
                    </div>

                    <div className="user-form__actions user-form__field--full">
                        <Button type="button" variant="secondary" onClick={() => navigate(APP_ROUTES.USERS.LIST)} disabled={submitting}>{tc('cancel')}</Button>
                        <Button type="submit" loading={submitting}>{isEditMode ? t('form.submitUpdate') : t('form.submitCreate')}</Button>
                    </div>
                </form>
            </Card>
        </div>
    )
}



