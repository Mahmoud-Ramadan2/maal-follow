import { useCallback, useEffect, useState } from 'react'

import { userApi } from '@services/api/modules/user.api'
import type { AppUser, UserRole } from '@/types/modules/user.types'

interface UseCollectorsReturn {
    collectors: AppUser[]
    loading: boolean
    error: string | null
    refetch: () => void
}

export function useCollectors(roles: UserRole[] = ['COLLECTOR']): UseCollectorsReturn {
    const [collectors, setCollectors] = useState<AppUser[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const rolesKey = roles.join('|')

    const fetchCollectors = useCallback(async () => {
        setLoading(true)
        setError(null)
        try {
            const normalizedRoles = rolesKey ? (rolesKey.split('|') as UserRole[]) : []
            const items = await userApi.getCollectors(normalizedRoles)
            setCollectors(items)
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to load collectors'
            setError(message)
            setCollectors([])
        } finally {
            setLoading(false)
        }
    }, [rolesKey])

    useEffect(() => {
        fetchCollectors()
    }, [fetchCollectors])

    return { collectors, loading, error, refetch: fetchCollectors }
}




