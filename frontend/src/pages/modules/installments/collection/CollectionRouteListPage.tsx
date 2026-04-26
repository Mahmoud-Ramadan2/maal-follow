/**
 * CollectionRouteListPage
 *
 * Displays a paginated list of collection routes.
 * Allows users to browse, filter, search, and manage routes.
 *
 * Features:
 * - Paginated table with sorting and filtering
 * - Filter by route type and status
 * - Search by route name
 * - Create new route button
 * - Click row to view/edit route details
 * - Deactivate route action
 */

import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import {
    Box,
    Button,
    Card,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TablePagination,
    TableRow,
    TextField,
    Select,
    MenuItem,
    FormControl,
    InputLabel,
    Stack,
    Chip,
    IconButton,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogContentText,
    DialogActions,
} from '@mui/material'
import { Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material'
import PageLoader from '@components/common/PageLoader'
import { useCollectionRoutes, useCollectionRouteUpdate } from '@hooks/modules'
import { APP_ROUTES } from '@/router/routes.config'
import type { CollectionRoute, RouteType, RouteStatus } from '@/types/modules/collection.types'
import { RouteType as RouteTypeEnum, RouteStatus as RouteStatusEnum } from '@/types/modules/collection.types'
import './CollectionRouteListPage.css'

const ROUTE_TYPE_OPTIONS: RouteType[] = [
    RouteTypeEnum.BY_ADDRESS,
    RouteTypeEnum.BY_DATE,
    RouteTypeEnum.BY_COLLECTOR,
    RouteTypeEnum.CUSTOM,
]

const ROUTE_STATUS_OPTIONS: RouteStatus[] = [
    RouteStatusEnum.DRAFT,
    RouteStatusEnum.ACTIVE,
    RouteStatusEnum.IN_PROGRESS,
    RouteStatusEnum.COMPLETED,
    RouteStatusEnum.CANCELLED,
]

interface FilterState {
    searchTerm: string
    routeType: RouteType | ''
    routeStatus: RouteStatus | ''
}

/**
 * Main page component for browsing collection routes.
 */
export function CollectionRouteListPage() {
    const { t } = useTranslation(['collection', 'common'])
    const navigate = useNavigate()

    // Data fetching hooks
    const { routes, page, setPage, size, setSize, loading, refetch } =
        useCollectionRoutes({ initialPage: 0, initialSize: 10 })
    const { deactivate: deactivateRoute, loading: deactivateLoading } =
        useCollectionRouteUpdate()

    // Local state
    const [filters, setFilters] = useState<FilterState>({
        searchTerm: '',
        routeType: '',
        routeStatus: '',
    })
    const [deactivateConfirm, setDeactivateConfirm] = useState<{
        open: boolean
        routeId: number | null
        routeName: string
    }>({ open: false, routeId: null, routeName: '' })

    // Handle navigation to create page
    const handleCreateRoute = () => {
        navigate(APP_ROUTES.COLLECTION_ROUTES.CREATE)
    }

    // Handle row click to view route details
    const handleViewRoute = (routeId: number) => {
        navigate(APP_ROUTES.COLLECTION_ROUTES.VIEW_PATTERN.replace(':id', routeId.toString()))
    }

    // Handle deactivate confirmation dialog
    const handleDeactivateClick = (route: CollectionRoute) => {
        setDeactivateConfirm({
            open: true,
            routeId: route.id,
            routeName: route.name,
        })
    }

    // Handle confirm deactivation
    const handleConfirmDeactivate = async () => {
        if (deactivateConfirm.routeId) {
            try {
                await deactivateRoute(deactivateConfirm.routeId)
                setDeactivateConfirm({ open: false, routeId: null, routeName: '' })
                refetch()
            } catch (err) {
                console.error('Error deactivating route:', err)
            }
        }
    }

    // Handle filter changes
    const handleFilterChange = (key: keyof FilterState, value: any) => {
        setFilters(prev => ({ ...prev, [key]: value }))
    }

    // Handle clear filters
    const handleClearFilters = () => {
        setFilters({
            searchTerm: '',
            routeType: '',
            routeStatus: '',
        })
    }

    // Filter routes locally based on filter state
    const filteredRoutes = routes.filter(route => {
        const matchesSearch = filters.searchTerm === '' ||
            route.name.toLowerCase().includes(filters.searchTerm.toLowerCase())
        const matchesType = filters.routeType === '' || route.routeType === filters.routeType
        const matchesStatus = filters.routeStatus === '' || route.routeStatus === filters.routeStatus
        return matchesSearch && matchesType && matchesStatus
    })

    // Get route type display label
    const getRouteTypeLabel = (type: RouteType): string => {
        const labels: Record<RouteType, string> = {
            BY_ADDRESS: t('byAddress'),
            BY_DATE: t('byDate'),
            BY_COLLECTOR: t('byCollector'),
            CUSTOM: t('custom'),
        }
        return labels[type]
    }

    // Get route status display label
    const getRouteStatusLabel = (status: RouteStatus): string => {
        const labels: Record<RouteStatus, string> = {
            DRAFT: t('draft'),
            ACTIVE: t('active'),
            IN_PROGRESS: t('inProgress'),
            COMPLETED: t('completed'),
            CANCELLED: t('cancelled'),
        }
        return labels[status]
    }

    // Get status chip color
    const getStatusColor = (status: RouteStatus): 'default' | 'primary' | 'secondary' | 'error' | 'info' | 'success' | 'warning' => {
        const colors: Record<RouteStatus, any> = {
            DRAFT: 'default',
            ACTIVE: 'success',
            IN_PROGRESS: 'info',
            COMPLETED: 'primary',
            CANCELLED: 'error',
        }
        return colors[status]
    }

    if (loading) {
        return <PageLoader />
    }

    return (
        <Box className="collection-list-page">
            {/* Page Header */}
            <Box className="collection-list__header">
                <h1 className="collection-list__title">{t('moduleTitle')}</h1>
                <p>{t('description')}</p>
            </Box>

            {/* Filter Section */}
            <Card className="collection-list__filters-card">
                <Stack spacing={2}>
                    {/* Search and Filters Row */}
                    <Stack direction="row" spacing={2} className="collection-list__filters-row">
                        <TextField
                            label={t('searchRoutes')}
                            placeholder={t('searchRoutes')}
                            value={filters.searchTerm}
                            onChange={e => handleFilterChange('searchTerm', e.target.value)}
                            fullWidth
                            size="small"
                            variant="outlined"
                        />
                        <FormControl size="small" style={{ minWidth: 200 }}>
                            <InputLabel>{t('filterByType')}</InputLabel>
                            <Select
                                value={filters.routeType}
                                label={t('filterByType')}
                                onChange={e => handleFilterChange('routeType', e.target.value)}
                            >
                                <MenuItem value="">{t('all')}</MenuItem>
                                {ROUTE_TYPE_OPTIONS.map(type => (
                                    <MenuItem key={type} value={type}>
                                        {getRouteTypeLabel(type)}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                        <FormControl size="small" style={{ minWidth: 200 }}>
                            <InputLabel>{t('filterByStatus')}</InputLabel>
                            <Select
                                value={filters.routeStatus}
                                label={t('filterByStatus')}
                                onChange={e => handleFilterChange('routeStatus', e.target.value)}
                            >
                                <MenuItem value="">{t('all')}</MenuItem>
                                {ROUTE_STATUS_OPTIONS.map(status => (
                                    <MenuItem key={status} value={status}>
                                        {getRouteStatusLabel(status)}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                        <Button
                            variant="outlined"
                            onClick={handleClearFilters}
                            size="small"
                        >
                            {t('clearFilters')}
                        </Button>
                    </Stack>

                    {/* Action Buttons Row */}
                    <Stack direction="row" spacing={2} justifyContent="flex-end">
                        <Button
                            variant="contained"
                            color="primary"
                            onClick={handleCreateRoute}
                        >
                            {t('createRoute')}
                        </Button>
                    </Stack>
                </Stack>
            </Card>

            {/* Routes Table */}
            {filteredRoutes.length > 0 ? (
                <TableContainer component={Paper} className="collection-list__table-container">
                    <Table className="collection-list__table">
                        <TableHead>
                            <TableRow className="collection-list__table-header">
                                <TableCell>{t('routeName')}</TableCell>
                                <TableCell>{t('routeType')}</TableCell>
                                <TableCell align="center">{t('routeItems')}</TableCell>
                                <TableCell>{t('totalOutstanding')}</TableCell>
                                <TableCell>{t('totalCollected')}</TableCell>
                                <TableCell>{t('collectionPercentage')}</TableCell>
                                <TableCell>{t('status')}</TableCell>
                                <TableCell align="right">{t('actions')}</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {filteredRoutes.map(route => (
                                <TableRow
                                    key={route.id}
                                    className="collection-list__table-row"
                                    hover
                                    style={{ cursor: 'pointer' }}
                                >
                                    <TableCell
                                        onClick={() => handleViewRoute(route.id)}
                                        className="collection-list__clickable"
                                    >
                                        <strong>{route.name}</strong>
                                        {route.description && (
                                            <div className="collection-list__description">{route.description}</div>
                                        )}
                                    </TableCell>
                                    <TableCell onClick={() => handleViewRoute(route.id)}>
                                        {getRouteTypeLabel(route.routeType)}
                                    </TableCell>
                                    <TableCell align="center" onClick={() => handleViewRoute(route.id)}>
                                        {route.routeItems?.length || 0}
                                    </TableCell>
                                    <TableCell onClick={() => handleViewRoute(route.id)}>
                                        {new Intl.NumberFormat('en-US', {
                                            style: 'currency',
                                            currency: 'USD',
                                        }).format(route.totalOutstanding || 0)}
                                    </TableCell>
                                    <TableCell onClick={() => handleViewRoute(route.id)}>
                                        {new Intl.NumberFormat('en-US', {
                                            style: 'currency',
                                            currency: 'USD',
                                        }).format(route.totalCollected || 0)}
                                    </TableCell>
                                    <TableCell onClick={() => handleViewRoute(route.id)}>
                                        <Chip
                                            label={`${route.collectionPercentage?.toFixed(1) || 0}%`}
                                            size="small"
                                            color={route.collectionPercentage >= 80 ? 'success' : route.collectionPercentage >= 50 ? 'warning' : 'default'}
                                            variant="outlined"
                                        />
                                    </TableCell>
                                    <TableCell onClick={() => handleViewRoute(route.id)}>
                                        <Chip
                                            label={getRouteStatusLabel(route.routeStatus)}
                                            size="small"
                                            color={getStatusColor(route.routeStatus)}
                                            variant="filled"
                                        />
                                    </TableCell>
                                    <TableCell align="right">
                                        <IconButton
                                            size="small"
                                            onClick={() => handleViewRoute(route.id)}
                                            title={t('viewDetails')}
                                        >
                                            <EditIcon fontSize="small" />
                                        </IconButton>
                                        <IconButton
                                            size="small"
                                            color="error"
                                            onClick={() => handleDeactivateClick(route)}
                                            disabled={!route.isActive}
                                            title={t('deactivateRoute')}
                                        >
                                            <DeleteIcon fontSize="small" />
                                        </IconButton>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>

                    {/* Pagination */}
                    <TablePagination
                        rowsPerPageOptions={[5, 10, 25, 50]}
                        component="div"
                        count={filteredRoutes.length}
                        rowsPerPage={size}
                        page={page}
                        onPageChange={(_, newPage) => setPage(newPage)}
                        onRowsPerPageChange={(e) => {
                            setSize(parseInt(e.target.value, 10))
                            setPage(0)
                        }}
                    />
                </TableContainer>
            ) : (
                <Card className="collection-list__empty-state">
                    <Box className="collection-list__empty-state-content">
                        <h2>{t('noRoutesFound')}</h2>
                        <p>{t('createFirstRoute')}</p>
                        <Button
                            variant="contained"
                            color="primary"
                            onClick={handleCreateRoute}
                            className="collection-list__empty-state-button"
                        >
                            {t('createNewRoute')}
                        </Button>
                    </Box>
                </Card>
            )}

            {/* Deactivate Confirmation Dialog */}
            <Dialog
                open={deactivateConfirm.open}
                onClose={() => setDeactivateConfirm({ open: false, routeId: null, routeName: '' })}
            >
                <DialogTitle>{t('deactivateRoute')}</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        {t('confirmDeactivate')}
                        <br />
                        <strong>{deactivateConfirm.routeName}</strong>
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button
                        onClick={() => setDeactivateConfirm({ open: false, routeId: null, routeName: '' })}
                    >
                        {t('cancel')}
                    </Button>
                    <Button
                        onClick={handleConfirmDeactivate}
                        color="error"
                        variant="contained"
                        disabled={deactivateLoading}
                    >
                        {deactivateLoading ? t('loading') : t('deactivateRoute')}
                    </Button>
                </DialogActions>
            </Dialog>
        </Box>
    )
}

export default CollectionRouteListPage

