import { describe, expect, it } from 'vitest'
import { deriveRoleAccess } from '../../authz'

describe('deriveRoleAccess', () => {
  it('grants super admin access to super admin view and admin view', () => {
    const access = deriveRoleAccess(['ROLE_SUPER_ADMIN'])
    expect(access.canViewSuperAdmin).toBe(true)
    expect(access.canViewAdmin).toBe(true)
    expect(access.canViewManager).toBe(false)
    expect(access.canViewUser).toBe(false)
  })

  it('hides admin when manager role is present', () => {
    const access = deriveRoleAccess(['ROLE_ADMIN', 'ROLE_MANAGER'])
    expect(access.canViewAdmin).toBe(false)
    expect(access.canViewManager).toBe(true)
  })

  it('shows user view only for non-manager user role', () => {
    const access = deriveRoleAccess(['ROLE_USER'])
    expect(access.canViewUser).toBe(true)
    expect(access.canViewManager).toBe(false)
    expect(access.canViewAdmin).toBe(false)
  })

  it('returns no access for empty roles', () => {
    const access = deriveRoleAccess([])
    expect(access).toEqual({
      canViewSuperAdmin: false,
      canViewAdmin: false,
      canViewManager: false,
      canViewUser: false
    })
  })

  it('treats undefined roles as empty', () => {
    const access = deriveRoleAccess(undefined as unknown as string[])
    expect(access.canViewAdmin).toBe(false)
    expect(access.canViewSuperAdmin).toBe(false)
  })
})
