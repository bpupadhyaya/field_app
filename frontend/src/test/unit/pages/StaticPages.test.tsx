import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import GenericSectionPage from '../../../pages/GenericSectionPage'
import ManagerPage from '../../../pages/ManagerPage'
import UserPage from '../../../pages/UserPage'

describe('static pages', () => {
  it('renders manager page copy', () => {
    render(<ManagerPage />)
    expect(screen.getByRole('heading', { name: 'Manager Console' })).toBeInTheDocument()
    expect(screen.getByText(/delegate tasks to users/i)).toBeInTheDocument()
  })

  it('renders user page copy', () => {
    render(<UserPage />)
    expect(screen.getByRole('heading', { name: 'User Console' })).toBeInTheDocument()
    expect(screen.getByText(/Zone boundary restrictions/i)).toBeInTheDocument()
  })

  it('renders generic page with provided and default subtitle', () => {
    const { rerender } = render(<GenericSectionPage title="A" subtitle="B" />)
    expect(screen.getByRole('heading', { name: 'A' })).toBeInTheDocument()
    expect(screen.getByText('B')).toBeInTheDocument()

    rerender(<GenericSectionPage title="X" />)
    expect(screen.getByText(/Placeholder page with room/i)).toBeInTheDocument()
  })
})
