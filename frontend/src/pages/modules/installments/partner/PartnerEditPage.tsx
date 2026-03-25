import type { ReactNode } from 'react'
import { useParams } from 'react-router-dom'
import PartnerFormPage from './PartnerFormPage'

export default function PartnerEditPage(): ReactNode {
    const { id } = useParams<{ id: string }>()
    return <PartnerFormPage partnerId={Number(id)} />
}
