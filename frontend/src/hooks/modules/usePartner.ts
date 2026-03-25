import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { partnerApi } from '@services/api/modules/partner.api'
import { partnerInvestmentApi } from '@services/api/modules/partnerInvestment.api'
import { partnerWithdrawalApi } from '@services/api/modules/partnerWithdrawal.api'
import type { Partner, PartnerInvestment, PartnerWithdrawal } from '@/types/modules/partner.types'

interface UsePartnerReturn {
    partner: Partner | null
    investments: PartnerInvestment[]
    withdrawals: PartnerWithdrawal[]
    loading: boolean
    error: string | null
    refetch: () => void
}

export function usePartner(id: number): UsePartnerReturn {
    const { t } = useTranslation('partner')
    const [partner, setPartner] = useState<Partner | null>(null)
    const [investments, setInvestments] = useState<PartnerInvestment[]>([])
    const [withdrawals, setWithdrawals] = useState<PartnerWithdrawal[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetch = useCallback(async () => {
        if (!id) return
        setLoading(true); setError(null)
        try {
            const p = await partnerApi.getById(id)
            setPartner(p)
            const [inv, wdl] = await Promise.all([
                partnerInvestmentApi.getByPartner(id).catch(() => [] as PartnerInvestment[]),
                partnerWithdrawalApi.getByPartner(id).catch(() => [] as PartnerWithdrawal[]),
            ])
            setInvestments(inv)
            setWithdrawals(wdl)
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('fetchOneError')
            setError(msg); toast.error(t('fetchOneError'))
        } finally { setLoading(false) }
    }, [id, t])

    useEffect(() => { fetch() }, [fetch])
    return { partner, investments, withdrawals, loading, error, refetch: fetch }
}

