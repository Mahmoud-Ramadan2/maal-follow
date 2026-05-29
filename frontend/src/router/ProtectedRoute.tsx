import type { ReactNode } from 'react'
import { Navigate, Outlet, useLocation } from 'react-router-dom'
import PageLoader from '@components/common/PageLoader'
import { useAuth } from '@contexts/useAuth'
import {
	clearRedirectSuppression,
	saveRedirectAfterLogin,
	shouldSuppressRedirectAfterLogin,
} from '@services/auth/session'
import { hasRequiredRoles } from '@utils/helpers/auth.helper'
import type { AuthRole } from '@/types/auth.types'
import { APP_ROUTES } from './routes.config'

interface ProtectedRouteProps {
	children?: ReactNode
	requiredRoles?: readonly AuthRole[]
}

export default function ProtectedRoute({ children, requiredRoles }: ProtectedRouteProps) {
	const { user, isAuthenticated, isLoading } = useAuth()
	const location = useLocation()

	if (isLoading) {
		return <PageLoader />
	}

	if (!isAuthenticated || !user) {
		if (!shouldSuppressRedirectAfterLogin()) {
			saveRedirectAfterLogin(`${location.pathname}${location.search}${location.hash}`)
		} else {
            clearRedirectSuppression()
		}
		return <Navigate to={APP_ROUTES.AUTH.LOGIN} replace />
	}

	if (!hasRequiredRoles(user, requiredRoles)) {
		return <Navigate to={APP_ROUTES.FORBIDDEN} replace />
	}

	return children ?? <Outlet />
}



