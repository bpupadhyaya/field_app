import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import App from '../../App'

let token: string | null = null
const apiMock = vi.fn()

vi.mock('../../api', () => ({
  api: (...args: unknown[]) => apiMock(...args),
  authStore: {
    get token() {
      return token
    },
    set token(v: string | null) {
      token = v
    }
  }
}))

describe('App', () => {
  beforeEach(() => {
    token = null
    apiMock.mockReset()
  })

  it('renders login page when unauthenticated and logs in successfully', async () => {
    apiMock.mockImplementation((path: string) => {
      if (path === '/api/auth/login') {
        return Promise.resolve({
          token: 'jwt',
          username: 'manager1',
          displayName: 'Manager',
          roles: ['ROLE_MANAGER']
        })
      }
      if (path === '/api/site/navigation') return Promise.resolve([])
      if (path === '/api/devices') return Promise.resolve([])
      return Promise.resolve({})
    })

    render(
      <MemoryRouter initialEntries={['/login']}>
        <App />
      </MemoryRouter>
    )

    fireEvent.change(screen.getByPlaceholderText('username'), { target: { value: 'manager1' } })
    fireEvent.change(screen.getByPlaceholderText('password'), { target: { value: 'manager123' } })
    fireEvent.click(screen.getByRole('button', { name: 'Login' }))

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'Device Dashboard' })).toBeInTheDocument()
      expect(screen.getByRole('button', { name: 'Manager' })).toBeInTheDocument()
      expect(screen.queryByRole('button', { name: 'Admin' })).not.toBeInTheDocument()
    })
  })

  it('handles bootstrap failure by clearing token and showing login', async () => {
    token = 'existing'
    apiMock.mockRejectedValue(new Error('unauthorized'))

    render(
      <MemoryRouter initialEntries={['/dashboard']}>
        <App />
      </MemoryRouter>
    )

    expect(await screen.findByRole('heading', { name: 'Field App Login' })).toBeInTheDocument()
    expect(token).toBeNull()
  })

  it('renders section route and decodes section item', async () => {
    token = 'ok'
    apiMock.mockImplementation((path: string) => {
      if (path === '/api/users/whoami') return Promise.resolve({ username: 'admin', roles: ['ROLE_ADMIN'] })
      if (path === '/api/site/navigation') return Promise.resolve([])
      return Promise.resolve([])
    })

    render(
      <MemoryRouter initialEntries={['/section/equipment/Large%20Loader']}>
        <App />
      </MemoryRouter>
    )

    expect(await screen.findByRole('heading', { name: 'Large Loader' })).toBeInTheDocument()
  })

  it('renders section page title from section when item is absent', async () => {
    token = 'ok'
    apiMock.mockImplementation((path: string) => {
      if (path === '/api/users/whoami') return Promise.resolve({ username: 'admin', roles: ['ROLE_ADMIN'] })
      if (path === '/api/site/navigation') return Promise.resolve([])
      return Promise.resolve([])
    })

    render(
      <MemoryRouter initialEntries={['/section/equipment']}>
        <App />
      </MemoryRouter>
    )

    expect(await screen.findByRole('heading', { name: 'equipment' })).toBeInTheDocument()
  })

  it('renders digital tools page for /section/digital and supports item navigation', async () => {
    token = 'ok'
    apiMock.mockImplementation((path: string) => {
      if (path === '/api/users/whoami') return Promise.resolve({ username: 'admin', roles: ['ROLE_ADMIN'] })
      if (path === '/api/site/navigation') return Promise.resolve([])
      return Promise.resolve([])
    })

    render(
      <MemoryRouter initialEntries={['/section/digital']}>
        <App />
      </MemoryRouter>
    )

    expect(await screen.findByRole('heading', { name: 'Digital Tools' })).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: /MyFinancials/i }))
    expect(await screen.findByRole('heading', { name: 'MyFinancials' })).toBeInTheDocument()
  })

  it('renders digital tools page for /section/digital/Digital Tools', async () => {
    token = 'ok'
    apiMock.mockImplementation((path: string) => {
      if (path === '/api/users/whoami') return Promise.resolve({ username: 'admin', roles: ['ROLE_ADMIN'] })
      if (path === '/api/site/navigation') return Promise.resolve([])
      return Promise.resolve([])
    })

    render(
      <MemoryRouter initialEntries={['/section/digital/Digital%20Tools']}>
        <App />
      </MemoryRouter>
    )

    expect(await screen.findByRole('heading', { name: 'Digital Tools' })).toBeInTheDocument()
  })

  it('shows not authorized for admin route when manager is logged in and allows logout', async () => {
    token = 'ok'
    apiMock.mockImplementation((path: string) => {
      if (path === '/api/users/whoami') return Promise.resolve({ username: 'manager1', roles: ['ROLE_MANAGER'] })
      if (path === '/api/site/navigation') return Promise.resolve([])
      return Promise.resolve([])
    })

    render(
      <MemoryRouter initialEntries={['/admin']}>
        <App />
      </MemoryRouter>
    )

    expect(await screen.findByRole('heading', { name: 'Not Authorized' })).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Logout' }))
    expect(await screen.findByRole('heading', { name: 'Field App Login' })).toBeInTheDocument()
  })

  it('allows user route for user role and superadmin route for super admin role', async () => {
    token = 'ok'
    apiMock.mockImplementation((path: string) => {
      if (path === '/api/users/whoami') return Promise.resolve({ username: 'user1', roles: ['ROLE_USER'] })
      if (path === '/api/site/navigation') return Promise.resolve([])
      return Promise.resolve([])
    })

    render(
      <MemoryRouter initialEntries={['/user']}>
        <App />
      </MemoryRouter>
    )
    expect(await screen.findByRole('heading', { name: 'User Console' })).toBeInTheDocument()

    cleanup()
    apiMock.mockReset()
    apiMock.mockImplementation((path: string) => {
      if (path === '/api/users/whoami') return Promise.resolve({ username: 'sa', roles: ['ROLE_SUPER_ADMIN'] })
      if (path === '/api/site/navigation') return Promise.resolve([])
      if (path === '/api/superadmin/cloud-topology') return Promise.resolve([])
      if (path === '/api/superadmin/ai-management') return Promise.resolve(null)
      if (path === '/api/admin/runtime') return Promise.resolve([])
      if (path === '/api/admin/traffic') return Promise.resolve([])
      if (path === '/api/admin/snapshots') return Promise.resolve([])
      if (path === '/api/admin/audit') return Promise.resolve([])
      return Promise.resolve([])
    })

    render(
      <MemoryRouter initialEntries={['/superadmin']}>
        <App />
      </MemoryRouter>
    )
    expect(await screen.findByRole('heading', { name: /Super Admin: Cloud Topology/i })).toBeInTheDocument()
  })

  it('handles missing roles by treating them as empty access', async () => {
    token = 'ok'
    apiMock.mockImplementation((path: string) => {
      if (path === '/api/users/whoami') return Promise.resolve({ username: 'nobody' })
      if (path === '/api/site/navigation') return Promise.resolve([])
      return Promise.resolve([])
    })

    render(
      <MemoryRouter initialEntries={['/admin']}>
        <App />
      </MemoryRouter>
    )

    expect(await screen.findByRole('heading', { name: 'Not Authorized' })).toBeInTheDocument()
  })
})
