import { fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes, useParams } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import DigitalToolsPage from '../../../pages/DigitalToolsPage'

const EXPECTED_DIGITAL_ITEMS = [
  'Operations Center',
  'Equipment Mobile',
  'MyFinancials',
  'iOS Mobile Apps',
  'Android Mobile Apps',
  'GNSS & Starfire Tools',
  'Company University',
  'Product Activation and Management',
  'Rewards',
  'Display & Command Simulator',
  'TimberManager',
  'SmartGrade Remote Support',
  'Customer Service Advisor'
]

function escapeRegex(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

function DigitalItemRoute() {
  const { item } = useParams()
  return <h2>{decodeURIComponent(item || '')}</h2>
}

describe('DigitalToolsPage', () => {
  it('renders digital tools cards with preview images', () => {
    render(
      <MemoryRouter initialEntries={['/section/digital']}>
        <DigitalToolsPage />
      </MemoryRouter>
    )

    expect(screen.getByRole('heading', { name: 'Digital Tools' })).toBeInTheDocument()
    for (const item of EXPECTED_DIGITAL_ITEMS) {
      expect(screen.getByRole('button', { name: new RegExp(`\\b${escapeRegex(item)}\\b`, 'i') })).toBeInTheDocument()
      expect(screen.getByRole('img', { name: `${item} preview` })).toBeInTheDocument()
    }
    expect(screen.getAllByRole('button')).toHaveLength(EXPECTED_DIGITAL_ITEMS.length)
  })

  it('navigates to selected digital item page on click', () => {
    render(
      <MemoryRouter initialEntries={['/section/digital']}>
        <Routes>
          <Route path="/section/digital" element={<DigitalToolsPage />} />
          <Route path="/section/digital/:item" element={<DigitalItemRoute />} />
        </Routes>
      </MemoryRouter>
    )

    fireEvent.click(screen.getByRole('button', { name: /MyFinancials/i }))
    expect(screen.getByRole('heading', { name: 'MyFinancials' })).toBeInTheDocument()
  })
})
