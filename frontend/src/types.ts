export type Role = 'ROLE_SUPER_ADMIN' | 'ROLE_ADMIN' | 'ROLE_MANAGER' | 'ROLE_USER' | 'ROLE_DEALER'

export interface LoginResponse {
  token: string
  username: string
  displayName: string
  roles: Role[]
}

export interface Device {
  id: number
  deviceCode: string
  name: string
  category: string
  type: string
  locationName: string
  latitude: number
  longitude: number
  zoneId: string
  status: string
  currentPrice: number
}

export interface PricePoint {
  id: number
  deviceId: number
  price: number
  capturedAt: string
}
