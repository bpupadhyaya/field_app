import { beforeEach, describe, expect, it, vi } from 'vitest'
import { api, authStore } from '../../api'

describe('api/authStore', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.restoreAllMocks()
  })

  it('stores and clears token', () => {
    expect(authStore.token).toBeNull()
    authStore.token = 'abc'
    expect(authStore.token).toBe('abc')
    authStore.token = null
    expect(authStore.token).toBeNull()
  })

  it('sends auth header and returns json when response is ok', async () => {
    authStore.token = 'jwt'
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue({ value: 1 })
    } as unknown as Response)

    const data = await api<{ value: number }>('/api/test', { headers: { 'X-Test': '1' } })

    expect(data.value).toBe(1)
    expect(fetchMock).toHaveBeenCalledWith('/api/test', {
      headers: {
        'Content-Type': 'application/json',
        'X-Test': '1',
        Authorization: 'Bearer jwt'
      }
    })
  })

  it('throws response text when response is not ok', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      text: vi.fn().mockResolvedValue('bad request')
    } as unknown as Response)

    await expect(api('/api/fail')).rejects.toThrow('bad request')
  })
})
