import type { ReactNode } from 'react'
import CustomerDetailsPage from './CustomerDetailsPage'

/**
 * Customer view route — renders the details page.
 * The `:id` route param is read inside CustomerDetailsPage.
 */
export default function CustomerViewPage(): ReactNode {
    return <CustomerDetailsPage />
}

