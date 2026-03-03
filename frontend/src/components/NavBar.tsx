import React, { useState } from 'react'

interface MenuGroup {
  key: string
  title: string
  items: string[]
}

interface Props {
  menu: MenuGroup[]
  onNavigate: (path: string) => void
  onOpenLayout: () => void
  onLogout: () => void
  canViewAdmin: boolean
  canViewSuperAdmin: boolean
  canViewManager: boolean
  canViewUser: boolean
  user: { displayName?: string; username?: string } | null
}

export default function NavBar({ menu, onNavigate, onOpenLayout, onLogout, canViewAdmin, canViewSuperAdmin, canViewManager, canViewUser, user }: Props) {
  const [open, setOpen] = useState<string | null>(null)

  return (
    <div className="header-wrap">
      <div className="brand-row">
        <div className="brand">Bhim's Field App</div>
        <div className="user-info">
          {user?.displayName || user?.username} <button className="ghost" onClick={onLogout}>Logout</button>
        </div>
      </div>
      <div className="menu-row">
        {menu.map((m) => (
          <div key={m.key} className="menu-item" onMouseEnter={() => setOpen(m.key)} onMouseLeave={() => setOpen(null)}>
            <button className="menu-button" onClick={() => onNavigate(`/section/${m.key}`)}>{m.title}</button>
            {open === m.key && (
              <div className="dropdown">
                {m.items.map((item) => (
                  <button key={item} className="dropdown-item" onClick={() => onNavigate(`/section/${m.key}/${encodeURIComponent(item)}`)}>{item}</button>
                ))}
              </div>
            )}
          </div>
        ))}
        <button className="menu-button" onClick={onOpenLayout}>Layout</button>
        <button className="menu-button" onClick={() => onNavigate('/dashboard')}>Dashboard</button>
        <button className="menu-button" onClick={() => onNavigate('/devices')}>Device Search</button>
        {canViewManager && <button className="menu-button" onClick={() => onNavigate('/manager')}>Manager</button>}
        {canViewUser && <button className="menu-button" onClick={() => onNavigate('/user')}>User</button>}
        {canViewSuperAdmin
          ? <button className="menu-button" onClick={() => onNavigate('/superadmin')}>Super Admin</button>
          : canViewAdmin && <button className="menu-button" onClick={() => onNavigate('/admin')}>Admin</button>}
      </div>
    </div>
  )
}
