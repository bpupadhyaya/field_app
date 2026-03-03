import React, { useEffect, useState } from 'react'
import { api } from '../api'
import type { Device } from '../types'

export default function DashboardPage() {
  const [devices, setDevices] = useState<Device[]>([])
  const [selected, setSelected] = useState<Device | null>(null)
  const [health, setHealth] = useState<unknown>(null)
  const [essential, setEssential] = useState<unknown>(null)

  useEffect(() => { api<Device[]>('/api/devices').then(setDevices) }, [])

  async function drill(device: Device) {
    setSelected(device)
    setHealth(await api(`/api/devices/${device.id}/health`))
    setEssential(await api(`/api/devices/${device.id}/essential`))
  }

  return (
    <div className="grid-2">
      <div className="card">
        <h2>Device Dashboard</h2>
        {devices.map((d) => <button key={d.id} className="list-item" onClick={() => drill(d)}>{d.name} • {d.locationName}</button>)}
      </div>
      <div className="card">
        <h2>Drilldown</h2>
        {!selected && <p>Select a device from left</p>}
        {selected && <>
          <h3>{selected.name}</h3>
          <p>Status: {selected.status}</p>
          <p>Field Ops: harvesting, picking, loading, unloading</p>
          <pre>{JSON.stringify(health, null, 2)}</pre>
          <pre>{JSON.stringify(essential, null, 2)}</pre>
        </>}
      </div>
    </div>
  )
}
