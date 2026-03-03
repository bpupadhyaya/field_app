import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import DashboardPage from '../../../pages/DashboardPage'

const apiMock = vi.fn()

vi.mock('../../../api', () => ({
  api: (...args: unknown[]) => apiMock(...args)
}))

describe('DashboardPage', () => {
  beforeEach(() => {
    apiMock.mockReset()
  })

  it('loads devices and drills into health and essential details', async () => {
    apiMock.mockImplementation((path: string) => {
      if (path === '/api/devices') {
        return Promise.resolve([
          {
            id: 1,
            name: 'Harvester',
            locationName: 'Zone A',
            status: 'ONLINE'
          }
        ])
      }
      if (path === '/api/devices/1/health') return Promise.resolve({ battery: 80 })
      if (path === '/api/devices/1/essential') return Promise.resolve({ mode: 'AUTO' })
      return Promise.resolve([])
    })

    render(<DashboardPage />)

    expect(await screen.findByRole('button', { name: /Harvester/i })).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: /Harvester/i }))

    await waitFor(() => {
      expect(screen.getByText('Status: ONLINE')).toBeInTheDocument()
      expect(screen.getByText(/"battery": 80/)).toBeInTheDocument()
      expect(screen.getByText(/"mode": "AUTO"/)).toBeInTheDocument()
    })
  })
})
