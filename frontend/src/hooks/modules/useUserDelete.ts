import { useCallback, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'

import { userApi } from '@services/api/modules/user.api'

interface UseUserDeleteReturn {
    deleteUser: (id: number) => Promise<boolean>
    loading: boolean
    error: string | null
}

export function useUserDelete(): UseUserDeleteReturn {
    const { t } = useTranslation('user')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const deleteUser = useCallback(async (id: number): Promise<boolean> => {
        const confirmed = window.confirm(t('deleteConfirm'))
        if (!confirmed) return false

        setLoading(true)
        setError(null)
        try {
            await userApi.delete(id)
            toast.success(t('deleted'))
            return true
        } catch (err) {
            const message = err instanceof Error ? err.message : t('deleteError')
            setError(message)
            toast.error(t('deleteError'))
            return false
        } finally {
            setLoading(false)
        }
    }, [t])

    return { deleteUser, loading, error }
}

