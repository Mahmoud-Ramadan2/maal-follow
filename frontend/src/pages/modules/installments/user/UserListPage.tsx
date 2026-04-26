import { useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'

import { useDebounce, usePagination } from '@hooks/common'
import { useUsers, useUserDelete } from '@hooks/modules'
import type { UserFilters, AppUser, UserRole } from '@/types/modules/user.types'
import Table from '@components/common/Table'
import type { TableColumn } from '@components/common/Table'
import Input from '@components/common/Input'
import Button from '@components/common/Button'
import Card from '@components/ui/Card'
import { APP_ROUTES, ROUTE_HELPERS } from '@/router/routes.config'
import './UserListPage.css'

const PAGE_SIZE_OPTIONS = [10, 25, 50] as const

export default function UserListPage(): ReactNode {
    const { t } = useTranslation('user')
    const { t: tc } = useTranslation('common')
    const navigate = useNavigate()

    const [search, setSearch] = useState('')
    const [role, setRole] = useState<'ALL' | UserRole>('ALL')
    const debouncedSearch = useDebounce(search, 350)

    const { page, size, nextPage, prevPage, setSize } = usePagination()

    const filters = useMemo<UserFilters>(() => ({
        page,
        size,
        ...(debouncedSearch.trim() ? { search: debouncedSearch.trim() } : {}),
        ...(role !== 'ALL' ? { role } : {}),
    }), [page, size, debouncedSearch, role])

    const { users, loading, error, totalPages, totalElements, refetch } = useUsers(filters)
    const { deleteUser, loading: deleteLoading } = useUserDelete()

    const from = totalElements === 0 ? 0 : page * size + 1
    const to = Math.min((page + 1) * size, totalElements)

    const columns = useMemo<TableColumn<AppUser>[]>(() => [
        { key: 'name', label: t('columns.name') },
        { key: 'email', label: t('columns.email'), render: (row) => row.email || '—' },
        { key: 'phone', label: t('columns.phone'), render: (row) => row.phone || '—' },
        { key: 'role', label: t('columns.role'), render: (row) => t(`role.${row.role}`, row.role) },
        {
            key: 'actions',
            label: t('columns.actions'),
            render: (row) => (
                <div className="user-list__actions">
                    <Button size="sm" variant="secondary" onClick={() => navigate(ROUTE_HELPERS.USER_VIEW(row.id))}>{tc('view')}</Button>
                    <Button size="sm" variant="secondary" onClick={() => navigate(ROUTE_HELPERS.USER_EDIT(row.id))}>{tc('edit')}</Button>
                    <Button
                        size="sm"
                        variant="danger"
                        loading={deleteLoading}
                        onClick={async () => {
                            const ok = await deleteUser(row.id)
                            if (ok) refetch()
                        }}
                    >
                        {tc('delete')}
                    </Button>
                </div>
            ),
        },
    ], [t, tc, navigate, deleteLoading, deleteUser, refetch])

    if (error && users.length === 0) {
        return (
            <div className="user-list__error">
                <p>{error}</p>
                <Button onClick={refetch}>{t('retry')}</Button>
            </div>
        )
    }

    return (
        <div>
            <div className="user-list__header">
                <h1 className="user-list__title">{t('title')}</h1>
                <Button onClick={() => navigate(APP_ROUTES.USERS.CREATE)}>{t('createNew')}</Button>
            </div>

            <Card>
                <div className="user-list__filters">
                    <Input
                        name="search"
                        placeholder={t('searchPlaceholder')}
                        value={search}
                        onChange={(event) => setSearch(event.target.value)}
                    />
                    <select
                        className="user-list__select"
                        value={role}
                        onChange={(event) => setRole(event.target.value as 'ALL' | UserRole)}
                    >
                        <option value="ALL">{t('filter.allRoles')}</option>
                        <option value="ADMIN">{t('role.ADMIN')}</option>
                        <option value="OWNER">{t('role.OWNER')}</option>
                        <option value="MANAGER">{t('role.MANAGER')}</option>
                        <option value="COLLECTOR">{t('role.COLLECTOR')}</option>
                        <option value="ACCOUNTANT">{t('role.ACCOUNTANT')}</option>
                        <option value="USER">{t('role.USER')}</option>
                    </select>
                    <Button variant="secondary" onClick={() => { setSearch(''); setRole('ALL') }}>{t('clearFilters')}</Button>
                </div>

                <Table columns={columns} data={users} loading={loading} emptyMessage={t('empty')} />

                {totalElements > 0 && (
                    <div className="user-list__pagination">
                        <span>{t('pagination.showing', { from, to, total: totalElements })}</span>
                        <div className="user-list__pagination-controls">
                            <label htmlFor="user-page-size">{t('pagination.pageSize')}</label>
                            <select id="user-page-size" className="user-list__select" value={size} onChange={(event) => setSize(Number(event.target.value))}>
                                {PAGE_SIZE_OPTIONS.map((option) => <option key={option} value={option}>{option}</option>)}
                            </select>
                            <Button size="sm" variant="secondary" onClick={prevPage} disabled={page === 0}>{t('pagination.previous')}</Button>
                            <Button size="sm" variant="secondary" onClick={nextPage} disabled={page + 1 >= totalPages}>{t('pagination.next')}</Button>
                        </div>
                    </div>
                )}
            </Card>
        </div>
    )
}

