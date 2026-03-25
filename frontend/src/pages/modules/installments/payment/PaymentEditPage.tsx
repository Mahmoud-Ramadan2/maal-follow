import type { ReactNode } from 'react'
import PaymentDetailsPage from './PaymentDetailsPage'

/** Payments are not editable — this renders the details page with cancel/refund actions. */
export default function PaymentEditPage(): ReactNode {
    return <PaymentDetailsPage />
}
