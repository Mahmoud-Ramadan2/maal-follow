import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'

import { userApi } from '@services/api/modules/user.api'
import type { AppUser, UserFilters } from '@/types/modules/user.types'

interface UseUsersReturn {
    users: AppUser[]
    loading: boolean
    error: string | null
    totalPages: number
    totalElements: number
    refetch: () => void
}

export function useUsers(filters?: UserFilters): UseUsersReturn {
    const { t } = useTranslation('user')
    const [users, setUsers] = useState<AppUser[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [totalPages, setTotalPages] = useState(0)
    const [totalElements, setTotalElements] = useState(0)

    const fetchUsers = useCallback(async () => {
        setLoading(true)
        setError(null)
        try {
            const page = await userApi.getAll(filters)
            setUsers(page.content)
            setTotalPages(page.totalPages)
            setTotalElements(page.totalElements)
        } catch (err) {
            const message = err instanceof Error ? err.message : t('fetchError')
            setError(message)
            toast.error(t('fetchError'))
        } finally {
            setLoading(false)
        }
    }, [filters, t])

    useEffect(() => {
        fetchUsers()
    }, [fetchUsers])

    return { users, loading, error, totalPages, totalElements, refetch: fetchUsers }
}

