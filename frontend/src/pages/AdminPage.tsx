import React, { useEffect, useState } from 'react'
import { api } from '../api'

interface Props { roles: string[] }

export default function AdminPage({ roles }: Props) {
  const [runtime, setRuntime] = useState<unknown[]>([])
  const [traffic, setTraffic] = useState<unknown[]>([])
  const [audit, setAudit] = useState<unknown[]>([])
  const [snapshots, setSnapshots] = useState<unknown[]>([])

  async function load() {
    setRuntime(await api('/api/admin/runtime'))
    setTraffic(await api('/api/admin/traffic'))
    setSnapshots(await api('/api/admin/snapshots'))
    if (roles.includes('ROLE_ADMIN') || roles.includes('ROLE_SUPER_ADMIN')) setAudit(await api('/api/admin/audit'))
  }

  useEffect(() => { load() }, [])

  async function createSnapshot() { await api('/api/admin/snapshots', { method: 'POST' }); load() }
  async function cleanup() { await api('/api/admin/snapshots/cleanup', { method: 'POST', body: JSON.stringify({ keepMinCount: 5, maxAgeDays: 30 }) }); load() }

  return (
    <div className="grid-2">
      <div className="card">
        <h2>Runtime / Datacenters</h2>
        <button onClick={createSnapshot}>Capture Snapshot</button>
        <button onClick={cleanup}>Cleanup Old Snapshots</button>
        <h3>Online/Offline Users</h3>
        <pre>{JSON.stringify(runtime, null, 2)}</pre>
      </div>
      <div className="card">
        <h2>Traffic</h2>
        <pre>{JSON.stringify(traffic, null, 2)}</pre>
        <h3>Snapshots</h3>
        <pre>{JSON.stringify(snapshots, null, 2)}</pre>
        {(roles.includes('ROLE_ADMIN') || roles.includes('ROLE_SUPER_ADMIN')) && <><h3>Audit</h3><pre>{JSON.stringify(audit, null, 2)}</pre></>}
      </div>
    </div>
  )
}
