import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { Button, Input } from '@components/common'
import Card from '@components/ui/Card'
import { useAuth } from '@contexts/useAuth'
import { consumeRedirectAfterLogin } from '@services/auth/session'
import { APP_ROUTES } from '@/router/routes.config'
import { type LoginFormData, loginSchema } from '@utils/validators/login.validator.ts'

export default function LoginPage(): ReactNode {
    const { t } = useTranslation('user')
    const navigate = useNavigate()
    const { login, isAuthenticated, isLoading } = useAuth()
    const [submitError, setSubmitError] = useState<string | null>(null)

    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm<LoginFormData>({
        resolver: zodResolver(loginSchema),
        defaultValues: {
            email: '',
            password: '',
        },
    })

    useEffect(() => {
        if (isLoading || !isAuthenticated) {
            return
        }

        const redirectTo = consumeRedirectAfterLogin() ?? APP_ROUTES.DASHBOARD
        navigate(redirectTo, { replace: true })
    }, [isAuthenticated, isLoading, navigate])

    const onSubmit = async (data: LoginFormData) => {
        setSubmitError(null)

        try {
            await login(data.email, data.password ?? '')
            const redirectTo = consumeRedirectAfterLogin() ?? APP_ROUTES.DASHBOARD
            navigate(redirectTo, { replace: true })
            toast.success(t('auth.messages.loginSuccess'))
        } catch (error) {
            console.error('Auth check failed:', error)
            const message = t('auth.errors.invalidCredentials')
            setSubmitError(message)
        }
    }

    if (!isLoading && isAuthenticated) {
        return <Navigate to={APP_ROUTES.DASHBOARD} replace />
    }

    return (
        <div className="flex min-h-screen items-center justify-center p-4">
            <div style={{ width: '100%', maxWidth: '480px' }}>
                <Card>
                    <div className="flex flex-col gap-6">
                        <div className="text-center">
                            <h1 className="text-3xl font-bold">{t('auth.loginTitle')}</h1>
                            <p className="text-secondary mt-2">{t('auth.loginSubtitle')}</p>
                        </div>

                        <form className="flex flex-col gap-4" onSubmit={handleSubmit(onSubmit)}>
                            <Input
                                label={t('form.email')}
                                placeholder={t('auth.placeholders.email')}
                                autoComplete="email"
                                error={errors.email?.message}
                                {...register('email')}
                            />

                            <Input
                                label={t('form.password')}
                                type="password"
                                placeholder={t('auth.placeholders.password')}
                                autoComplete="current-password"
                                error={errors.password?.message}
                                {...register('password')}
                            />

                            {submitError && (
                                <p className="text-error text-sm" role="alert">
                                    {submitError}
                                </p>
                            )}

                            <Button type="submit" loading={isSubmitting} fullWidth>
                                {t('auth.actions.login')}
                            </Button>
                        </form>
                    </div>
                </Card>
            </div>
        </div>
    )
}
