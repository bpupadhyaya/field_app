import { act, fireEvent, render, screen } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
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

  afterEach(() => {
    vi.useRealTimers()
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

  it('renders layout control in dedicated far-right slot', () => {
    render(<NavBar {...baseProps} canViewManager={true} canViewUser={true} />)

    const layoutButton = screen.getByRole('button', { name: 'Layout' })
    const layoutSlot = layoutButton.closest('.layout-slot')
    expect(layoutSlot).not.toBeNull()

    const menuRow = layoutButton.closest('.menu-row') as HTMLElement
    const actionButtons = Array.from(menuRow.querySelectorAll(':scope > button.menu-button')).map((b) => b.textContent)
    expect(actionButtons).not.toContain('Layout')
  })

  it('executes logout handler when logout clicked', () => {
    render(<NavBar {...baseProps} />)
    fireEvent.click(screen.getByRole('button', { name: 'Logout' }))
    expect(baseProps.onLogout).toHaveBeenCalledTimes(1)
  })

  it('keeps dropdown visible briefly during hover transition and closes after delay', () => {
    vi.useFakeTimers()
    render(
      <NavBar
        {...baseProps}
        menu={[{ key: 'equipment', title: 'Equipment', items: ['Large Loader'] }]}
      />
    )

    const menuItem = screen.getByText('Equipment').closest('.menu-item') as HTMLElement
    fireEvent.mouseEnter(menuItem)
    expect(screen.getByRole('button', { name: 'Large Loader' })).toBeInTheDocument()

    fireEvent.mouseLeave(menuItem)
    expect(screen.getByRole('button', { name: 'Large Loader' })).toBeInTheDocument()

    act(() => {
      vi.advanceTimersByTime(120)
    })
    expect(screen.queryByRole('button', { name: 'Large Loader' })).not.toBeInTheDocument()
  })

  it('keeps dropdown open when pointer reaches dropdown before close delay expires', () => {
    vi.useFakeTimers()
    render(
      <NavBar
        {...baseProps}
        menu={[{ key: 'equipment', title: 'Equipment', items: ['Large Loader'] }]}
      />
    )

    const menuItem = screen.getByText('Equipment').closest('.menu-item') as HTMLElement
    fireEvent.mouseEnter(menuItem)
    const dropdown = screen.getByRole('button', { name: 'Large Loader' }).closest('.dropdown') as HTMLElement

    fireEvent.mouseLeave(menuItem)
    fireEvent.mouseEnter(dropdown)
    act(() => {
      vi.advanceTimersByTime(300)
    })
    expect(screen.getByRole('button', { name: 'Large Loader' })).toBeInTheDocument()

    fireEvent.mouseLeave(dropdown)
    act(() => {
      vi.advanceTimersByTime(120)
    })
    expect(screen.queryByRole('button', { name: 'Large Loader' })).not.toBeInTheDocument()
  })
})
