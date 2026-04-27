import { useCallback, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'

import { userApi } from '@services/api/modules/user.api'
import type { AppUser, UserRequest } from '@/types/modules/user.types'

interface UseUserCreateReturn {
    createUser: (data: UserRequest) => Promise<AppUser | null>
    loading: boolean
    error: string | null
}

export function useUserCreate(): UseUserCreateReturn {
    const { t } = useTranslation('user')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const createUser = useCallback(async (data: UserRequest): Promise<AppUser | null> => {
        setLoading(true)
        setError(null)
        try {
            const created = await userApi.create(data)
            toast.success(t('created'))
            return created
        } catch (err) {
            const message = err instanceof Error ? err.message : t('createError')
            setError(message)
            toast.error(t('createError'))
            return null
        } finally {
            setLoading(false)
        }
    }, [t])

    return { createUser, loading, error }
}

