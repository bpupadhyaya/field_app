import React from 'react'

type Layout = Record<string, boolean>

interface Props {
  open: boolean
  onClose: () => void
  layout: Layout
  setLayout: (next: Layout) => void
}

const options = [
  { key: 'price24h', label: '24h Chart' },
  { key: 'price7d', label: '7d Chart' },
  { key: 'price30d', label: '30d Chart' },
  { key: 'price90d', label: '90d Chart' },
  { key: 'showLocation', label: 'Location' },
  { key: 'showStatus', label: 'Status' }
]

export default function LayoutModal({ open, onClose, layout, setLayout }: Props) {
  if (!open) return null
  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h2>Customize Layout</h2>
        <div className="layout-grid">
          {options.map((o) => (
            <button key={o.key} className={layout[o.key] ? 'pill active' : 'pill'} onClick={() => setLayout({ ...layout, [o.key]: !layout[o.key] })}>
              {o.label}
            </button>
          ))}
        </div>
        <button className="btn-primary" onClick={onClose}>Close</button>
      </div>
    </div>
  )
}
