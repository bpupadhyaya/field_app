import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import LayoutModal from '../../../components/LayoutModal'

describe('LayoutModal', () => {
  it('renders nothing when closed', () => {
    const { container } = render(
      <LayoutModal open={false} onClose={vi.fn()} layout={{}} setLayout={vi.fn()} />
    )
    expect(container.firstChild).toBeNull()
  })

  it('toggles a layout option and handles close actions', () => {
    const onClose = vi.fn()
    const setLayout = vi.fn()
    const layout = {
      price24h: false,
      price7d: true,
      price30d: false,
      price90d: false,
      showLocation: true,
      showStatus: true
    }

    render(<LayoutModal open={true} onClose={onClose} layout={layout} setLayout={setLayout} />)

    fireEvent.click(screen.getByRole('button', { name: '24h Chart' }))
    expect(setLayout).toHaveBeenCalledWith({ ...layout, price24h: true })

    const modal = screen.getByRole('heading', { name: 'Customize Layout' }).parentElement as HTMLElement
    fireEvent.click(modal)
    expect(onClose).not.toHaveBeenCalled()

    fireEvent.click(screen.getByRole('button', { name: 'Close' }))
    expect(onClose).toHaveBeenCalledTimes(1)

    fireEvent.click(screen.getByText('Customize Layout').closest('.modal-backdrop') as HTMLElement)
    expect(onClose).toHaveBeenCalledTimes(2)
  })
})
