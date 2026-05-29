import React, { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { toast } from 'react-toastify'
import { authApi } from '@services/api/modules/auth.api'
import type { AuthUser } from '@/types/auth.types'
import { AuthContext, type AuthContextValue } from './auth.context'
import {
    clearRedirectAfterLogin,
    clearSessionStorage,
    clearRedirectSuppression,
    getStoredAuthUser,
    getStoredSessionTokens,
    setStoredSessionTokens,
    suppressRedirectAfterLogin,
} from '@services/auth/session'

// ===== TYPES =====
type User = AuthUser

// ===== PROVIDER COMPONENT =====
export const AuthProvider:
    React.FC<{ children: ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null)
    const [isLoading, setIsLoading] = useState(true)

    // Check if a user is already logged in (on app load)
    useEffect(() => {
        checkAuth()
    }, [])

    // Check authentication status
    const checkAuth = async () => {
        const { accessToken, refreshToken } = getStoredSessionTokens()
        const storedUser = getStoredAuthUser<AuthUser>()

        if (!accessToken && !refreshToken) {
            setUser(null)
            setIsLoading(false)
            return
        }

        try {
            if (refreshToken) {
                const refreshedSession = await authApi.refresh(refreshToken)
                setStoredSessionTokens(refreshedSession)

                if (refreshedSession.user) {
                    setUser(refreshedSession.user)
                    return
                }
            }

            if (storedUser) {
                setUser(storedUser)
                return
            }

            if (!accessToken) {
                setUser(null)
                return
            }

            setUser(null)
        } catch (error) {
            console.error('Auth check failed:', error)
            clearSessionStorage()
            setUser(null)
        } finally {
            setIsLoading(false)
        }
    }

    // Login function
    const login = async (email: string, password: string) => {
        const session = await authApi.login({ email, password })

        setStoredSessionTokens(session)
        clearRedirectAfterLogin()
        clearRedirectSuppression()
        setUser(session.user)
    }

    // Logout function
    const logout = async () => {
        const { refreshToken } = getStoredSessionTokens()

        try {
            suppressRedirectAfterLogin()
            if (refreshToken) {
                await authApi.logout(refreshToken)
            }
        } catch (error) {
            console.error('Logout request failed:', error)
        } finally {
        clearSessionStorage()
        setUser(null)
        toast.info('Logged out successfully')
        }
    }

    const logoutAll = async () => {
        const { refreshToken } = getStoredSessionTokens()

        try {
            suppressRedirectAfterLogin()
            if (refreshToken) {
                await authApi.logoutAll(refreshToken)
            }
        } catch (error) {
            console.error('Logout-all request failed:', error)
        } finally {
            clearSessionStorage()
            setUser(null)
            toast.info('Signed out from all sessions')
        }
    }

    // Context value
    const value: AuthContextValue = {
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
        logoutAll,
        checkAuth,
    }

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    )
}
