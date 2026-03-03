import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import AdminPage from '../../../pages/AdminPage'

const apiMock = vi.fn()

vi.mock('../../../api', () => ({
  api: (...args: unknown[]) => apiMock(...args)
}))

describe('AdminPage', () => {
  beforeEach(() => {
    apiMock.mockReset()
    apiMock.mockImplementation((path: string) => {
      if (path === '/api/admin/runtime') return Promise.resolve([{ users: 1 }])
      if (path === '/api/admin/traffic') return Promise.resolve([{ qps: 10 }])
      if (path === '/api/admin/snapshots') return Promise.resolve([{ id: 1 }])
      if (path === '/api/admin/audit') return Promise.resolve([{ action: 'x' }])
      return Promise.resolve({ message: 'ok' })
    })
  })

  it('loads admin data and audit for admin role, and executes snapshot actions', async () => {
    render(<AdminPage roles={['ROLE_ADMIN']} />)

    expect(await screen.findByText(/"users": 1/)).toBeInTheDocument()
    expect(screen.getByText(/"qps": 10/)).toBeInTheDocument()
    expect(screen.getByText(/"id": 1/)).toBeInTheDocument()
    expect(screen.getByText(/"action": "x"/)).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Capture Snapshot' }))
    fireEvent.click(screen.getByRole('button', { name: 'Cleanup Old Snapshots' }))

    await waitFor(() => {
      expect(apiMock).toHaveBeenCalledWith('/api/admin/snapshots', { method: 'POST' })
      expect(apiMock).toHaveBeenCalledWith('/api/admin/snapshots/cleanup', {
        method: 'POST',
        body: JSON.stringify({ keepMinCount: 5, maxAgeDays: 30 })
      })
    })
  })

  it('does not fetch audit for non-admin roles', async () => {
    render(<AdminPage roles={['ROLE_MANAGER']} />)

    await screen.findByText(/"users": 1/)
    expect(apiMock).not.toHaveBeenCalledWith('/api/admin/audit')
    expect(screen.queryByRole('heading', { name: 'Audit' })).not.toBeInTheDocument()
  })
})
