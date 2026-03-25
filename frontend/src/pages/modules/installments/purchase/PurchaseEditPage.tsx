import type { ReactNode } from 'react'
import { useParams } from 'react-router-dom'
import PurchaseFormPage from './PurchaseFormPage'

/**
 * Edit Purchase page — reads `:id` from the URL and renders
 * the shared form in edit mode.
 */
export default function PurchaseEditPage(): ReactNode {
    const { id } = useParams<{ id: string }>()

    return <PurchaseFormPage purchaseId={Number(id)} />
}
