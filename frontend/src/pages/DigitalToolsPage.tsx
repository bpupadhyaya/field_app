import React from 'react'
import { useNavigate } from 'react-router-dom'

interface DigitalTool {
  title: string
  description: string
  color: string
}

const DIGITAL_TOOLS: DigitalTool[] = [
  { title: 'Operations Center', description: 'Plan and monitor field operations with connected insights.', color: '#2f7bdc' },
  { title: 'Equipment Mobile', description: 'Track machine status and location from your phone.', color: '#2fa66c' },
  { title: 'MyFinancials', description: 'Review statements, balances, and financing activity.', color: '#2f9bcf' },
  { title: 'iOS Mobile Apps', description: 'Access productivity apps designed for iPhone and iPad.', color: '#6b67d5' },
  { title: 'Android Mobile Apps', description: 'Use toolkits and workflows optimized for Android.', color: '#48a148' },
  { title: 'GNSS & Starfire Tools', description: 'Configure precision guidance and correction services.', color: '#b07d2e' },
  { title: 'Company University', description: 'Explore digital training content for operators and teams.', color: '#8f53ba' },
  { title: 'Product Activation and Management', description: 'Activate subscriptions and manage digital entitlements.', color: '#3e8c91' },
  { title: 'Rewards', description: 'View earned rewards and eligible value-added offers.', color: '#b75656' },
  { title: 'Display & Command Simulator', description: 'Practice display workflows in a safe simulation mode.', color: '#4970bf' },
  { title: 'TimberManager', description: 'Coordinate forestry operations and logistics digitally.', color: '#4f8e52' },
  { title: 'SmartGrade Remote Support', description: 'Enable guided setup and troubleshooting assistance.', color: '#9060b8' },
  { title: 'Customer Service Advisor', description: 'Use service diagnostics and maintenance insights.', color: '#5478a8' }
]

function toolImageDataUrl(tool: DigitalTool): string {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="360" height="220" viewBox="0 0 360 220"><rect width="360" height="220" rx="16" fill="${tool.color}"/><rect x="26" y="26" width="148" height="168" rx="12" fill="#0f1728" stroke="#d6e3ff" stroke-width="2"/><rect x="40" y="48" width="120" height="76" rx="8" fill="#dde9ff"/><rect x="40" y="136" width="120" height="12" rx="6" fill="#dde9ff"/><rect x="40" y="156" width="80" height="12" rx="6" fill="#dde9ff"/><text x="196" y="88" font-size="20" font-family="Arial, sans-serif" fill="#f5f8ff">Digital Tool</text><text x="196" y="122" font-size="16" font-family="Arial, sans-serif" fill="#f5f8ff">${tool.title.replace(/&/g, '&amp;')}</text></svg>`
  return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`
}

export default function DigitalToolsPage() {
  const navigate = useNavigate()

  return (
    <div className="card">
      <h2>Digital Tools</h2>
      <p>Choose a digital capability below. Each option opens its dedicated in-app page.</p>
      <div className="digital-tools-grid">
        {DIGITAL_TOOLS.map((tool) => (
          <button
            key={tool.title}
            type="button"
            className="digital-tool-card"
            onClick={() => navigate(`/section/digital/${encodeURIComponent(tool.title)}`)}
          >
            <img className="digital-tool-image" src={toolImageDataUrl(tool)} alt={`${tool.title} preview`} />
            <span className="digital-tool-copy">
              <strong>{tool.title}</strong>
              <span>{tool.description}</span>
            </span>
          </button>
        ))}
      </div>
    </div>
  )
}
