import React from 'react'

export default function ManagerPage() {
  return (
    <div className="card">
      <h2>Manager Console</h2>
      <p>Manager capabilities: manage assigned devices, issue control commands, monitor operations, and delegate tasks to users.</p>
      <ul>
        <li>Use <b>Device Search</b> to find assigned devices.</li>
        <li>Use <b>Dashboard</b> to drill down into operations and health.</li>
        <li>Use <b>Layout</b> to customize visible columns/charts.</li>
      </ul>
    </div>
  )
}
