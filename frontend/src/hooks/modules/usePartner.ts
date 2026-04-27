import { useState, useEffect, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { toast } from 'react-toastify'
import { partnerApi } from '@services/api/modules/partner.api'
import { partnerInvestmentApi } from '@services/api/modules/partnerInvestment.api'
import { partnerWithdrawalApi } from '@services/api/modules/partnerWithdrawal.api'
import { partnerCommissionApi } from '@services/api/modules/partnerCommission.api'
import { partnerCustomerAcquisitionApi } from '@services/api/modules/partnerCustomerAcquisition.api'
import { partnerMonthlyProfitApi } from '@services/api/modules/partnerMonthlyProfit.api'
import type {
    Partner,
    PartnerCommission,
    PartnerCustomerAcquisition,
    PartnerInvestment,
    PartnerMonthlyProfit,
    PartnerWithdrawal,
} from '@/types/modules/partner.types'

interface UsePartnerReturn {
    partner: Partner | null
    investments: PartnerInvestment[]
    withdrawals: PartnerWithdrawal[]
    commissions: PartnerCommission[]
    acquisitions: PartnerCustomerAcquisition[]
    monthlyProfits: PartnerMonthlyProfit[]
    loading: boolean
    error: string | null
    refetch: () => void
}

export function usePartner(id: number): UsePartnerReturn {
    const { t } = useTranslation('partner')
    const [partner, setPartner] = useState<Partner | null>(null)
    const [investments, setInvestments] = useState<PartnerInvestment[]>([])
    const [withdrawals, setWithdrawals] = useState<PartnerWithdrawal[]>([])
    const [commissions, setCommissions] = useState<PartnerCommission[]>([])
    const [acquisitions, setAcquisitions] = useState<PartnerCustomerAcquisition[]>([])
    const [monthlyProfits, setMonthlyProfits] = useState<PartnerMonthlyProfit[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const fetch = useCallback(async () => {
        if (!id) return
        setLoading(true); setError(null)
        try {
            const p = await partnerApi.getById(id)
            setPartner(p)
            const [inv, wdl, comm, acq, profits] = await Promise.all([
                partnerInvestmentApi.getByPartner(id).catch(() => [] as PartnerInvestment[]),
                partnerWithdrawalApi.getByPartner(id).catch(() => [] as PartnerWithdrawal[]),
                partnerCommissionApi.getByPartner(id, 0, 10).then((res) => res.content).catch(() => [] as PartnerCommission[]),
                partnerCustomerAcquisitionApi.getByPartner(id).catch(() => [] as PartnerCustomerAcquisition[]),
                partnerMonthlyProfitApi.getByPartner(id).catch(() => [] as PartnerMonthlyProfit[]),
            ])
            setInvestments(inv)
            setWithdrawals(wdl)
            setCommissions(comm)
            setAcquisitions(acq)
            setMonthlyProfits(profits)
        } catch (err) {
            const msg = err instanceof Error ? err.message : t('fetchOneError')
            setError(msg); toast.error(t('fetchOneError'))
        } finally { setLoading(false) }
    }, [id, t])

    useEffect(() => { fetch() }, [fetch])
    return { partner, investments, withdrawals, commissions, acquisitions, monthlyProfits, loading, error, refetch: fetch }
}

