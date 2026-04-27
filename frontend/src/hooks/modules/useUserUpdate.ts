import { useCallback, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'

import { userApi } from '@services/api/modules/user.api'
import type { AppUser, UserRequest } from '@/types/modules/user.types'

interface UseUserUpdateReturn {
    updateUser: (id: number, data: UserRequest) => Promise<AppUser | null>
    loading: boolean
    error: string | null
}

export function useUserUpdate(): UseUserUpdateReturn {
    const { t } = useTranslation('user')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const updateUser = useCallback(async (id: number, data: UserRequest): Promise<AppUser | null> => {
        setLoading(true)
        setError(null)
        try {
            const updated = await userApi.update(id, data)
            toast.success(t('updated'))
            return updated
        } catch (err) {
            const message = err instanceof Error ? err.message : t('updateError')
            setError(message)
            toast.error(t('updateError'))
            return null
        } finally {
            setLoading(false)
        }
    }, [t])

    return { updateUser, loading, error }
}

