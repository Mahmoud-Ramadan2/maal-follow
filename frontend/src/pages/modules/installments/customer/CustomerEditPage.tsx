import type { ReactNode } from 'react'
import { useParams } from 'react-router-dom'
import CustomerFormPage from './CustomerFormPage'

/**
 * Edit Customer page — reads `:id` from the URL and renders
 * the shared form in edit mode.
 */
export default function CustomerEditPage(): ReactNode {
    const { id } = useParams<{ id: string }>()
    return <CustomerFormPage customerId={Number(id)} />
}
