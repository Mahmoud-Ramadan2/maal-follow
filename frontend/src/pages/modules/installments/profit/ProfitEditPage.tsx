import type { ReactNode } from 'react'
import { useParams } from 'react-router-dom'
import ProfitFormPage from './ProfitFormPage'

export default function ProfitEditPage(): ReactNode {
    const { id } = useParams<{ id: string }>()
    return <ProfitFormPage distributionId={Number(id)} />
}

