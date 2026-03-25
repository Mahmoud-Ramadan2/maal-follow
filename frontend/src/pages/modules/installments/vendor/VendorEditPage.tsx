import type { ReactNode } from 'react'
import { useParams } from 'react-router-dom'
import VendorFormPage from './VendorFormPage'

/**
 * Edit Vendor page — reads `:id` from the URL and renders
 * the shared form in edit mode.
 */
export default function VendorEditPage(): ReactNode {
    const { id } = useParams<{ id: string }>()

    return <VendorFormPage vendorId={Number(id)} />
}

