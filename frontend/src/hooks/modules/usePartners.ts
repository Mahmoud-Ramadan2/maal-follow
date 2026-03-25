import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { partnerApi } from '@services/api/modules/partner.api'
import type { Partner, PartnerStatus } from '@/types/modules/partner.types'
import {IS_DEV} from "@/config";

interface UsePartnersReturn {
    partners: Partner[]
    loading: boolean
    error: string | null
    refetch: () => void
}

export function usePartners(status?: PartnerStatus): UsePartnersReturn {
    const { t } = useTranslation('partner')
    const [partners, setPartners] = useState<Partner[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetch = useCallback(async () => {
        setLoading(true); setError(null)
        try {
            setPartners(await partnerApi.getAll(status))
        } catch (err) {
            // const msg = err instanceof Error ? err.message : t('fetchError')
            const msg = err instanceof Error ? IS_DEV ? err.message : t('fetchError') : t('fetchError')

            setError(msg); toast.error(t('fetchError'))
        } finally { setLoading(false) }
    }, [status, t])

    useEffect(() => { fetch() }, [fetch])
    return { partners, loading, error, refetch: fetch }
}

