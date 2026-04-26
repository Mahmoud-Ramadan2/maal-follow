import { useEffect, useMemo, useState } from 'react'
import type { DragEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import {
    Box,
    Button,
    Card,
    Chip,
    CircularProgress,
    IconButton,
    Stack,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    TextField,
    Typography,
} from '@mui/material'
import { Add as AddIcon, Delete as DeleteIcon, DragIndicator as DragIndicatorIcon } from '@mui/icons-material'
import PageLoader from '@components/common/PageLoader'
import { useCollectionRoute, useCollectionRouteItemsManage, useCollectionRouteUpdate } from '@hooks/modules'
import { APP_ROUTES } from '@/router/routes.config'
import { collectionApi } from '@services/api/modules/collection.api'
import type { CustomerUnpaidSummary } from '@/types/modules/collection.types'
import './CollectionRouteViewPage.css'

export default function CollectionRouteViewPage() {
    const { t } = useTranslation(['collection', 'common'])
    const navigate = useNavigate()
    const { id } = useParams<{ id: string }>()
    const routeId = Number(id)

    const { route, items, loading, refetch } = useCollectionRoute(routeId)
    const { deactivate, loading: deactivateLoading } = useCollectionRouteUpdate()
    const {
        addCustomer,
        removeItem,
        reorderItems,
        loading: itemActionLoading,
    } = useCollectionRouteItemsManage()

    const [search, setSearch] = useState('')
    const [candidateLoading, setCandidateLoading] = useState(false)
    const [candidates, setCandidates] = useState<CustomerUnpaidSummary[]>([])
    const [editableItems, setEditableItems] = useState(items)
    const [reorderMode, setReorderMode] = useState(false)
    const [draggedItemId, setDraggedItemId] = useState<number | null>(null)

    const title = useMemo(() => route?.name ?? t('routeName'), [route?.name, t])

    const loadCandidates = async () => {
        if (!route?.isActive) return
        setCandidateLoading(true)
        try {
            const result = await collectionApi.searchUncollected({ search, page: 0, size: 20 })
            setCandidates(result.content || [])
        } finally {
            setCandidateLoading(false)
        }
    }

    const handleAddCustomer = async (customerId: number) => {
        await addCustomer(routeId, customerId)
        await Promise.all([refetch(), loadCandidates()])
    }

    const handleRemoveItem = async (itemId: number) => {
        await removeItem(itemId)
        await Promise.all([refetch(), loadCandidates()])
    }

    const onDragStart = (itemId: number) => {
        if (!reorderMode || itemActionLoading) return
        setDraggedItemId(itemId)
    }

    const onDragOver = (event: DragEvent<HTMLTableRowElement>) => {
        if (!reorderMode || !draggedItemId) return
        event.preventDefault()
    }

    const onDrop = (targetItemId: number) => {
        if (!reorderMode || !draggedItemId || draggedItemId === targetItemId) return

        setEditableItems((prev) => {
            const next = [...prev]
            const fromIndex = next.findIndex((x) => x.id === draggedItemId)
            const toIndex = next.findIndex((x) => x.id === targetItemId)
            if (fromIndex < 0 || toIndex < 0) return prev

            const [moved] = next.splice(fromIndex, 1)
            next.splice(toIndex, 0, moved)
            return next
        })
        setDraggedItemId(null)
    }

    const cancelReorder = () => {
        setEditableItems(items)
        setReorderMode(false)
        setDraggedItemId(null)
    }

    const saveReorder = async () => {
        if (!route) return
        await reorderItems(route.id, editableItems.map((item) => item.id))
        await refetch()
        setReorderMode(false)
    }

    useEffect(() => {
        setEditableItems(items)
    }, [items])

    useEffect(() => {
        if (route?.isActive) {
            void loadCandidates()
        }
        // only run when route identity changes
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [route?.id, route?.isActive])

    if (loading) return <PageLoader />
    if (!route) {
        return (
            <Box p={3}>
                <Typography>{t('noRoutesFound')}</Typography>
                <Button onClick={() => navigate(APP_ROUTES.COLLECTION_ROUTES.LIST)}>
                    {t('back', { ns: 'common' })}
                </Button>
            </Box>
        )
    }

    return (
        <Box className="collection-route-view">
            <Stack direction="row" justifyContent="space-between" mb={2}>
                <Box>
                    <Typography variant="h5">{title}</Typography>
                    <Typography color="text.secondary">{route.description || '-'}</Typography>
                </Box>
                <Stack direction="row" spacing={1}>
                    <Button onClick={() => navigate(APP_ROUTES.COLLECTION_ROUTES.LIST)}>
                        {t('back', { ns: 'common' })}
                    </Button>
                    <Button
                        variant="contained"
                        color="error"
                        onClick={async () => {
                            await deactivate(route.id)
                            await refetch()
                        }}
                        disabled={deactivateLoading || !route.isActive}
                    >
                        {t('deactivateRoute')}
                    </Button>
                </Stack>
            </Stack>

            <Card className="collection-route-view__summary">
                <Stack direction="row" spacing={1} alignItems="center">
                    <Chip label={route.routeType} color="info" size="small" />
                    <Chip
                        label={route.isActive ? t('active') : t('cancelled')}
                        color={route.isActive ? 'success' : 'error'}
                        size="small"
                    />
                    <Typography variant="body2">
                        {t('routeItems')}: {items.length}
                    </Typography>
                </Stack>
            </Card>

            <Card className="collection-route-view__card">
                <Stack
                    direction="row"
                    alignItems="center"
                    justifyContent="space-between"
                    className="collection-route-view__card-header"
                >
                    <Typography variant="h6">{t('routeItems')}</Typography>
                    <Stack direction="row" spacing={1}>
                        {!reorderMode ? (
                            <Button
                                variant="outlined"
                                onClick={() => setReorderMode(true)}
                                disabled={itemActionLoading || !route.isActive || editableItems.length < 2}
                            >
                                {t('reorderItems')}
                            </Button>
                        ) : (
                            <>
                                <Button variant="outlined" onClick={cancelReorder} disabled={itemActionLoading}>
                                    {t('cancel', { ns: 'common' })}
                                </Button>
                                <Button variant="contained" onClick={saveReorder} disabled={itemActionLoading}>
                                    {t('save', { ns: 'common' })}
                                </Button>
                            </>
                        )}
                    </Stack>
                </Stack>

                {reorderMode && (
                    <Typography variant="body2" className="collection-route-view__hint">
                        {t('draggableItem')}
                    </Typography>
                )}

                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>{t('sequenceOrder')}</TableCell>
                            <TableCell>{t('customer')}</TableCell>
                            <TableCell>{t('phone')}</TableCell>
                            <TableCell>{t('address')}</TableCell>
                            <TableCell>{t('notes')}</TableCell>
                            <TableCell>{t('actions')}</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {editableItems.map((item, index) => (
                            <TableRow
                                key={item.id}
                                draggable={reorderMode && !itemActionLoading}
                                onDragStart={() => onDragStart(item.id)}
                                onDragOver={onDragOver}
                                onDrop={() => onDrop(item.id)}
                                className={reorderMode ? 'collection-route-view__draggable-row' : ''}
                            >
                                <TableCell>
                                    <Stack direction="row" alignItems="center" spacing={1}>
                                        {reorderMode && <DragIndicatorIcon fontSize="small" color="action" />}
                                        <span>{index + 1}</span>
                                    </Stack>
                                </TableCell>
                                <TableCell>{item.customer?.name || '-'}</TableCell>
                                <TableCell>{item.customer?.phone || '-'}</TableCell>
                                <TableCell>{item.customer?.address || '-'}</TableCell>
                                <TableCell>{item.notes || '-'}</TableCell>
                                <TableCell>
                                    <IconButton
                                        color="error"
                                        onClick={() => handleRemoveItem(item.id)}
                                        disabled={itemActionLoading || !route.isActive || reorderMode}
                                        title={t('removeCustomer')}
                                    >
                                        <DeleteIcon fontSize="small" />
                                    </IconButton>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </Card>

            <Card className="collection-route-view__card">
                <Typography variant="h6" mb={2}>{t('addCustomer')}</Typography>
                <Stack direction="row" spacing={1} className="collection-route-view__search-row">
                    <TextField
                        label={t('searchCustomers')}
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        fullWidth
                        size="small"
                    />
                    <Button
                        variant="outlined"
                        onClick={loadCandidates}
                        disabled={candidateLoading || !route.isActive}
                    >
                        {candidateLoading ? <CircularProgress size={18} /> : t('search', { ns: 'common' })}
                    </Button>
                </Stack>

                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>{t('customer')}</TableCell>
                            <TableCell>{t('phone')}</TableCell>
                            <TableCell>{t('address')}</TableCell>
                            <TableCell>{t('actions')}</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {candidates.map((candidate) => (
                            <TableRow key={candidate.id}>
                                <TableCell>{candidate.name}</TableCell>
                                <TableCell>{candidate.phone || '-'}</TableCell>
                                <TableCell>{candidate.address || '-'}</TableCell>
                                <TableCell>
                                    <IconButton
                                        color="primary"
                                        onClick={() => handleAddCustomer(candidate.id)}
                                        disabled={itemActionLoading || !route.isActive}
                                        title={t('addCustomer')}
                                    >
                                        <AddIcon fontSize="small" />
                                    </IconButton>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </Card>
        </Box>
    )
}
