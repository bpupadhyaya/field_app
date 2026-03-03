import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import SuperAdminPage from '../../../pages/SuperAdminPage'

const apiMock = vi.fn()

vi.mock('../../../api', () => ({
  api: (...args: unknown[]) => apiMock(...args)
}))

describe('SuperAdminPage', () => {
  beforeEach(() => {
    apiMock.mockReset()
    vi.stubGlobal('prompt', vi.fn())
  })

  it('loads all datasets and executes snapshot operations', async () => {
    apiMock.mockImplementation((path: string) => {
      if (path === '/api/superadmin/cloud-topology') return Promise.resolve([{ region: 'us-east-1' }])
      if (path === '/api/superadmin/ai-management') return Promise.resolve({ provider: 'x' })
      if (path === '/api/admin/runtime') return Promise.resolve([{ users: 2 }])
      if (path === '/api/admin/traffic') return Promise.resolve([{ qps: 20 }])
      if (path === '/api/admin/snapshots') return Promise.resolve([{ id: 7 }])
      if (path === '/api/admin/audit') return Promise.resolve([{ evt: 'login' }])
      return Promise.resolve({ message: 'ok' })
    })
    ;(globalThis.prompt as ReturnType<typeof vi.fn>).mockReturnValue('7')

    render(<SuperAdminPage />)

    expect(await screen.findByText(/"region": "us-east-1"/)).toBeInTheDocument()
    expect(screen.getByText(/"provider": "x"/)).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Capture Snapshot' }))
    fireEvent.click(screen.getByRole('button', { name: 'Cleanup Snapshots' }))
    fireEvent.click(screen.getByRole('button', { name: 'Restore Snapshot' }))

    await waitFor(() => {
      expect(apiMock).toHaveBeenCalledWith('/api/admin/snapshots', { method: 'POST' })
      expect(apiMock).toHaveBeenCalledWith('/api/admin/snapshots/cleanup', {
        method: 'POST',
        body: JSON.stringify({ keepMinCount: 5, maxAgeDays: 30 })
      })
      expect(apiMock).toHaveBeenCalledWith('/api/admin/snapshots/7/restore', { method: 'POST' })
    })
  })

  it('uses fallback values for failed endpoints and restore cancel path', async () => {
    apiMock.mockImplementation((path: string) => {
      if (path === '/api/superadmin/cloud-topology') return Promise.reject(new Error('x'))
      if (path === '/api/superadmin/ai-management') return Promise.reject(new Error('x'))
      if (path === '/api/admin/runtime') return Promise.reject(new Error('x'))
      if (path === '/api/admin/traffic') return Promise.reject(new Error('x'))
      if (path === '/api/admin/snapshots') return Promise.reject(new Error('x'))
      if (path === '/api/admin/audit') return Promise.reject(new Error('x'))
      return Promise.resolve({})
    })
    ;(globalThis.prompt as ReturnType<typeof vi.fn>).mockReturnValue('')

    render(<SuperAdminPage />)

    expect(await screen.findByText('null')).toBeInTheDocument()
    expect(screen.getAllByText('[]').length).toBeGreaterThan(0)
    fireEvent.click(screen.getByRole('button', { name: 'Restore Snapshot' }))
    expect(apiMock).not.toHaveBeenCalledWith(expect.stringMatching(/\/restore$/), { method: 'POST' })
  })
})
