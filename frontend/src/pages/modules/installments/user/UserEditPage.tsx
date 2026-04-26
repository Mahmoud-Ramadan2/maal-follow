import type { ReactNode } from 'react'
import { useParams } from 'react-router-dom'
import UserFormPage from './UserFormPage'

export default function UserEditPage(): ReactNode {
    const { id } = useParams<{ id: string }>()
    return <UserFormPage userId={Number(id)} />
}

