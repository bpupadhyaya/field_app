import React, { useEffect, useState } from 'react'
import { Navigate, Route, Routes, useNavigate, useParams } from 'react-router-dom'
import { api, authStore } from './api'
import LoginPage from './pages/LoginPage'
import NavBar from './components/NavBar'
import LayoutModal from './components/LayoutModal'
import DevicesPage from './pages/DevicesPage'
import DashboardPage from './pages/DashboardPage'
import AdminPage from './pages/AdminPage'
import SuperAdminPage from './pages/SuperAdminPage'
import ManagerPage from './pages/ManagerPage'
import UserPage from './pages/UserPage'
import GenericSectionPage from './pages/GenericSectionPage'
import DigitalToolsPage from './pages/DigitalToolsPage'
import type { LoginResponse } from './types'
import { deriveRoleAccess } from './authz'

interface MenuGroup {
  key: string
  title: string
  items: string[]
}

interface WhoAmI {
  username: string
  displayName?: string
  roles: string[]
}

function SectionRoute() {
  const { section, item } = useParams()
  const decodedItem = item ? decodeURIComponent(item) : null
  if (section === 'digital' && (!decodedItem || decodedItem === 'Digital Tools')) {
    return <DigitalToolsPage />
  }
  const title = decodedItem || section!
  return <GenericSectionPage title={title} subtitle="SPA route rendered in-place without full page refresh." />
}

export default function App() {
  const [user, setUser] = useState<WhoAmI | LoginResponse | null>(null)
  const [menu, setMenu] = useState<MenuGroup[]>([])
  const [layoutOpen, setLayoutOpen] = useState(false)
  const [layout, setLayout] = useState<Record<string, boolean>>({ price24h: false, price7d: true, price30d: false, price90d: false, showLocation: true, showStatus: true })
  const navigate = useNavigate()

  async function bootstrap() {
    if (!authStore.token) return
    try {
      const who = await api<WhoAmI>('/api/users/whoami')
      setUser(who)
      const nav = await api<MenuGroup[]>('/api/site/navigation')
      setMenu(nav)
    } catch {
      authStore.token = null
      setUser(null)
    }
  }

  useEffect(() => { bootstrap() }, [])

  function onLogin(data: LoginResponse) {
    setUser(data)
    api<MenuGroup[]>('/api/site/navigation').then(setMenu)
    navigate('/dashboard')
  }

  function logout() {
    authStore.token = null
    setUser(null)
    navigate('/login')
  }

  if (!authStore.token || !user) return <LoginPage onLogin={onLogin} />

  const roles = user.roles || []
  const { canViewSuperAdmin, canViewAdmin, canViewManager, canViewUser } = deriveRoleAccess(roles)

  return (
    <div className="app-shell">
      <NavBar
        menu={menu}
        user={user}
        onNavigate={navigate}
        onOpenLayout={() => setLayoutOpen(true)}
        onLogout={logout}
        canViewAdmin={canViewAdmin}
        canViewSuperAdmin={canViewSuperAdmin}
        canViewManager={canViewManager}
        canViewUser={canViewUser}
      />
      <LayoutModal open={layoutOpen} onClose={() => setLayoutOpen(false)} layout={layout} setLayout={setLayout} />
      <div className="content">
        <Routes>
          <Route path="/" element={<Navigate to="/dashboard" />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/devices" element={<DevicesPage layout={layout} />} />
          <Route path="/manager" element={canViewManager ? <ManagerPage /> : <GenericSectionPage title="Not Authorized" subtitle="Manager role required." />} />
          <Route path="/user" element={canViewUser ? <UserPage /> : <GenericSectionPage title="Not Authorized" subtitle="User role required." />} />
          <Route path="/admin" element={canViewAdmin ? <AdminPage roles={roles} /> : <GenericSectionPage title="Not Authorized" subtitle="You do not have access to Admin page." />} />
          <Route path="/superadmin" element={canViewSuperAdmin ? <SuperAdminPage /> : <GenericSectionPage title="Not Authorized" subtitle="Super Admin access required." />} />
          <Route path="/section/:section" element={<SectionRoute />} />
          <Route path="/section/:section/:item" element={<SectionRoute />} />
          <Route path="*" element={<Navigate to="/dashboard" />} />
        </Routes>
      </div>
    </div>
  )
}
