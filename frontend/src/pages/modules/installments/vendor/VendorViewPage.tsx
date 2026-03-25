import type { ReactNode } from 'react'
import VendorDetailsPage from './VendorDetailsPage'

/**
 * Vendor view route — renders the details page.
 * The `:id` route param is read inside VendorDetailsPage.
 */
export default function VendorViewPage(): ReactNode {
    return <VendorDetailsPage />
}

