import type { ReactNode } from 'react'
import { useParams } from 'react-router-dom'
import ScheduleFormPage from './ScheduleFormPage'

export default function ScheduleEditPage(): ReactNode {
    const { id } = useParams<{ id: string }>()
    return <ScheduleFormPage scheduleId={Number(id)} />
}

