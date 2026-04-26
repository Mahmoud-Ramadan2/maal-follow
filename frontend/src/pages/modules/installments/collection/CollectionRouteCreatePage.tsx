import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import {
    Box,
    Button,
    Card,
    FormControl,
    InputLabel,
    MenuItem,
    Select,
    Stack,
    TextField,
    Typography,
} from '@mui/material'
import { useCollectionRouteCreate } from '@hooks/modules'
import { APP_ROUTES, ROUTE_HELPERS } from '@/router/routes.config'
import { RouteType } from '@/types/modules/collection.types'
import './CollectionRouteCreatePage.css'

export default function CollectionRouteCreatePage() {
    const { t } = useTranslation(['collection', 'common'])
    const navigate = useNavigate()
    const { create, loading } = useCollectionRouteCreate()

    const [name, setName] = useState('')
    const [description, setDescription] = useState('')
    const [routeType, setRouteType] = useState(RouteType.BY_ADDRESS)

    const handleSubmit = async () => {
        if (!name.trim()) return

        const route = await create({
            name: name.trim(),
            description: description.trim() || undefined,
            routeType,
        })

        navigate(ROUTE_HELPERS.COLLECTION_ROUTE_VIEW(route.id))
    }

    return (
        <Box className="collection-route-create">
            <Card className="collection-route-create__card">
                <Stack spacing={2}>
                    <Typography variant="h5">{t('createRoute')}</Typography>
                    <TextField
                        label={t('routeName')}
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        fullWidth
                        required
                    />
                    <TextField
                        label={t('routeDescription')}
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        fullWidth
                        multiline
                        minRows={3}
                    />
                    <FormControl fullWidth>
                        <InputLabel>{t('routeType')}</InputLabel>
                        <Select
                            value={routeType}
                            label={t('routeType')}
                            onChange={(e) => setRouteType(e.target.value as typeof routeType)}
                        >
                            <MenuItem value={RouteType.BY_ADDRESS}>{t('byAddress')}</MenuItem>
                            <MenuItem value={RouteType.BY_DATE}>{t('byDate')}</MenuItem>
                            <MenuItem value={RouteType.BY_COLLECTOR}>{t('byCollector')}</MenuItem>
                            <MenuItem value={RouteType.CUSTOM}>{t('custom')}</MenuItem>
                        </Select>
                    </FormControl>
                    <Stack direction="row" spacing={1} justifyContent="flex-end" className="collection-route-create__actions">
                        <Button onClick={() => navigate(APP_ROUTES.COLLECTION_ROUTES.LIST)}>
                            {t('cancel', { ns: 'common' })}
                        </Button>
                        <Button
                            variant="contained"
                            onClick={handleSubmit}
                            disabled={loading || !name.trim()}
                        >
                            {loading ? t('loading', { ns: 'common' }) : t('createRoute')}
                        </Button>
                    </Stack>
                </Stack>
            </Card>
        </Box>
    )
}
