import React, { useState } from 'react'
import { api, authStore } from '../api'
import type { LoginResponse } from '../types'

interface Props { onLogin: (data: LoginResponse) => void }

export default function LoginPage({ onLogin }: Props) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    try {
      const data = await api<LoginResponse>('/api/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) })
      authStore.token = data.token
      onLogin(data)
    } catch {
      setError('Invalid credentials')
    }
  }

  return (
    <div className="login-wrap">
      <form className="card" onSubmit={submit}>
        <h1>Field App Login</h1>
        <input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="username" />
        <input value={password} onChange={(e) => setPassword(e.target.value)} type="password" placeholder="password" />
        {error && <div className="error">{error}</div>}
        <button className="btn-primary" type="submit">Login</button>
      </form>
    </div>
  )
}
