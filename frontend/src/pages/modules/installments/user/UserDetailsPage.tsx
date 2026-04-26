import type { ReactNode } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'

import { useUser, useUserDelete } from '@hooks/modules'
import Button from '@components/common/Button'
import Card from '@components/ui/Card'
import LoadingSpinner from '@components/ui/LoadingSpinner'
import { APP_ROUTES, ROUTE_HELPERS } from '@/router/routes.config'
import './UserDetailsPage.css'

export default function UserDetailsPage(): ReactNode {
    const { id } = useParams<{ id: string }>()
    const userId = Number(id)
    const navigate = useNavigate()
    const { t } = useTranslation('user')
    const { t: tc } = useTranslation('common')

    const { user, loading, error } = useUser(userId)
    const { deleteUser, loading: deleting } = useUserDelete()

    if (loading) {
        return <div className="user-details__center"><LoadingSpinner size="lg" /></div>
    }

    if (error || !user) {
        return (
            <div className="user-details__center">
                <p>{error || t('details.notFound')}</p>
                <Button onClick={() => navigate(APP_ROUTES.USERS.LIST)}>{t('details.backToList')}</Button>
            </div>
        )
    }

    return (
        <div>
            <div className="user-details__header">
                <div className="user-details__header-left">
                    <Button variant="secondary" size="sm" onClick={() => navigate(APP_ROUTES.USERS.LIST)}>← {t('details.backToList')}</Button>
                    <h1 className="user-details__title">{t('details.title', { id: user.id })}</h1>
                </div>
                <div className="user-details__actions">
                    <Button variant="secondary" onClick={() => navigate(ROUTE_HELPERS.USER_EDIT(user.id))}>{tc('edit')}</Button>
                    <Button
                        variant="danger"
                        loading={deleting}
                        onClick={async () => {
                            const ok = await deleteUser(user.id)
                            if (ok) navigate(APP_ROUTES.USERS.LIST)
                        }}
                    >
                        {tc('delete')}
                    </Button>
                </div>
            </div>

            <Card title={t('details.userInfo')}>
                <div className="user-details__grid">
                    <div><strong>{t('details.name')}:</strong> {user.name}</div>
                    <div><strong>{t('details.email')}:</strong> {user.email || '—'}</div>
                    <div><strong>{t('details.phone')}:</strong> {user.phone || '—'}</div>
                    <div><strong>{t('details.role')}:</strong> {t(`role.${user.role}`, user.role)}</div>
                    <div><strong>{t('details.createdAt')}:</strong> {new Date(user.createdAt).toLocaleString()}</div>
                </div>
            </Card>
        </div>
    )
}

