import React from 'react'

export default function UserPage() {
  return (
    <div className="card">
      <h2>User Console</h2>
      <p>User capabilities: read device data and perform allowed partial controls within assigned zones.</p>
      <ul>
        <li>Use <b>Device Search</b> for read + partial control workflows.</li>
        <li>Use <b>Dashboard</b> to view real-time device status and drilldown details.</li>
        <li>Zone boundary restrictions are enforced by backend policies.</li>
      </ul>
    </div>
  )
}
