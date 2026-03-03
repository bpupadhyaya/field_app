import React from 'react'

interface Props { title: string; subtitle?: string }

export default function GenericSectionPage({ title, subtitle }: Props) {
  return (
    <div className="card">
      <h2>{title}</h2>
      <p>{subtitle || 'Placeholder page with room for detailed business flows and content.'}</p>
    </div>
  )
}
