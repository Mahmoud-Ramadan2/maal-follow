import type { ReactNode } from 'react'
import PurchaseDetailsPage from './PurchaseDetailsPage'

/**
 * Purchase view route — renders the details page.
 * The `:id` route param is read inside PurchaseDetailsPage.
 */
export default function PurchaseViewPage(): ReactNode {
    return <PurchaseDetailsPage />
}

