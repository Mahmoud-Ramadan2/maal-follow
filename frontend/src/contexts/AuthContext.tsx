import React, { createContext, useContext, useState, useEffect } from 'react'
import type { ReactNode } from 'react'
import { toast } from 'react-toastify'

// ===== TYPES =====
interface User {
    id: number
    username: string
    email: string
    fullName: string
    roles: string[]
}

interface AuthContextType {
    user: User | null
    isAuthenticated: boolean
    isLoading: boolean
    login: (username: string, password: string) => Promise<void>
    logout: () => void
    checkAuth: () => Promise<void>
}

// ===== CREATE CONTEXT =====
const AuthContext
    = createContext<AuthContextType | undefined>(undefined)

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
    const checkAuth
        = async () => {
        const token = localStorage.getItem('token')

        if (!token) {
            setIsLoading(false)
            return
        }

        try {
            // Verify token with backend
            const response =
                await fetch('/api/auth/me', {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            })

            if (response.ok) {
                const userData = await response.json()
                setUser(userData)
            } else {
                // Token invalid, clear it
                localStorage.removeItem('token')
                setUser(null)
            }
        } catch (error) {
            console.error('Auth check failed:', error)
            localStorage.removeItem('token')
            setUser(null)
        } finally {
            setIsLoading(false)
        }
    }

    // Login function
    const login = async (username: string, password: string) => {
        try {
            const response =
                await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, password }),
            })

            if (!response.ok) {
                throw new Error('Login failed')
            }

            const data = await response.json()

            // Store token
            localStorage.setItem('token', data.token)

            // Store user info
            setUser(data.user)

            toast.success('Login successful!')
        } catch (error) {
            toast.error('Invalid username or password')
            throw error
        }
    }

    // Logout function
    const logout = () => {
        localStorage.removeItem('token')
        setUser(null)
        toast.info('Logged out successfully')
    }

    // Context value
    const value: AuthContextType = {
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
        checkAuth,
    }

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    )
}

// ===== CUSTOM HOOK =====
export const useAuth = () => {
    const context = useContext(AuthContext)
    if (context === undefined) {
        throw new Error('useAuth must be used within AuthProvider')
    }
    return context
}