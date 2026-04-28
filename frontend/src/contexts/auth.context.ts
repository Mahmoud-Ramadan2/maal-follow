import { createContext, useContext } from 'react'
import type { AuthUser } from '@/types/auth.types'

export interface AuthContextValue {
    user: AuthUser | null
    isAuthenticated: boolean
    isLoading: boolean
    login: (username: string, password: string) => Promise<void>
    logout: () => void
    checkAuth: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export const useAuth = () => {
    const context = useContext(AuthContext)
    if (context === undefined) {
        throw new Error('useAuth must be used within AuthProvider')
    }
    return context
}


