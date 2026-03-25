import { useState, useCallback } from 'react'
import type { ReactNode } from 'react'
import { Outlet } from 'react-router-dom'
import Header from '../Header'
import Sidebar from '../Sidebar'
import './MainLayout.css'

/**
 * Main application shell.
 *
 * Composes the Header (sticky top bar), Sidebar (fixed side
 * navigation), and a scrollable main content area where the
 * current route renders via `<Outlet />`.
 *
 * On mobile (≤ 768px) the sidebar is hidden off-screen and
 * toggled by the hamburger button in the header.
 */
export default function MainLayout(): ReactNode {
    const [sidebarOpen, setSidebarOpen] = useState(false)

    const toggleSidebar = useCallback(() => {
        setSidebarOpen((prev) => !prev)
    }, [])

    const closeSidebar = useCallback(() => {
        setSidebarOpen(false)
    }, [])

    return (
        <div className="layout">
            <Header onMobileMenuToggle={toggleSidebar} />

            <div className="layout__body">
                <Sidebar isOpen={sidebarOpen} onClose={closeSidebar} />

                <main className="layout__main">
                    <Outlet />
                </main>
            </div>
        </div>
    )
}
