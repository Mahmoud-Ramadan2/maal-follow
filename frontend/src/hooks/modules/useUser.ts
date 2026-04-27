import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'

import { userApi } from '@services/api/modules/user.api'
import type { AppUser } from '@/types/modules/user.types'

interface UseUserReturn {
    user: AppUser | null
    loading: boolean
    error: string | null
    refetch: () => void
}

export function useUser(id: number): UseUserReturn {
    const { t } = useTranslation('user')
    const [user, setUser] = useState<AppUser | null>(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetchUser = useCallback(async () => {
        if (!id) return
        setLoading(true)
        setError(null)
        try {
            setUser(await userApi.getById(id))
        } catch (err) {
            const message = err instanceof Error ? err.message : t('fetchOneError')
            setError(message)
            toast.error(t('fetchOneError'))
        } finally {
            setLoading(false)
        }
    }, [id, t])

    useEffect(() => {
        fetchUser()
    }, [fetchUser])

    return { user, loading, error, refetch: fetchUser }
}

