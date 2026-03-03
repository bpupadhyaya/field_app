import { fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes, useParams } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import DigitalToolsPage from '../../../pages/DigitalToolsPage'

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
    expect(screen.getByRole('button', { name: /Operations Center/i })).toBeInTheDocument()
    expect(screen.getByRole('img', { name: 'MyFinancials preview' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Customer Service Advisor/i })).toBeInTheDocument()
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
