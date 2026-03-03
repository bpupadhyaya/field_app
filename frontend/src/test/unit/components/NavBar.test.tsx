import { fireEvent, render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import NavBar from '../../../components/NavBar'

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
  beforeEach(() => {
    baseProps.onNavigate.mockReset()
    baseProps.onOpenLayout.mockReset()
    baseProps.onLogout.mockReset()
  })

  it('shows manager tab and hides admin tab for manager view', () => {
    render(<NavBar {...baseProps} canViewManager={true} />)

    expect(screen.getByRole('button', { name: 'Manager' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Admin' })).not.toBeInTheDocument()
  })

  it('shows admin tab for admin view and fallback username text', () => {
    render(<NavBar {...baseProps} canViewAdmin={true} user={{ username: 'admin1' }} />)

    expect(screen.getByRole('button', { name: 'Admin' })).toBeInTheDocument()
    expect(screen.getByText(/admin1/i)).toBeInTheDocument()
  })

  it('shows super admin tab and supports menu/dropdown navigation and quick actions', () => {
    render(
      <NavBar
        {...baseProps}
        canViewSuperAdmin={true}
        canViewUser={true}
        menu={[{ key: 'equipment', title: 'Equipment', items: ['Large Loader'] }]}
      />
    )

    fireEvent.mouseEnter(screen.getByText('Equipment').closest('.menu-item') as HTMLElement)
    fireEvent.click(screen.getByRole('button', { name: 'Large Loader' }))
    fireEvent.click(screen.getByRole('button', { name: 'Equipment' }))
    fireEvent.mouseLeave(screen.getByText('Equipment').closest('.menu-item') as HTMLElement)

    fireEvent.click(screen.getByRole('button', { name: 'Layout' }))
    fireEvent.click(screen.getByRole('button', { name: 'Dashboard' }))
    fireEvent.click(screen.getByRole('button', { name: 'Device Search' }))
    fireEvent.click(screen.getByRole('button', { name: 'User' }))
    fireEvent.click(screen.getByRole('button', { name: 'Super Admin' }))

    expect(baseProps.onOpenLayout).toHaveBeenCalledTimes(1)
    expect(baseProps.onNavigate).toHaveBeenCalledWith('/section/equipment')
    expect(baseProps.onNavigate).toHaveBeenCalledWith('/section/equipment/Large%20Loader')
    expect(baseProps.onNavigate).toHaveBeenCalledWith('/dashboard')
    expect(baseProps.onNavigate).toHaveBeenCalledWith('/devices')
    expect(baseProps.onNavigate).toHaveBeenCalledWith('/user')
    expect(baseProps.onNavigate).toHaveBeenCalledWith('/superadmin')
  })

  it('executes logout handler when logout clicked', () => {
    render(<NavBar {...baseProps} />)
    fireEvent.click(screen.getByRole('button', { name: 'Logout' }))
    expect(baseProps.onLogout).toHaveBeenCalledTimes(1)
  })
})
