import { useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { partnerApi } from '@services/api/modules/partner.api'

export function usePartnerDelete() {
    const { t } = useTranslation('partner')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const deletePartner = useCallback(async (id: number): Promise<boolean> => {
        const confirmed = window.confirm(t('deleteConfirm'))
        if (!confirmed) return false
        setLoading(true); setError(null)
        try {
            await partnerApi.delete(id)
            toast.success(t('deleted'))
            return true
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('deleteError')
            setError(msg); toast.error(t('deleteError'))
            return false
        } finally { setLoading(false) }
    }, [t])

    return { deletePartner, loading, error }
}

