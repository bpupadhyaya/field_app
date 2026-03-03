import { beforeEach, describe, expect, it, vi } from 'vitest'

const renderMock = vi.fn()
const createRootMock = vi.fn(() => ({ render: renderMock }))

vi.mock('react-dom/client', () => ({
  createRoot: createRootMock
}))

vi.mock('../../App', () => ({
  default: () => null
}))

describe('main', () => {
  beforeEach(() => {
    vi.resetModules()
    document.body.innerHTML = '<div id="root"></div>'
    renderMock.mockReset()
    createRootMock.mockClear()
  })

  it('mounts application into root element', async () => {
    await import('../../main')

    expect(createRootMock).toHaveBeenCalledWith(document.getElementById('root'))
    expect(renderMock).toHaveBeenCalledTimes(1)
  })
})
