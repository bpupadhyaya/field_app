import React, { useEffect, useState } from 'react'
import { api } from '../api'
import type { Device, PricePoint } from '../types'

interface Props { layout: Record<string, boolean> }

function sparkline(points: PricePoint[]) {
  if (!points.length) return null
  const values = points.map((p) => Number(p.price))
  const min = Math.min(...values)
  const max = Math.max(...values)
  const norm = (v: number) => (max === min ? 10 : 20 - ((v - min) / (max - min)) * 20)
  const path = values.map((v, i) => `${i === 0 ? 'M' : 'L'} ${i * (60 / Math.max(values.length - 1, 1))} ${norm(v)}`).join(' ')
  return <svg width="60" height="20"><path d={path} fill="none" stroke="#39d98a" strokeWidth="2" /></svg>
}

export default function DevicesPage({ layout }: Props) {
  const [q, setQ] = useState('')
  const [devices, setDevices] = useState<Device[]>([])
  const [history, setHistory] = useState<Record<number, PricePoint[]>>({})
  const [sortBy, setSortBy] = useState<keyof Device>('name')
  const [asc, setAsc] = useState(true)

  async function load() {
    const ds = await api<Device[]>(`/api/devices${q ? `?q=${encodeURIComponent(q)}` : ''}`)
    setDevices(ds)
    const ranges: string[] = []
    if (layout.price24h) ranges.push('24h')
    if (layout.price7d) ranges.push('7d')
    if (layout.price30d) ranges.push('30d')
    if (layout.price90d) ranges.push('90d')
    const selectedRange = ranges[0] || '7d'
    const pairs = await Promise.all(ds.map(async (d) => [d.id, await api<PricePoint[]>(`/api/devices/${d.id}/price-history?range=${selectedRange}`)] as const))
    setHistory(Object.fromEntries(pairs))
  }

  useEffect(() => { load() }, [layout.price24h, layout.price7d, layout.price30d, layout.price90d])

  const sorted = [...devices].sort((a, b) => {
    const av = a[sortBy]
    const bv = b[sortBy]
    if (av === bv) return 0
    return asc ? (av > bv ? 1 : -1) : (av < bv ? 1 : -1)
  })

  async function control(device: Device, command: string) {
    try {
      const res = await api<{ message: string }>(`/api/devices/${device.id}/control`, {
        method: 'POST',
        body: JSON.stringify({ command, zoneId: device.zoneId })
      })
      alert(res.message)
    } catch (e) {
      alert(`Control failed: ${String((e as Error).message || '')}`)
    }
  }

  async function updatePrice(id: number) {
    const p = prompt('New price?')
    if (!p) return
    await api(`/api/devices/${id}/price`, { method: 'PATCH', body: JSON.stringify({ price: Number(p) }) })
    load()
  }

  return (
    <div className="card">
      <h2>Equipment / Device Search</h2>
      <div className="row">
        <input value={q} onChange={(e) => setQ(e.target.value)} placeholder="Search devices" />
        <button className="btn-primary" onClick={load}>Search</button>
      </div>
      <table>
        <thead>
          <tr>
            {(['name', 'category', 'type', 'currentPrice', 'status', 'locationName'] as (keyof Device)[]).map((h) => (
              <th key={h} onClick={() => { if (sortBy === h) setAsc(!asc); else { setSortBy(h); setAsc(true) } }}>{h}</th>
            ))}
            <th>Chart</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {sorted.map((d) => (
            <tr key={d.id}>
              <td>{d.name}</td>
              <td>{d.category}</td>
              <td>{d.type}</td>
              <td>{d.currentPrice}</td>
              <td>{layout.showStatus ? d.status : '-'}</td>
              <td>{layout.showLocation ? d.locationName : '-'}</td>
              <td>{sparkline(history[d.id] || [])}</td>
              <td>
                <button onClick={() => control(d, 'lane_navigation')}>Lane</button>
                <button onClick={() => control(d, 'loading')}>Load</button>
                <button onClick={() => updatePrice(d.id)}>Update Price</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
