import { fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import DevicesPage from '../../../pages/DevicesPage'
import { compareDeviceField } from '../../../pages/DevicesPage'

const apiMock = vi.fn()

vi.mock('../../../api', () => ({
  api: (...args: unknown[]) => apiMock(...args)
}))

const devices = [
  {
    id: 1,
    deviceCode: 'D1',
    name: 'Beta',
    category: 'Harvest',
    type: 'A',
    locationName: 'Zone B',
    latitude: 0,
    longitude: 0,
    zoneId: 'ZB',
    status: 'ONLINE',
    currentPrice: 10
  },
  {
    id: 3,
    deviceCode: 'D3',
    name: 'Alpha',
    category: 'Loader',
    type: 'B',
    locationName: 'Zone C',
    latitude: 0,
    longitude: 0,
    zoneId: 'ZC',
    status: 'ONLINE',
    currentPrice: 30
  },
  {
    id: 2,
    deviceCode: 'D2',
    name: 'Alpha',
    category: 'Loader',
    type: 'B',
    locationName: 'Zone A',
    latitude: 0,
    longitude: 0,
    zoneId: 'ZA',
    status: 'OFFLINE',
    currentPrice: 20
  }
]

describe('DevicesPage', () => {
  beforeEach(() => {
    apiMock.mockReset()
    vi.stubGlobal('alert', vi.fn())
    vi.stubGlobal('prompt', vi.fn())

    apiMock.mockImplementation((path: string, options?: RequestInit) => {
      if (path.startsWith('/api/devices?') || path === '/api/devices') return Promise.resolve(devices)
      if (path.includes('/price-history?range=24h')) return Promise.resolve([{ id: 1, deviceId: 1, price: 9, capturedAt: 'x' }])
      if (path.includes('/price-history?range=7d')) return Promise.resolve([{ id: 2, deviceId: 2, price: 20, capturedAt: 'y' }])
      if (path.endsWith('/control') && options?.method === 'POST') return Promise.resolve({ message: 'ok' })
      if (path.endsWith('/price') && options?.method === 'PATCH') return Promise.resolve({})
      return Promise.resolve([])
    })
  })

  it('loads, sorts, controls devices and updates price', async () => {
    ;(globalThis.prompt as ReturnType<typeof vi.fn>).mockReturnValue('42')

    render(
      <DevicesPage
        layout={{
          price24h: true,
          price7d: false,
          price30d: false,
          price90d: false,
          showLocation: true,
          showStatus: true
        }}
      />
    )

    expect(await screen.findByText('Beta')).toBeInTheDocument()
    expect(apiMock).toHaveBeenCalledWith('/api/devices/1/price-history?range=24h')

    expect(screen.getAllByRole('row')[1]).toHaveTextContent('Alpha')
    fireEvent.click(screen.getByRole('columnheader', { name: 'name' }))
    expect(screen.getAllByRole('row')[1]).toHaveTextContent('Beta')
    fireEvent.change(screen.getByPlaceholderText('Search devices'), { target: { value: 'field ops' } })
    fireEvent.click(screen.getByRole('button', { name: 'Search' }))
    await waitFor(() => expect(apiMock).toHaveBeenCalledWith('/api/devices?q=field%20ops'))

    fireEvent.click(within(screen.getByText('Beta').closest('tr') as HTMLElement).getByRole('button', { name: 'Lane' }))
    await waitFor(() => expect(globalThis.alert).toHaveBeenCalledWith('ok'))

    fireEvent.click(within(screen.getByText('Beta').closest('tr') as HTMLElement).getByRole('button', { name: 'Update Price' }))
    await waitFor(() => {
      expect(apiMock).toHaveBeenCalledWith('/api/devices/1/price', {
        method: 'PATCH',
        body: JSON.stringify({ price: 42 })
      })
    })
  })

  it('handles control failure and update price cancel path', async () => {
    apiMock.mockImplementation((path: string, options?: RequestInit) => {
      if (path.endsWith('/control') && options?.method === 'POST') return Promise.reject(new Error('denied'))
      if (path.startsWith('/api/devices')) return Promise.resolve(devices)
      if (path.includes('/price-history')) return Promise.resolve([])
      return Promise.resolve({})
    })
    ;(globalThis.prompt as ReturnType<typeof vi.fn>).mockReturnValue('')

    render(
      <DevicesPage
        layout={{
          price24h: false,
          price7d: true,
          price30d: false,
          price90d: false,
          showLocation: false,
          showStatus: false
        }}
      />
    )

    expect(await screen.findByText('Beta')).toBeInTheDocument()
    expect(screen.getAllByText('-').length).toBeGreaterThan(0)

    fireEvent.click(within(screen.getByText('Beta').closest('tr') as HTMLElement).getByRole('button', { name: 'Load' }))
    await waitFor(() => {
      expect(globalThis.alert).toHaveBeenCalledWith(expect.stringMatching(/Control failed/))
    })

    const callsBefore = apiMock.mock.calls.length
    fireEvent.click(screen.getAllByRole('button', { name: 'Update Price' })[0])
    expect(apiMock.mock.calls.length).toBe(callsBefore)
  })

  it('uses 30d and 90d ranges and falls back to empty message when error has no message', async () => {
    apiMock.mockImplementation((path: string, options?: RequestInit) => {
      if (path.endsWith('/control') && options?.method === 'POST') return Promise.reject({})
      if (path.startsWith('/api/devices')) return Promise.resolve(devices)
      if (path.includes('/price-history?range=30d')) return Promise.resolve([])
      return Promise.resolve({})
    })

    const { rerender } = render(
      <DevicesPage
        layout={{
          price24h: false,
          price7d: false,
          price30d: true,
          price90d: false,
          showLocation: true,
          showStatus: true
        }}
      />
    )

    expect(await screen.findByText('Beta')).toBeInTheDocument()
    await waitFor(() => expect(apiMock).toHaveBeenCalledWith('/api/devices/1/price-history?range=30d'))

    rerender(
      <DevicesPage
        layout={{
          price24h: false,
          price7d: false,
          price30d: false,
          price90d: true,
          showLocation: true,
          showStatus: true
        }}
      />
    )
    await waitFor(() => expect(apiMock).toHaveBeenCalledWith('/api/devices/1/price-history?range=90d'))

    fireEvent.click(within(screen.getByText('Beta').closest('tr') as HTMLElement).getByRole('button', { name: 'Load' }))
    await waitFor(() => expect(globalThis.alert).toHaveBeenCalledWith('Control failed: '))
  })

  it('falls back to 7d range when no range toggles are enabled and hits sort branch for non-default column', async () => {
    render(
      <DevicesPage
        layout={{
          price24h: false,
          price7d: false,
          price30d: false,
          price90d: false,
          showLocation: true,
          showStatus: true
        }}
      />
    )

    expect(await screen.findByText('Beta')).toBeInTheDocument()
    await waitFor(() => expect(apiMock).toHaveBeenCalledWith('/api/devices/1/price-history?range=7d'))

    fireEvent.click(screen.getByRole('columnheader', { name: 'category' }))
    expect(screen.getAllByRole('row')[1]).toHaveTextContent('Beta')
  })

  it('covers comparator branches directly', () => {
    expect(compareDeviceField('A', 'A', true)).toBe(0)
    expect(compareDeviceField('B', 'A', true)).toBe(1)
    expect(compareDeviceField('A', 'B', true)).toBe(-1)
    expect(compareDeviceField('B', 'A', false)).toBe(-1)
    expect(compareDeviceField('A', 'B', false)).toBe(1)
  })
})
