import React, { useEffect, useState } from 'react'
import { api } from '../api'

export default function SuperAdminPage() {
  const [topology, setTopology] = useState<unknown[]>([])
  const [ai, setAi] = useState<unknown>(null)
  const [runtime, setRuntime] = useState<unknown[]>([])
  const [traffic, setTraffic] = useState<unknown[]>([])
  const [audit, setAudit] = useState<unknown[]>([])
  const [snapshots, setSnapshots] = useState<unknown[]>([])

  async function loadAll() {
    const [tp, aiData, rt, tr, sn, au] = await Promise.all([
      api<unknown[]>('/api/superadmin/cloud-topology').catch(() => []),
      api<unknown>('/api/superadmin/ai-management').catch(() => null),
      api<unknown[]>('/api/admin/runtime').catch(() => []),
      api<unknown[]>('/api/admin/traffic').catch(() => []),
      api<unknown[]>('/api/admin/snapshots').catch(() => []),
      api<unknown[]>('/api/admin/audit').catch(() => [])
    ])
    setTopology(tp)
    setAi(aiData)
    setRuntime(rt)
    setTraffic(tr)
    setSnapshots(sn)
    setAudit(au)
  }

  useEffect(() => {
    loadAll()
  }, [])

  async function createSnapshot() {
    await api('/api/admin/snapshots', { method: 'POST' })
    loadAll()
  }

  async function cleanupSnapshots() {
    await api('/api/admin/snapshots/cleanup', {
      method: 'POST',
      body: JSON.stringify({ keepMinCount: 5, maxAgeDays: 30 })
    })
    loadAll()
  }

  async function restoreSnapshot() {
    const id = prompt('Snapshot ID to restore?')
    if (!id) return
    await api(`/api/admin/snapshots/${id}/restore`, { method: 'POST' })
    loadAll()
  }

  return (
    <div className="grid-2">
      <div className="card">
        <h2>Super Admin: Cloud Topology</h2>
        <pre>{JSON.stringify(topology, null, 2)}</pre>
        <h2>Super Admin: AI Management</h2>
        <pre>{JSON.stringify(ai, null, 2)}</pre>
      </div>

      <div className="card">
        <h2>Admin Capabilities (Inherited)</h2>
        <div className="row">
          <button onClick={createSnapshot}>Capture Snapshot</button>
          <button onClick={cleanupSnapshots}>Cleanup Snapshots</button>
          <button onClick={restoreSnapshot}>Restore Snapshot</button>
        </div>
        <h3>Runtime / Online Users</h3>
        <pre>{JSON.stringify(runtime, null, 2)}</pre>
        <h3>Traffic</h3>
        <pre>{JSON.stringify(traffic, null, 2)}</pre>
        <h3>Snapshots</h3>
        <pre>{JSON.stringify(snapshots, null, 2)}</pre>
        <h3>Audit</h3>
        <pre>{JSON.stringify(audit, null, 2)}</pre>
      </div>
    </div>
  )
}
