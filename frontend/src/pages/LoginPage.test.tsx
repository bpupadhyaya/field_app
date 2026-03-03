import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import LoginPage from './LoginPage'

const apiMock = vi.fn()

vi.mock('../api', () => ({
  api: (...args: unknown[]) => apiMock(...args),
  authStore: { token: null as string | null }
}))

describe('LoginPage', () => {
  beforeEach(() => {
    apiMock.mockReset()
  })

  it('submits credentials and calls onLogin on success', async () => {
    const onLogin = vi.fn()
    apiMock.mockResolvedValue({
      token: 'jwt-token',
      username: 'admin',
      displayName: 'Admin',
      roles: ['ROLE_ADMIN']
    })

    render(<LoginPage onLogin={onLogin} />)

    fireEvent.change(screen.getByPlaceholderText('username'), { target: { value: 'admin' } })
    fireEvent.change(screen.getByPlaceholderText('password'), { target: { value: 'admin123' } })
    fireEvent.click(screen.getByRole('button', { name: 'Login' }))

    await waitFor(() => expect(apiMock).toHaveBeenCalledTimes(1))
    expect(onLogin).toHaveBeenCalledWith(
      expect.objectContaining({ username: 'admin', token: 'jwt-token' })
    )
  })

  it('shows invalid credential error on failed login', async () => {
    apiMock.mockRejectedValue(new Error('bad creds'))

    render(<LoginPage onLogin={vi.fn()} />)
    fireEvent.click(screen.getByRole('button', { name: 'Login' }))

    await screen.findByText('Invalid credentials')
  })
})
