import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import NavBar from './NavBar'

const baseProps = {
  menu: [],
  onNavigate: vi.fn(),
  onOpenLayout: vi.fn(),
  onLogout: vi.fn(),
  canViewAdmin: false,
  canViewSuperAdmin: false,
  canViewManager: false,
  canViewUser: false,
  user: { displayName: 'Test User' }
}

describe('NavBar', () => {
  it('shows manager tab and hides admin tab for manager view', () => {
    render(<NavBar {...baseProps} canViewManager={true} />)

    expect(screen.getByRole('button', { name: 'Manager' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Admin' })).not.toBeInTheDocument()
  })

  it('shows admin tab for admin view', () => {
    render(<NavBar {...baseProps} canViewAdmin={true} />)

    expect(screen.getByRole('button', { name: 'Admin' })).toBeInTheDocument()
  })

  it('executes logout handler when logout clicked', () => {
    render(<NavBar {...baseProps} />)
    fireEvent.click(screen.getByRole('button', { name: 'Logout' }))
    expect(baseProps.onLogout).toHaveBeenCalledTimes(1)
  })
})
