import type { ReactNode } from 'react'
import { useParams } from 'react-router-dom'
import ContractFormPage from './ContractFormPage'

export default function ContractEditPage(): ReactNode {
    const { id } = useParams<{ id: string }>()
    return <ContractFormPage contractId={Number(id)} />
}
