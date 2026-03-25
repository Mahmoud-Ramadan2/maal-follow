// src/hooks/common/useDeleteConfirmation.ts
import { useState } from 'react'
import { toast } from 'react-toastify'

interface UseDeleteConfirmationReturn {
    isModalOpen: boolean
    itemId: number | null
    confirmDelete: (id: number) => void
    handleDelete: (deleteFn: (id: number)
        => Promise<any>) => Promise<void>
    handleCloseModal: () => void
}

export function useDeleteConfirmation(
    successMessage?: string,
    onDeleted?: () => void
): UseDeleteConfirmationReturn {
    const [isModalOpen, setIsModalOpen] = useState(false)
    const [itemId, setItemId] = useState<number | null>(null)

    const confirmDelete = (id: number) => {
        console.log('confirmDelete called with id:', id)
        setItemId(id)
        setIsModalOpen(true)
    }

    const handleDelete = async (deleteFn: (id: number) => Promise<any>) => {
        console.log('itemId in handleDelete:', itemId)
        if (!itemId) return
        await deleteFn(itemId)
        if (successMessage) toast.success(successMessage)
        onDeleted?.()
        setIsModalOpen(false)
        setItemId(null)
    }

    const handleCloseModal = () => {
        setIsModalOpen(false)
        setItemId(null)
    }

    return {
        isModalOpen,
        itemId,
        confirmDelete,
        handleDelete,
        handleCloseModal,
    }
}

