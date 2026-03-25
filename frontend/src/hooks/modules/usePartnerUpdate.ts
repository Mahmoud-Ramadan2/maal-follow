import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { partnerApi } from '@services/api/modules/partner.api'
import type { Partner, PartnerRequest } from '@/types/modules/partner.types'

export function usePartnerUpdate() {
    const { t } = useTranslation('partner')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const updatePartner = useCallback(async (id: number, data: PartnerRequest): Promise<Partner | null> => {
        setLoading(true); setError(null)
        try {
            const updated = await partnerApi.update(id, data)
            toast.success(t('updated'))
            return updated
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('updateError')
            setError(msg); toast.error(t('updateError'))
            return null
        } finally { setLoading(false) }
    }, [t])

    return { updatePartner, loading, error }
}

