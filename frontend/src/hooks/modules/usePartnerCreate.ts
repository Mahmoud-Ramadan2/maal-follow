import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { partnerApi } from '@services/api/modules/partner.api'
import type { Partner, PartnerRequest } from '@/types/modules/partner.types'

export function usePartnerCreate() {
    const { t } = useTranslation('partner')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const createPartner = useCallback(async (data: PartnerRequest): Promise<Partner | null> => {
        setLoading(true); setError(null)
        try {
            const created = await partnerApi.create(data)
            toast.success(t('created'))
            return created
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('createError')
            setError(msg); toast.error(t('createError'))
            return null
        } finally { setLoading(false) }
    }, [t])

    return { createPartner, loading, error }
}

