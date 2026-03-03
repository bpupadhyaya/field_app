const tokenKey = 'field_app_token'

export const authStore = {
  get token(): string | null {
    return localStorage.getItem(tokenKey)
  },
  set token(v: string | null) {
    if (v) localStorage.setItem(tokenKey, v)
    else localStorage.removeItem(tokenKey)
  }
}

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...((options.headers as Record<string, string>) || {})
  }
  if (authStore.token) headers.Authorization = `Bearer ${authStore.token}`
  const res = await fetch(path, { ...options, headers })
  if (!res.ok) throw new Error(await res.text())
  return res.json() as Promise<T>
}
